package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.result.ResultDto;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.dto.result.ResultType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ResultChecker {
    private final SessionObject sessionObject;
    @Getter
    Master loser = null;

    public void setLoser(Master loser) {
        if (this.loser != null) {
            return;
        }
        this.loser = loser;
    }

    public boolean checkResult() {
        if (loser == null) {
            return false;
        }
        return true;
    }

    public void broadcastResult(ResultMmrDto mmrDto) {
        ResultDto resultDto = new ResultDto(
                (loser==Master.LeftPlayer?ResultType.Lose:ResultType.Win),
                (loser==Master.RightPlayer?ResultType.Lose:ResultType.Win),
                mmrDto
        );

        sessionObject.sendFrameInfo(
                sessionObject.getRightUserId(),
                resultDto
        );
        sessionObject.sendFrameInfo(
                sessionObject.getLeftUserId(),
                resultDto
        );

        log.info("[Game Result] leftUser: {} {}, rightUer: {} {}", sessionObject.getLeftUserId(), resultDto.getLeftPlayer(), sessionObject.getRightUserId(), resultDto.getRightPlayer());
    }
}
