package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector2;
import lombok.Data;

import java.util.List;

@Data
public class InputRequestDto {
    private final String type = "useMagic";
    private final List<CardType> cards;
    private final int id;
    private final Vector2 position;
}
