package com.wordonline.server.game.service.pve;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.pve.PveInstallObject;
import com.wordonline.server.game.service.GameContext;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Scope("prototype")
public class PveScenarioInstaller {

    @Getter
    public static class RuntimeState {
        private final String stageId;
        private final Map<String, Integer> installedObjectIds = new HashMap<>();

        private RuntimeState(String stageId) {
            this.stageId = stageId;
        }

        public int getInstalledObjectId(String installerId) {
            return installedObjectIds.getOrDefault(installerId, -1);
        }
    }

    @Getter
    private RuntimeState runtime;

    public void install(String stageId, List<PveInstallObject> installers, GameContext gameContext) {
        this.runtime = new RuntimeState(stageId);

        for (PveInstallObject installObject : installers) {
            GameObject gameObject = new GameObject(
                    installObject.master(),
                    installObject.prefabType(),
                    installObject.position(),
                    gameContext
            );

            runtime.getInstalledObjectIds().put(installObject.installerId(), gameObject.getId());
        }
    }

    public GameObject getInstalledObject(GameContext gameContext, String installerId) {
        if (runtime == null || installerId == null || installerId.isBlank()) {
            return null;
        }

        int objectId = runtime.getInstalledObjectId(installerId);
        if (objectId < 0) {
            return null;
        }

        for (GameObject gameObject : gameContext.getGameObjects()) {
            if (gameObject.getId() == objectId) {
                return gameObject;
            }
        }
        return null;
    }
}
