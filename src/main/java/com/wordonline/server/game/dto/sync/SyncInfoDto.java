package com.wordonline.server.game.dto.sync;

import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncInfoDto {
    private final String type = "sync";
    private int updatedMana;
    private int leftPlayerHp;
    private int rightPlayerHp;
    private final SnapshotResponseDto snapshotResponseDto;

    public SyncInfoDto(FrameInfoDto frameInfoDto, SnapshotResponseDto snapshotResponseDto) {
        this(frameInfoDto.getUpdatedMana(), frameInfoDto.getLeftPlayerHp(), frameInfoDto.getRightPlayerHp(), snapshotResponseDto);
    }
}
