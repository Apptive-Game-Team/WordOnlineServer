package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;

// This interface is used to represent a component in the game
public abstract class Component {
    public final GameObject gameObject;

    public abstract void start();
    public abstract void update();
    public abstract void onDestroy();

    public Component(GameObject gameObject) {
        this.gameObject = gameObject;
    }
}
