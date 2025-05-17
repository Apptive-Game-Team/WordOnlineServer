package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.service.GameLoop;


public class DummyComponent extends Component {

    public DummyComponent(GameObject gameObject) {
        super(gameObject);
    }

    private float speed = 1;

    @Override
    public void start() {
        // Initialization logic for the dummy component
    }

    @Override
    public void update() {
        Vector2 position = gameObject.getPosition();
        position.setX(position.getX() + speed / GameLoop.FPS);
        position.setY(position.getY() + speed / GameLoop.FPS);
        gameObject.setPosition(position);
    }

    @Override
    public void onDestroy() {
        // Cleanup logic for the dummy component
    }
}
