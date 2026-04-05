package com.wordonline.server.game.dto.input;

public record InputResponseDto(
    String type,
    String message,
    boolean valid,
    int updatedMana,
    int id,
    long magicId,
    int frameNum
) {
    public InputResponseDto(
            boolean valid, int updatedMana, int id, long magicId, int frameNum
    ) {
        this(
                (valid ? "magic use is valid" : "magic use is not valid"),
                valid,
                updatedMana,
                id,
                magicId,
                frameNum
        );
    }

    public InputResponseDto(
            String message, boolean valid, int updatedMana, int id, long magicId, int frameNum
    ) {
        this("magicValid", message, valid, updatedMana, id, magicId, frameNum);
    }
}
