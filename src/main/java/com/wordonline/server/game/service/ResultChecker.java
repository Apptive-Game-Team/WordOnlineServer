package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.result.ResultDto;
import com.wordonline.server.game.dto.result.ResultMmrDto;
import com.wordonline.server.game.dto.result.ResultType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ResultChecker {
    private final SessionObject sessionObject;
    @Getter
    private Master loser = null;
    @Setter
    private boolean isEnd = false;

    public void setEnd() {
        isEnd = true;
    }

    public void setLoser(Master loser) {
        isEnd = true;
        if (this.loser != null) {
            return;
        }
        this.loser = loser;
    }

    public boolean checkResult() {
        return isEnd;
    }

    public void broadcastResult(ResultMmrDto mmrDto) {
        ResultDto resultDto;

        if (loser == null) {
            resultDto = new ResultDto(
                    ResultType.Draw,
                    ResultType.Draw,
                    mmrDto
            );
        } else {
            resultDto = new ResultDto(
                    (loser==Master.LeftPlayer?ResultType.Lose:ResultType.Win),
                    (loser==Master.RightPlayer?ResultType.Lose:ResultType.Win),
                    mmrDto
            );
        }

        sessionObject.sendFrameInfo(
                sessionObject.getRightUserId(),
                resultDto
        );
        sessionObject.sendFrameInfo(
                sessionObject.getLeftUserId(),
                resultDto
        );

        log.info("[Game Result] leftUser: {} {}, rightUser: {} {}", sessionObject.getLeftUserId(), resultDto.getLeftPlayer(), sessionObject.getRightUserId(), resultDto.getRightPlayer());
    }
}
