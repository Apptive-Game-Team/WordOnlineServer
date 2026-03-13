package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.pve.PveInstallObject;
import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveScenarioEvent;
import com.wordonline.server.game.domain.pve.PveTriggerType;
import com.wordonline.server.game.dto.Master;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class PveScenarioRegistry {

    private final Map<String, PveScenario> scenarios = Map.of(
            "1-1",
            new PveScenario(
                    "1-1",
                    List.of("enemy_boss_1"),
                    List.of(
                            new PveInstallObject(
                                    "enemy_boss_1",
                                    PrefabType.PveNatureSlimeNest,
                                    Master.RightPlayer,
                                    new Vector3(14, 5, 0)
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
            ),
            "1-2",
            new PveScenario(
                    "1-2",
                    List.of("nature_nest", "water_nest"),
                    List.of(
                            new PveInstallObject(
                                    "nature_nest",
                                    PrefabType.PveNatureSlimeNest,
                                    Master.RightPlayer,
                                    new Vector3(14, 7, 0)
                            ),
                            new PveInstallObject(
                                    "water_nest",
                                    PrefabType.PveWaterSlimeNest,
                                    Master.RightPlayer,
                                    new Vector3(14, 3, 0)
                            )
                    ),
                    List.of(
                            new PveScenarioEvent(
                                    "intro",
                                    PveTriggerType.FrameNumGte,
                                    10,
                                    "water_nest",
                                    "pve_1_2_intro",
                                    List.of("Stage 1-2", "Destroy the enemy nest!")
                            ),
                            new PveScenarioEvent(
                                    "enemyLine",
                                    PveTriggerType.FrameNumGte,
                                    20,
                                    "water_nest",
                                    "pve_1_2_enemy_line",
                                    List.of("Burn it all down!")
                            )
                    )
            ),
            "1-3",
            new PveScenario(
                    "1-3",
                    List.of("pve_vine_colony"),
                    List.of(
                            new PveInstallObject(
                                    "pve_vine_colony",
                                    PrefabType.PveVineColony,
                                    Master.RightPlayer,
                                    new Vector3(14, 5, 0)
                            )
                    ),
                    List.of(
                            new PveScenarioEvent(
                                    "intro",
                                    PveTriggerType.FrameNumGte,
                                    10,
                                    "pve_vine_colony",
                                    "pve_1_3_intro",
                                    List.of("Stage 1-2", "Destroy the enemy nest!")
                            ),
                            new PveScenarioEvent(
                                    "enemyLine",
                                    PveTriggerType.FrameNumGte,
                                    20,
                                    "pve_vine_colony",
                                    "pve_1_3_enemy_line",
                                    List.of("Burn it all down!")
                            )
                    )
            )
    );

    public PveScenario getScenario(String stageId) {
        PveScenario scenario = scenarios.get(stageId);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown stageId: " + stageId);
        }
        return scenario;
    }
}
