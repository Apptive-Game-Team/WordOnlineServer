package com.wordonline.server.game.dto.input;

public record InputResponseDto(
    String type,
    String message,
    boolean valid,
    InputResultCode resultCode,
    int updatedMana,
    int id,
    long magicId
) {
    public InputResponseDto(
            boolean valid, InputResultCode resultCode, int updatedMana, int id, long magicId
    ) {
        this(
                (valid ? "magic use is valid" : "magic use is not valid"),
                valid,
                resultCode,
                updatedMana,
                id,
                magicId
        );
    }

    public InputResponseDto(
            String message, boolean valid, InputResultCode resultCode, int updatedMana, int id, long magicId
    ) {
        this("magicValid", message, valid, resultCode, updatedMana, id, magicId);
    }
}
