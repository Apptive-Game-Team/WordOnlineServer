package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class LightningCloud extends MagicComponent {

    private final float strikeInterval;
    private final int strikeCount;
    private float timer;
    private int strikes;

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

        spawnLightningStrike();
        strikes++;
        if (strikes >= strikeCount) {
            gameObject.destroy();
        }
    }
}
