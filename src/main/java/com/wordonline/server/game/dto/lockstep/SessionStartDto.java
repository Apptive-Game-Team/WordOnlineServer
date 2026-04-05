package com.wordonline.server.game.dto.lockstep;

import com.wordonline.server.game.domain.magic.CardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Sent once at session creation. Both clients use the same seed and initial state
 * to produce identical deterministic simulations.
 */
@Getter
@AllArgsConstructor
public class SessionStartDto {
    private final String type = "sessionStart";
    private final int frameNum = 0;
    private final long rngSeed;
    private final long leftUserId;
    private final long rightUserId;
    private final List<CardType> leftCards;
    private final List<CardType> rightCards;
    /** Balance parameters: gameObject → { paramName → value } */
    private final Map<String, Map<String, Double>> parameters;
}
