package com.wordonline.server.game.dto.lockstep;

import com.wordonline.server.game.dto.input.InputRequestDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * Broadcast by the server once all player inputs for a frame arrive (or timeout).
 * Both clients execute the same simulation step with these confirmed inputs.
 */
@Getter
@AllArgsConstructor
public class ConfirmedFrameDto {
    private final String type = "confirmedFrame";
    private final int frameNum;
    private final Map<Long, InputRequestDto> inputs;
}
