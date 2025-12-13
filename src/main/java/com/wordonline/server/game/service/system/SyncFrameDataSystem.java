package com.wordonline.server.game.service.system;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.dto.frame.SnapshotResponseDto;
import com.wordonline.server.game.service.GameContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class SyncFrameDataSystem extends FrameDataSystem {

    @Override
    public void earlyUpdate(GameContext gameContext) {
        super.earlyUpdate(gameContext);
    }

    @Override
    public void lateUpdate(GameContext gameContext) {
        if (gameContext.getFrameNum() % 10 != 0) {
            super.lateUpdate(gameContext);
        } else {
            SnapshotResponseDto leftSnapshotResponseDto = gameContext.getGameLoop().getLastSnapshot(gameContext.getSessionObject().getLeftUserId());
            SnapshotResponseDto rightSnapshotResponseDto = gameContext.getGameLoop().getLastSnapshot(gameContext.getSessionObject().getRightUserId());

            gameContext.getSessionObject().sendFrameInfo(
                    gameContext.getSessionObject().getLeftUserId(),
                    getLeftFrameInfoDto().toSyncDto(leftSnapshotResponseDto)
            );
            gameContext.getSessionObject().sendFrameInfo(
                    gameContext.getSessionObject().getRightUserId(),
                    getRightFrameInfoDto().toSyncDto(rightSnapshotResponseDto)
            );

            // Broadcast sync info to spectators (userId = 0)
            // Use left player's snapshot as the canonical state for spectators
            FrameInfoDto broadcastFrameInfoDto = FrameInfoDto.createBroadcastDto(
                    gameContext.getObjectsInfoDto(),
                    gameContext.getGameSessionData()
            );
            gameContext.getSessionObject().broadcastFrameInfo(
                    broadcastFrameInfoDto.toSyncDto(leftSnapshotResponseDto)
            );
        }
    }
}
