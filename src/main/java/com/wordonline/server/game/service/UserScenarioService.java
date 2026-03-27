package com.wordonline.server.game.service;

import com.wordonline.server.game.repository.UserScenarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserScenarioService {

    private static final String FINISHED_STATE = "finished";

    private final UserScenarioRepository userScenarioRepository;

    public void markFinished(long userId, Long scenarioId) {
        if (userId < 0 || scenarioId == null) {
            return;
        }

        int updatedRows = userScenarioRepository.updateState(userId, scenarioId, FINISHED_STATE);
        if (updatedRows == 0) {
            log.warn("[UserScenario] No scenario row updated; userId: {}, scenarioId: {}", userId, scenarioId);
        }
    }
}
