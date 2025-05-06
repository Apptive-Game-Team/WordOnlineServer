package com.wordonline.server.game.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
public class FrameInfoDto {
    private final String type = "frame";
    private int updatedMana;
    private final CardInfoDto cards;
    private final ObjectsInfoDto objects;

    public FrameInfoDto(CardInfoDto cardInfoDto, ObjectsInfoDto objectsInfoDto){
        cards = cardInfoDto;
        objects = objectsInfoDto;
    }

    public FrameInfoDto(int updatedMana, CardInfoDto cards, ObjectsInfoDto objects) {
        this.updatedMana = updatedMana;
        this.cards = cards;
        this.objects = objects;
    }
}


