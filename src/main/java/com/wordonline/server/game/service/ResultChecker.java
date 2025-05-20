package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.result.ResultDto;
import com.wordonline.server.game.dto.result.ResultType;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class ResultChecker {
    private final SessionObject sessionObject;
    @Setter
    private ResultType leftPlayerResult = null, rightPlayerResult = null;

    public boolean checkResult() {
        if (leftPlayerResult == null || rightPlayerResult == null) {
            return false;
        }
        ResultDto resultDto = new ResultDto(leftPlayerResult, rightPlayerResult);

        sessionObject.sendFrameInfo(
                sessionObject.getRightUserId(),
                resultDto
        );
        sessionObject.sendFrameInfo(
                sessionObject.getLeftUserId(),
                resultDto
        );
        return true;
    }

}
