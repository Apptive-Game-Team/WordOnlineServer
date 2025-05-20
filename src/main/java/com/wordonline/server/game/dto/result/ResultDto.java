package com.wordonline.server.game.dto.result;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ResultDto {
    private final String type = "result";
    private final ResultType leftPlayer;
    private final ResultType rightPlayer;
}