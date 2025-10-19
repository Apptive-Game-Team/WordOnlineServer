package com.wordonline.server.game.service.system;

import com.wordonline.server.game.service.GameContext;

// update 전에 처리
public interface EarlyUpdateSystem {
    void earlyUpdate(GameContext gameContext);
}
