package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Status;

public class LightningCloud extends MagicComponent {

    // how long the client takes to grow the bolt out of the cloud on one strike
    private static final float STRIKE_VISUAL_DURATION = 0.3f;

    private final float strikeInterval;
    private final int strikeCount;
    private float timer;
    private int strikes;
    private float lastStrikeVisualTimer;

    public LightningCloud(GameObject gameObject, float strikeInterval, int strikeCount) {
        super(gameObject);
        this.strikeInterval = strikeInterval;
        this.strikeCount = strikeCount;
    }

    @Override
    public void start() {
        if (strikeCount <= 0) {
            gameObject.destroy();
            return;
        }
        strike();
    }

    @Override
    public void update() {
        if (strikes >= strikeCount) {
            // the last bolt is still growing on the client, so the cloud outlives its last strike
            lastStrikeVisualTimer += getGameContext().getDeltaTime();
            if (lastStrikeVisualTimer >= STRIKE_VISUAL_DURATION) {
                gameObject.destroy();
            }
            return;
        }

        timer += getGameContext().getDeltaTime();
        while (strikes < strikeCount && timer >= strikeInterval) {
            timer -= strikeInterval;
            strike();
        }
    }

    protected void spawnLightningStrike() {
        new GameObject(
                gameObject.getMaster(),
                PrefabType.LightningDrop,
                new Vector3(gameObject.getPosition()),
                getGameContext());
    }

    private void strike() {
        if (strikes >= strikeCount) {
            return;
        }

        // the client draws the bolt on the cloud itself, growing out of its underside
        gameObject.setStatus(Status.Attack);
        spawnLightningStrike();
        strikes++;
    }
}
