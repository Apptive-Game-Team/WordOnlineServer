package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.service.GameContext;

import lombok.Getter;

// This interface is used to represent a component in the game
public abstract class Component {
    @Getter
    public final GameObject gameObject;

    public GameContext getGameContext() {
        return gameObject.getGameContext();
    }

    // components of a dying game object are frozen unless they opt in here
    public boolean isActiveWhileDying() {
        return false;
    }

    public abstract void start();
    public abstract void update();
    public abstract void onDestroy();

    public Component(GameObject gameObject) {
        this.gameObject = gameObject;
    }
}
