package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.dto.frame.FrameInfoDto;

public class BotEye {
    public final GameSessionData data;
    public final FrameInfoDto myFrame;

    public BotEye(GameSessionData data,  FrameInfoDto myFrame) {
        this.data = data;
        this.myFrame = myFrame;
    }
}
