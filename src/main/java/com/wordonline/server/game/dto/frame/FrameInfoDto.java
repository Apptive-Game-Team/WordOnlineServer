package com.wordonline.server.game.dto.frame;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.dto.CardInfoDto;
import lombok.Data;

@Data
// This class is used to send frame information to the client
public class FrameInfoDto {
    private final String type = "frame";
    private int updatedMana;
    private int leftPlayerHp;
    private int rightPlayerHp;
    private final CardInfoDto cards;
    private final ObjectsInfoDto objects;

    public FrameInfoDto(CardInfoDto cardInfoDto, ObjectsInfoDto objectsInfoDto, GameSessionData gameSessionData){
        cards = cardInfoDto;
        objects = objectsInfoDto;
        leftPlayerHp = gameSessionData.leftPlayerData.hp;
        rightPlayerHp = gameSessionData.rightPlayerData.hp;
    }

    public FrameInfoDto(int updatedMana, CardInfoDto cards, ObjectsInfoDto objects) {
        this.updatedMana = updatedMana;
        this.cards = cards;
        this.objects = objects;
    }
}


