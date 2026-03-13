package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.pve.PveInstallObject;
import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveScenarioEvent;
import com.wordonline.server.game.domain.pve.PveTriggerType;
import com.wordonline.server.game.dto.Master;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PveScenarioRegistry {

    private final Map<String, PveScenario> scenarios;

    public PveScenarioRegistry(DatabaseMagicParser magicParser) {
        this.scenarios = Map.of(
                "1-1",
                new PveScenario(
                        "1-1",
                        "enemy_boss_1",
                        List.of(
                                new PveInstallObject(
                                        "enemy_boss_1",
                                        PrefabType.PveNatureSlimeNest,
                                        Master.RightPlayer,
                                        new Vector3(14, 5, 0),
                                        magics(magicParser, "nature_slime_swarm"),
                                        2.5f
                                )
                        ),
                        List.of(
                                new PveScenarioEvent(
                                        "intro",
                                        PveTriggerType.FrameNumGte,
                                        10,
                                        "enemy_boss_1",
                                        "pve_1_1_intro",
                                        List.of("Stage 1-1", "Destroy the enemy nest!")
                                ),
                                new PveScenarioEvent(
                                        "enemyLine",
                                        PveTriggerType.FrameNumGte,
                                        20,
                                        "enemy_boss_1",
                                        "pve_1_1_enemy_line",
                                        List.of("Burn it all down!")
                                )
                        )
                )
        );
    }

    private List<Magic> magics(DatabaseMagicParser magicParser, String... magicNames) {
        return Arrays.stream(magicNames)
                .map(magicParser::parseMagicForBot)
                .filter(Objects::nonNull)
                .toList();
    }

    public PveScenario getScenario(String stageId) {
        PveScenario scenario = scenarios.get(stageId);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown stageId: " + stageId);
        }
        return scenario;
    }
}
