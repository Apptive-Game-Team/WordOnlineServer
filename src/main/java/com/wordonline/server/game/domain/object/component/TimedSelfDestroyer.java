package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.frame.GaugeCategory;
import com.wordonline.server.game.dto.frame.GaugeDto;

public class TimedSelfDestroyer extends Component implements GaugeComponent {

    protected final float timeToLive;
    protected float elapsedTime;

    // recover(float) rewinds elapsedTime. Several sources (e.g. overlapping repair auras) can
    // each call it within the same frame; without a cap the total rewind would exceed the one
    // tick of decay update() just applied, so the object would live longer than its duration
    // instead of merely having its decay frozen. recoveredThisFrame tracks how much of that
    // one-tick budget has already been spent, and update() resets it for the next frame.
    protected float recoveredThisFrame;

    public TimedSelfDestroyer(GameObject gameObject, float timeToLive) {
        super(gameObject);
        this.timeToLive = timeToLive;
        this.elapsedTime = 0f;
        this.recoveredThisFrame = 0f;
    }

    @Override
    public void start() { }

    @Override
    public void update() {
        recoveredThisFrame = 0f;
        elapsedTime += getGameContext().getDeltaTime();
        if (elapsedTime >= timeToLive) {
            gameObject.destroy();
        }
    }

    @Override
    public void onDestroy() { }

    public void recover() {
        elapsedTime = 0f;
        gameObject.applyUpdate();
    }

    public void recover(float amount) {
        if (amount <= 0f) {
            return;
        }

        float budget = Math.max(0f, getGameContext().getDeltaTime() - recoveredThisFrame);
        float applied = Math.min(amount, budget);
        if (applied <= 0f) {
            return;
        }

        recoveredThisFrame += applied;
        elapsedTime = Math.max(0f, elapsedTime - applied);
        gameObject.applyUpdate();
    }

    @Override
    public GaugeDto getGauge() {
        return new GaugeDto(timeToLive - elapsedTime, timeToLive, GaugeCategory.TTL);
    }
}
