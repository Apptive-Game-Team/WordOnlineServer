package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.dto.frame.FrameInfoDto;

// this class is responsible for charging mana for players
public class ManaCharger {
    public final static float MANA_CHARGE_INTERVAL = 0.2f;
    public final static int DEFAULT_MANA_CHARGE_VALUE = 1;
    public final static int MAX_MANA = 100;

    private final Stat manaChangeValue = new Stat(DEFAULT_MANA_CHARGE_VALUE);

    public void updateManaCharge(int deltaValue) {
        manaChangeValue.addPercent(deltaValue);
    }

    // this method is called every frame to charge mana
    public void chargeMana(PlayerData player, FrameInfoDto frameInfoDto, int frameNum) {
        if (frameNum % ((int) (GameLoop.FPS * MANA_CHARGE_INTERVAL)) == 0)
            player.mana = Math.min((int) manaChangeValue.total() + player.mana, MAX_MANA);

        frameInfoDto.setUpdatedMana(player.mana);
    }
}
