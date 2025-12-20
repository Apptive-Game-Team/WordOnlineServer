package com.wordonline.server.game.dto.frame;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.dto.CardInfoDto;
import com.wordonline.server.game.dto.sync.SyncInfoDto;

import lombok.Data;

@Data
// This class is used to send frame information to the client
public class FrameInfoDto {
    private final String type = "frame";
    private int remainingTime;
    private int updatedMana;
    private int leftPlayerHp;
    private int rightPlayerHp;
    private final CardInfoDto cards;
    private final ObjectsInfoDto objects;

    public FrameInfoDto(long remainingTime, CardInfoDto cardInfoDto, ObjectsInfoDto objectsInfoDto, GameSessionData gameSessionData){
        this.remainingTime = (int) remainingTime;
        cards = cardInfoDto;
        objects = objectsInfoDto;
        leftPlayerHp = gameSessionData.leftPlayerData.hp;
        rightPlayerHp = gameSessionData.rightPlayerData.hp;
    }

    // Constructor for broadcast (spectator) - excludes player-specific card info
    public static FrameInfoDto createBroadcastDto(long remainingTime, ObjectsInfoDto objectsInfoDto, GameSessionData gameSessionData){
        FrameInfoDto dto = new FrameInfoDto(remainingTime, new CardInfoDto(), objectsInfoDto, gameSessionData);
        dto.setUpdatedMana(0); // Spectators don't need mana info
        return dto;
    }

    public SyncInfoDto toSyncDto(SnapshotResponseDto snapshotResponseDto) {
        return new SyncInfoDto(this, snapshotResponseDto);
    }
}


