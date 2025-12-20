package com.wordonline.server.game.dto.input;

import com.wordonline.server.game.dto.Master;

public record InputHandleEvent(
        Master master,
        InputResultCode ResultCode,
        long magicId
) {

    public static InputHandleEvent fail(Master master, InputResultCode inputResultCode) {
        return new InputHandleEvent(master, inputResultCode, -1);
    }
}


