package com.wordonline.server.game.service;

import jakarta.annotation.PostConstruct;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.dto.frame.FrameInfoDto;

import lombok.RequiredArgsConstructor;

// this class is responsible for charging mana for players
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ManaCharger {
    public final static float MANA_CHARGE_INTERVAL = 0.2f;
    public final static int DEFAULT_MANA_CHARGE_VALUE = 1;
    public static int MAX_MANA;

    private final Parameters parameters;

    private final Stat manaChangeValue = new Stat(DEFAULT_MANA_CHARGE_VALUE);

    @PostConstruct
    public void initMaxMana() {
        MAX_MANA = (int) parameters.getValue("player", "max_mana");
    }

    public void updateManaCharge(float deltaValue) {
        manaChangeValue.addPercent(deltaValue);
    }

    // this method is called every frame to charge mana
    public void chargeMana(PlayerData player, FrameInfoDto frameInfoDto, int frameNum) {
        if (frameNum % ((int) (GameLoop.FPS * MANA_CHARGE_INTERVAL)) == 0)
            player.mana = Math.min((int) manaChangeValue.total() + player.mana, MAX_MANA);

        frameInfoDto.setUpdatedMana(player.mana);
    }
}
