package com.wordonline.server.game.domain;

// This interface is used to represent a component in the game
public interface Component {
    void start();
    void update();
    void onDestroy();
}
