package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.repository.PveScenarioRepository;
import org.springframework.stereotype.Component;

@Component
public class PveScenarioRegistry {

    private final PveScenarioRepository pveScenarioRepository;

    public PveScenarioRegistry(PveScenarioRepository pveScenarioRepository) {
        this.pveScenarioRepository = pveScenarioRepository;
    }

    public PveScenario getScenario(Long scenarioId) {
        return pveScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown scenarioId: " + scenarioId));
    }
}
