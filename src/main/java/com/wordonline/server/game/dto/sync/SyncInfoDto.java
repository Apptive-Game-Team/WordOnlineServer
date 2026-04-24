package com.wordonline.server.game.dto.sync;

import java.util.List;

import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import com.wordonline.server.game.dto.frame.projectile.ProjectileDto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SyncInfoDto {
    private final String type = "sync";
    private int remainingTime;
    private int updatedMana;
    private int leftPlayerHp;
    private int rightPlayerHp;
    private final SnapshotResponseDto snapshotResponseDto;
    private final List<ProjectileDto> projectileDtos;

    public SyncInfoDto(FrameInfoDto frameInfoDto, SnapshotResponseDto snapshotResponseDto) {
        this(frameInfoDto.getRemainingTime(), frameInfoDto.getUpdatedMana(), frameInfoDto.getLeftPlayerHp(), frameInfoDto.getRightPlayerHp(), snapshotResponseDto, frameInfoDto.getObjects()
                .getProjectile());
    }
}
