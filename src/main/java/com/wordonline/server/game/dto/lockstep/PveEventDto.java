package com.wordonline.server.game.dto.lockstep;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * One dialogue event that fires when the simulation reaches a given frame (PVE only).
 * The client checks these each confirmedFrame and shows speech bubbles on the speaker object.
 */
@Getter
@AllArgsConstructor
public class PveEventDto {
    /** Frame at which this event fires (PveTriggerType.FrameNumGte). */
    private final int frameNum;
    private final String speakerInstallerId;
    private final String key;
    private final List<String> lines;
}
