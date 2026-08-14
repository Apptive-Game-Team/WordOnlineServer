package com.wordonline.server.bot.service;

import com.wordonline.server.bot.config.BotAutoMatchProperties;
import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.server.entity.ServerState;
import com.wordonline.server.server.service.ServerStatusService;
import com.wordonline.server.session.dto.SessionDto;
import com.wordonline.server.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotGameScheduler {

    private static final String SESSION_PREFIX = "bot-auto-";

    private final SessionService sessionService;
    private final BotPersonaService botPersonaService;
    private final ServerStatusService serverStatusService;
    private final BotAutoMatchProperties botAutoMatchProperties;

    @Scheduled(fixedDelayString = "${bot.auto-match.check-interval-ms:60000}")
    public void ensureBotGameWhenIdle() {
        if (!botAutoMatchProperties.enabled() || serverStatusService.getCurrentState() != ServerState.ACTIVE) {
            return;
        }

        long missingGames = botAutoMatchProperties.targetGames() - sessionService.getActiveSessions();
        if (missingGames <= 0) {
            return;
        }

        List<BotPersona> bots = botPersonaService.findEnabled();
        if (bots.size() < 2) {
            log.warn("[BotGameScheduler] Need at least two enabled bot personas. enabledCount={}", bots.size());
            return;
        }

        for (long i = 0; i < missingGames; i++) {
            createBotGame(bots);
        }
    }

    private void createBotGame(List<BotPersona> bots) {
        List<BotPersona> shuffledBots = new ArrayList<>(bots);
        Collections.shuffle(shuffledBots);

        BotPersona leftBot = shuffledBots.get(0);
        BotPersona rightBot = shuffledBots.get(1);
        SessionDto sessionDto = new SessionDto(
                SESSION_PREFIX + UUID.randomUUID(),
                leftBot.userId(),
                rightBot.userId(),
                SessionType.Practice,
                null
        );

        sessionService.createSession(sessionDto);
        log.info("[BotGameScheduler] Auto bot game created. sessionId={}, leftBot={}, rightBot={}",
                sessionDto.sessionId(), leftBot.name(), rightBot.name());
    }
}
