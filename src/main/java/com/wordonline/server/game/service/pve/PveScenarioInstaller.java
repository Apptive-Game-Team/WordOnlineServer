package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.pve.PveScenario;
import com.wordonline.server.game.domain.pve.PveWave;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
public class PveScenarioInstaller {

    @Getter
    public static class RuntimeState {
        private final String stageId;
        private final List<PrefabType> enemyPrefabTypes;
        private int waveIndex = -1;
        private int nextWaveFrame = 0;
        private boolean allWavesSpawned = false;

        private RuntimeState(String stageId, List<PrefabType> enemyPrefabTypes) {
            this.stageId = stageId;
            this.enemyPrefabTypes = enemyPrefabTypes;
        }

        public int getWaveIndex() {
            return waveIndex;
        }

        public boolean isAllWavesSpawned() {
            return allWavesSpawned;
        }
    }

    private final PveScenarioRegistry registry;

    @Getter
    private PveScenario scenario;

    @Getter
    private RuntimeState runtime;

    public PveScenarioInstaller(PveScenarioRegistry registry) {
        this.registry = registry;
    }

    public void install(String stageId, GameContext gameContext) {
        this.scenario = registry.getScenario(stageId);
        this.runtime = new RuntimeState(stageId, scenario.enemyPrefabTypes());
        runtime.waveIndex = -1;
        runtime.nextWaveFrame = gameContext.getFrameNum();
        runtime.allWavesSpawned = false;
    }

    public void update(GameContext gameContext) {
        if (scenario == null || runtime == null || runtime.allWavesSpawned) {
            return;
        }

        int frame = gameContext.getFrameNum();
        if (frame < runtime.nextWaveFrame) {
            return;
        }

        int nextIndex = runtime.waveIndex + 1;
        if (nextIndex >= scenario.waves().size()) {
            runtime.allWavesSpawned = true;
            return;
        }

        runtime.waveIndex = nextIndex;
        PveWave wave = scenario.waves().get(runtime.waveIndex);
        spawnWave(wave, gameContext);

        runtime.nextWaveFrame = frame + Math.max(0, wave.startDelayFrames());
        if (runtime.waveIndex >= scenario.waves().size() - 1) {
            // next tick will mark allWavesSpawned once frame >= nextWaveFrame
        }
    }

    private void spawnWave(PveWave wave, GameContext gameContext) {
        List<PrefabType> prefabs = new ArrayList<>();
        for (PveWave.PveSpawn spawn : wave.spawns()) {
            for (int i = 0; i < spawn.count(); i++) {
                prefabs.add(spawn.prefabType());
            }
        }

        float startY = GameConfig.RIGHT_PLAYER_POSITION.getY() - 2.0f;
        for (int i = 0; i < prefabs.size(); i++) {
            Vector3 pos = new Vector3(
                    GameConfig.RIGHT_PLAYER_POSITION.getX() - 1.5f,
                    startY + (i * 1.0f),
                    0
            );
            new GameObject(Master.RightPlayer, prefabs.get(i), pos, gameContext);
        }
    }
}
