package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.frame.GaugeCategory;
import com.wordonline.server.game.dto.frame.GaugeDto;

public class TimedSelfDestroyer extends Component implements GaugeComponent {

    protected final float timeToLive;
    protected float elapsedTime;

    public TimedSelfDestroyer(GameObject gameObject, float timeToLive) {
        super(gameObject);
        this.timeToLive = timeToLive;
        this.elapsedTime = 0f;
    }

    @Override
    public void start() { }

    @Override
    public void update() {
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

        elapsedTime = Math.max(0f, elapsedTime - amount);
        gameObject.applyUpdate();
    }

    @Override
    public GaugeDto getGauge() {
        return new GaugeDto(timeToLive - elapsedTime, timeToLive, GaugeCategory.TTL);
    }
}
