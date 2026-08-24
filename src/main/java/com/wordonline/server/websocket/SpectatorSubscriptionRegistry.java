package com.wordonline.server.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.AbstractSubProtocolEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

// Counts live STOMP subscriptions per destination so the game loop can tell whether anyone is
// listening before it builds a message. SimpMessagingTemplate serializes the payload inside
// convertAndSend, and the broker channel runs inline on the caller, so a message nobody
// subscribed to still costs the loop thread a full JSON encode before the broker drops it.
//
// Every destination is counted, not just spectator ones: the maps are keyed by the subscriptions
// a client actually holds, which is one or two per connected client, and staying destination
// agnostic keeps the STOMP address format out of this class.
@Component
public class SpectatorSubscriptionRegistry implements ApplicationListener<AbstractSubProtocolEvent> {

    // destination -> number of live subscriptions. A destination is absent when the count is zero,
    // so the loop's per-frame question is a single hash lookup.
    private final Map<String, Integer> subscriberCounts = new ConcurrentHashMap<>();

    // websocket session id -> (subscription id -> destination). UNSUBSCRIBE carries only the
    // subscription id, and a dropped connection carries neither, so the destinations a session
    // holds have to be remembered to be released again.
    private final Map<String, Map<String, String>> destinationsBySession = new ConcurrentHashMap<>();

    @Override
    public void onApplicationEvent(AbstractSubProtocolEvent event) {
        if (event instanceof SessionSubscribeEvent subscribeEvent) {
            handleSubscribe(StompHeaderAccessor.wrap(subscribeEvent.getMessage()));
        } else if (event instanceof SessionUnsubscribeEvent unsubscribeEvent) {
            handleUnsubscribe(StompHeaderAccessor.wrap(unsubscribeEvent.getMessage()));
        } else if (event instanceof SessionDisconnectEvent disconnectEvent) {
            handleDisconnect(disconnectEvent.getSessionId());
        }
    }

    // Read once per frame per session on the game loop thread.
    public boolean hasSubscribers(String destination) {
        return subscriberCounts.containsKey(destination);
    }

    public int getSubscriberCount(String destination) {
        return subscriberCounts.getOrDefault(destination, 0);
    }

    private void handleSubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        String destination = accessor.getDestination();
        if (sessionId == null || subscriptionId == null || destination == null) {
            return;
        }

        String replaced = destinationsBySession
                .computeIfAbsent(sessionId, id -> new ConcurrentHashMap<>())
                .put(subscriptionId, destination);
        if (destination.equals(replaced)) {
            // A repeated SUBSCRIBE with the same id is the same subscription, not a second one.
            return;
        }
        if (replaced != null) {
            release(replaced);
        }
        retain(destination);
    }

    private void handleUnsubscribe(StompHeaderAccessor accessor) {
        String sessionId = accessor.getSessionId();
        String subscriptionId = accessor.getSubscriptionId();
        if (sessionId == null || subscriptionId == null) {
            return;
        }

        Map<String, String> destinations = destinationsBySession.get(sessionId);
        if (destinations == null) {
            return;
        }
        // The now empty map is left in place; the session's DISCONNECT removes it. Removing it here
        // would race with a SUBSCRIBE arriving on another inbound channel thread.
        String destination = destinations.remove(subscriptionId);
        if (destination != null) {
            release(destination);
        }
    }

    // A client that drops the connection never sends UNSUBSCRIBE, so this is what keeps the counts
    // from stranding above zero and broadcasting to nobody for the rest of the session.
    private void handleDisconnect(String sessionId) {
        if (sessionId == null) {
            return;
        }

        Map<String, String> destinations = destinationsBySession.remove(sessionId);
        if (destinations == null) {
            return;
        }
        destinations.values().forEach(this::release);
    }

    private void retain(String destination) {
        subscriberCounts.merge(destination, 1, Integer::sum);
    }

    private void release(String destination) {
        subscriberCounts.computeIfPresent(destination, (key, count) -> count <= 1 ? null : count - 1);
    }
}
