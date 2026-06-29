package com.wordonline.server.session.service;

import com.wordonline.server.auth.repository.UserRepository;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.session.dto.SessionDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoBotMatchService {

    private final SessionService sessionService;
    private final UserRepository userRepository;
    private final AtomicBoolean creatingMatch = new AtomicBoolean(false);

    @Value("${wordonline.bot.auto-match.enabled:true}")
    private boolean enabled;

    @Scheduled(
            initialDelayString = "${wordonline.bot.auto-match.initial-delay-ms:10000}",
            fixedDelayString = "${wordonline.bot.auto-match.fixed-delay-ms:5000}"
    )
    public void createBotMatchWhenIdle() {
        if (!enabled || sessionService.getActiveSessions() > 0 || !creatingMatch.compareAndSet(false, true)) {
            return;
        }

        try {
            List<Long> botUserIds = userRepository.findBotUserIds();
            if (botUserIds.size() < 2) {
                log.debug("[AutoBotMatch] skipped; bot users with decks={}", botUserIds.size());
                return;
            }

            long matchSeed = ThreadLocalRandom.current().nextLong();
            Matchup matchup = selectMatchup(botUserIds, matchSeed);
            SessionDto sessionDto = new SessionDto(
                    "bot-auto-" + UUID.randomUUID(),
                    matchup.leftUserId(),
                    matchup.rightUserId(),
                    SessionType.PVP,
                    null
            );

            log.info("[AutoBotMatch] creating session={}, left={}({}), right={}({}), seed={}",
                    sessionDto.sessionId(),
                    "Bot",
                    matchup.leftUserId(),
                    "Bot",
                    matchup.rightUserId(),
                    matchSeed);
            sessionService.createSession(sessionDto);
        } catch (Exception e) {
            log.warn("[AutoBotMatch] failed to create bot match", e);
        } finally {
            creatingMatch.set(false);
        }
    }

    private Matchup selectMatchup(List<Long> botUserIds, long seed) {
        List<Long> shuffled = new ArrayList<>(botUserIds);
        Random random = new Random(seed);
        long left = shuffled.remove(random.nextInt(shuffled.size()));
        long right = shuffled.get(random.nextInt(shuffled.size()));
        return new Matchup(left, right);
    }

    private record Matchup(long leftUserId, long rightUserId) {
    }
}
