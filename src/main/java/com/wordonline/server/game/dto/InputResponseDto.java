package com.wordonline.server.game.dto;

public record InputResponseDto(
    String type,
    String message,
    boolean valid,
    int updatedMana,
    int id,
    long magicId
) {
    public InputResponseDto(
            boolean valid, int updatedMana, int id, long magicId
    ) {
        this(
                (valid ? "magic use is valid" : "magic use is not valid"),
                valid,
                updatedMana,
                id,
                magicId
        );
    }

    public InputResponseDto(
            String message, boolean valid, int updatedMana, int id, long magicId
    ) {
        this("magicValid", message, valid, updatedMana, id, magicId);
    }
}
