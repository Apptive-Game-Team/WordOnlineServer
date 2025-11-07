package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InputRequestDto {
    private String type;
    private List<CardType> cards;
    private int id;
    private Vector3 position;
}
