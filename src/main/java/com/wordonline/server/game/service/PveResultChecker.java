package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.Master;

public class PveResultChecker extends ResultChecker {
    private boolean cleared = false;
    private boolean failed = false;

    public PveResultChecker(SessionObject sessionObject) {
        super(sessionObject);
    }

    public void setCleared() {
        cleared = true;
        setLoser(Master.RightPlayer);
    }

    public void setFailed() {
        failed = true;
        setLoser(Master.LeftPlayer);
    }

    @Override
    public boolean checkResult() {
        return cleared || failed;
    }
}
