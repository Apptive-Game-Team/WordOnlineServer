package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.dto.FrameInfoDto;

public class ManaCharger {
    public final static float MANA_CHARGE_INTERVAL = 1;
    public final static int MANA_CHARGE_VALUE = 10;
    public final static int MAX_MANA = 100;

    public void chargeMana(PlayerData player, FrameInfoDto frameInfoDto, int frameNum) {
        if (frameNum % (GameLoop.FPS * MANA_CHARGE_INTERVAL) == 0)
            player.mana = Math.min(MANA_CHARGE_VALUE + player.mana, MAX_MANA);

        frameInfoDto.setUpdatedMana(player.mana);
    }
}
