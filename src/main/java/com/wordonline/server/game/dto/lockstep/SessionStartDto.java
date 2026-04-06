package com.wordonline.server.game.dto.lockstep;

import com.wordonline.server.game.domain.magic.CardType;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * Sent once at session creation. Both clients use the same seed and initial state
 * to produce identical deterministic simulations.
 *
 * sessionType: "PVP" | "PVE" | "PRACTICE"
 * initialObjects: non-null only for PVE — objects to spawn at sim start
 * scenarioEvents: non-null only for PVE — frame-triggered dialogue events
 */
@Getter
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
    private final String sessionType;
    private final List<InitialObjectDto> initialObjects;
    private final List<PveEventDto> scenarioEvents;

    public SessionStartDto(long rngSeed,
                           long leftUserId,
                           long rightUserId,
                           List<CardType> leftCards,
                           List<CardType> rightCards,
                           Map<String, Map<String, Double>> parameters,
                           String sessionType,
                           List<InitialObjectDto> initialObjects,
                           List<PveEventDto> scenarioEvents) {
        this.rngSeed = rngSeed;
        this.leftUserId = leftUserId;
        this.rightUserId = rightUserId;
        this.leftCards = leftCards;
        this.rightCards = rightCards;
        this.parameters = parameters;
        this.sessionType = sessionType;
        this.initialObjects = initialObjects;
        this.scenarioEvents = scenarioEvents;
    }
}
