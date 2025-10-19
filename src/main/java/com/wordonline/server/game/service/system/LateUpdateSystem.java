package com.wordonline.server.game.service.system;

import com.wordonline.server.game.service.GameContext;

// update 후에 처리
public interface LateUpdateSystem {
    void lateUpdate(GameContext gameContext);
}
