package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;

public class TimedSelfDestroyer extends Component {

    private final float timeToLive;
    private float elapsedTime;

    public TimedSelfDestroyer(GameObject gameObject, float timeToLive) {
        super(gameObject);
        this.timeToLive = timeToLive;
        this.elapsedTime = 0f;
    }

    @Override
    public void start() { }

    @Override
    public void update() {
        elapsedTime += gameObject.getGameLoop().deltaTime;
        if (elapsedTime >= timeToLive) {
            gameObject.destroy();
        }
    }

    @Override
    public void onDestroy() { }
}
