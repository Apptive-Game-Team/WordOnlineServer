package com.wordonline.server.matching.component;

import com.wordonline.server.auth.dto.UserDetailResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.component.SessionManager;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.matching.dto.MatchedInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchingManager implements Flow.Subscriber<Long> {

    private final ReentrantLock lock = new ReentrantLock();
    private final Queue<Long> matchingQueue = new ConcurrentLinkedQueue<>();
    private static int sessionIdCounter = 0;

    private Subscription subscription;

    private final DeckService deckService;
    private final UserService userService;
    private final SimpMessagingTemplate template;
    private final SessionManager sessionManager;
    private final Parameters parameters;

    private final ScheduledExecutorService botFallbackScheduler = Executors.newSingleThreadScheduledExecutor();

    public int getQueueLength() {
        return matchingQueue.size();
    }

    public boolean isInQueue(long userId) {
        return matchingQueue.contains(userId);
    }

    public boolean enqueue(long userId) {
        subscribe();
        // 1) 상태 체크 & OnMatching 으로 전환

        if (isInQueue(userId)) {
            return true;
        }

        try {
            userService.markMatching(userId);
        } catch (IllegalStateException ex) {
            log.warn("매칭 불가 상태: {} -> {}", userId, ex.getMessage());
            userService.markOnline(userId);
            return false;
        }

        // 2) 덱 선택 여부 체크
        if (!deckService.hasSelectedDeck(userId)) {
            // 상태 롤백
            userService.markOnline(userId);
            return false;
        }

        // 3) 큐에 집어넣기
        matchingQueue.add(userId);

//        botFallbackScheduler.schedule(() -> {
//            lock.lock();
//            try {
//                // 아직 큐가 비어있지 않고, userId가 헤드일 때만 봇 전환
//                if (!matchingQueue.isEmpty()
//                        && Objects.equals(matchingQueue.peek(), userId)) {
//
//                    // 세션 한도도 간단 체크 (가득 차면 아무 것도 안 함)
//                    if (sessionManager.getActiveSessions() < parameters.getValue("game", "count")) {
//                        matchingQueue.poll(); // 큐에서 제거
//                    } else {
//                        return; // 한도 초과면 그냥 대기 계속
//                    }
//                } else {
//                    return; // 헤드가 아니면 스킵 (이미 처리됐거나 위치 바뀐 케이스)
//                }
//            } finally {
//                lock.unlock();
//            }
//
//            // 락 밖에서 실제 작업 (메시징/DB 부하 분리)
//            matchPractice(userId);
//
//        }, 15, TimeUnit.SECONDS);

        return true;
    }

    public void removeFromQueue(long id) {
        matchingQueue.remove(id);
    }

    public boolean tryMatchUsers() {
        Long uid1;
        Long uid2;

        lock.lock();
        try {
            if (matchingQueue.size() < 2) {
                return false;
            }

            if (sessionManager.getActiveSessions() >= parameters.getValue("game", "count")) {
                return false;
            }

            sessionIdCounter++;

            uid1 = matchingQueue.poll();
            uid2 = matchingQueue.poll();
            if (uid1 == null || uid2 == null) {
                return false;
            }
        } finally {
            lock.unlock();
        }


        UserDetailResponseDto user1 = userService.getUserDetail(uid1);
        UserDetailResponseDto user2 = userService.getUserDetail(uid2);

        try {
            userService.markPlaying(uid1);
            userService.markPlaying(uid2);
        } catch (IllegalStateException ex) {
            log.warn("매칭 시작 실패: 상태가 올바르지 않습니다. {}", ex.getMessage());
            userService.markOnline(uid1);
            userService.markOnline(uid2);
            return false;
        }

        String sessionId = "session-" + sessionIdCounter;
        MatchedInfoDto matchedInfoDto = new MatchedInfoDto(
                "Successfully Matched",
                user1,
                user2,
                sessionId
        );
        template.convertAndSend(
                String.format("/queue/match-status/%s", uid1),
                matchedInfoDto);
        template.convertAndSend(
                String.format("/queue/match-status/%s", uid2),
                matchedInfoDto);

        log.info("[Matched] Users matched; user1: {}, user2: {}", uid1, uid2);

        try {
            Thread.sleep(2000);
            sessionManager.createSession(
                    new SessionObject(
                            sessionId,
                            uid1, uid2,
                            template,
                            deckService.getSelectedCards(uid1),
                            deckService.getSelectedCards(uid2)));
            return true;
        } catch (InterruptedException e) {
            log.error("Error while creating session", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public boolean matchPractice(long id) {

        removeFromQueue(id);

        UserDetailResponseDto user = userService.getUserDetail(id);
        UserDetailResponseDto botDetail = new UserDetailResponseDto(-1, "bot", null);

        try {
            userService.markPlaying(id);
        } catch (IllegalStateException ex) {
            log.warn("매칭 시작 실패: 상태가 올바르지 않습니다. {}", ex.getMessage());
            userService.markOnline(id);
            return false;
        }

        String sessionId = "session-" + sessionIdCounter;
        MatchedInfoDto matchedInfoDto = new MatchedInfoDto(
                "Successfully Matched",
                user,
                botDetail,
                sessionId
        );
        template.convertAndSend(
                String.format("/queue/match-status/%s", id),
                matchedInfoDto);
        log.info("[Practice] Users matched");

        try {
            Thread.sleep(2000);
            sessionManager.createSession(
                    new SessionObject(
                            sessionId,
                            id, botDetail.id(),
                            template,
                            deckService.getSelectedCards(id),
                            deckService.getSelectedCards(id),
                            SessionType.Practice));
            return true;
        } catch (InterruptedException e) {
            log.error("Error while creating session", e);
            Thread.currentThread().interrupt();
            return false;
        }
    }

    public String getHealthLog() {
        return "Matching Queue: " + matchingQueue;
    }

    private void subscribe() {
        if (subscription == null) {
            sessionManager.numOfSessionsFlow.subscribe(this);
        }
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        subscription.request(1);
    }

    @Override
    public void onNext(Long item) {
        tryMatchUsers();
        subscription.request(1);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onComplete() {

    }
}
