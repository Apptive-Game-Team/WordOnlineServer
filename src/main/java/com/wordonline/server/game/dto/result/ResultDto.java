package com.wordonline.server.game.dto.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResultDto {
    private final String type = "result";
    private final ResultType leftPlayer;
    private final ResultType rightPlayer;
    private final short lastLeftPlayerMmr;
    private final short lastRightPlayerMmr;
    private final short newLeftPlayerMmr;
    private final short newRightPlayerMmr;

    public ResultDto(
            ResultType leftPlayer, ResultType rightPlayer,
            ResultMmrDto resultMmrDto) {
        this(leftPlayer, rightPlayer, resultMmrDto.lastLeftMmr(), resultMmrDto.lastRightMmr(), resultMmrDto.newLeftMmr(), resultMmrDto.newRightMmr());

    }
}