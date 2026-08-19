package com.wordonline.server.websocket;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The game loop asks this registry once per frame whether a broadcast is worth building, so a
 * count that strands above zero costs a JSON encode per frame for the rest of the match, and one
 * that strands below zero silently blinds a spectator.
 */
class SpectatorSubscriptionRegistryTest {

    private static final String DESTINATION = "/game/session-1/frameInfos/0";

    private final SpectatorSubscriptionRegistry registry = new SpectatorSubscriptionRegistry();

    @Test
    void reportsNoSubscribersForADestinationNobodyAskedFor() {
        assertThat(registry.hasSubscribers(DESTINATION)).isFalse();
    }

    @Test
    void countsASubscriptionAndReleasesItOnUnsubscribe() {
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));
        assertThat(registry.hasSubscribers(DESTINATION)).isTrue();

        registry.onApplicationEvent(unsubscribe("ws-1", "sub-0"));
        assertThat(registry.hasSubscribers(DESTINATION)).isFalse();
    }

    @Test
    void releasesASubscriptionWhenTheClientDisconnectsWithoutUnsubscribing() {
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));

        registry.onApplicationEvent(disconnect("ws-1"));

        assertThat(registry.hasSubscribers(DESTINATION)).isFalse();
    }

    @Test
    void keepsBroadcastingWhileOneOfTwoSpectatorsRemains() {
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));
        registry.onApplicationEvent(subscribe("ws-2", "sub-0", DESTINATION));

        registry.onApplicationEvent(disconnect("ws-1"));

        assertThat(registry.getSubscriberCount(DESTINATION)).isEqualTo(1);
        assertThat(registry.hasSubscribers(DESTINATION)).isTrue();
    }

    @Test
    void countsARepeatedSubscribeWithTheSameIdOnlyOnce() {
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));

        assertThat(registry.getSubscriberCount(DESTINATION)).isEqualTo(1);

        registry.onApplicationEvent(unsubscribe("ws-1", "sub-0"));

        assertThat(registry.hasSubscribers(DESTINATION)).isFalse();
    }

    @Test
    void movesTheCountWhenASubscriptionIdIsReusedForAnotherDestination() {
        String other = "/game/session-2/frameInfos/0";
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));

        registry.onApplicationEvent(subscribe("ws-1", "sub-0", other));

        assertThat(registry.hasSubscribers(DESTINATION)).isFalse();
        assertThat(registry.hasSubscribers(other)).isTrue();
    }

    @Test
    void countsEachDestinationSeparately() {
        registry.onApplicationEvent(subscribe("ws-1", "sub-0", DESTINATION));
        registry.onApplicationEvent(subscribe("ws-1", "sub-1", "/game/session-1/frameInfos/7"));

        registry.onApplicationEvent(unsubscribe("ws-1", "sub-1"));

        assertThat(registry.hasSubscribers(DESTINATION)).isTrue();
        assertThat(registry.hasSubscribers("/game/session-1/frameInfos/7")).isFalse();
    }

    @Test
    void ignoresAnUnsubscribeForASessionItNeverSaw() {
        registry.onApplicationEvent(unsubscribe("ws-unknown", "sub-0"));

        assertThat(registry.getSubscriberCount(DESTINATION)).isZero();
    }

    private SessionSubscribeEvent subscribe(String sessionId, String subscriptionId, String destination) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setSessionId(sessionId);
        accessor.setSubscriptionId(subscriptionId);
        accessor.setDestination(destination);
        return new SessionSubscribeEvent(this, message(accessor));
    }

    private SessionUnsubscribeEvent unsubscribe(String sessionId, String subscriptionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.UNSUBSCRIBE);
        accessor.setSessionId(sessionId);
        accessor.setSubscriptionId(subscriptionId);
        return new SessionUnsubscribeEvent(this, message(accessor));
    }

    private SessionDisconnectEvent disconnect(String sessionId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
        accessor.setSessionId(sessionId);
        return new SessionDisconnectEvent(this, message(accessor), sessionId, CloseStatus.NORMAL);
    }

    private Message<byte[]> message(StompHeaderAccessor accessor) {
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
