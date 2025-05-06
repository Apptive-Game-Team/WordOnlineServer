package com.wordonline.server.game.domain;

public interface Component {
    void start();
    void update();
    void onDestroy();
}
