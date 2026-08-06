package com.wordonline.server.game.domain.object.component.effect.receiver;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.BaseStatusEffect;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.OverchargeStatusEffect;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class LightningSummonEffectReceiverTest {

    private GameObject lightningSummon() {
        GameObject gameObject = new GameObject(Master.LeftPlayer, PrefabType.ElectricSlime, Vector3.ZERO,
                mock(GameContext.class));
        gameObject.setElement(ElementType.LIGHTNING);
        return gameObject;
    }

    @Test
    void shockAppliesOverchargeInsteadOfStun() {
        LightningSummonEffectReceiver receiver = new LightningSummonEffectReceiver(lightningSummon());

        receiver.onReceive(Effect.Shock);

        BaseStatusEffect shock = receiver.getEffectByKey(StatusEffectKey.Shock_Receive);
        BaseStatusEffect overcharge = receiver.getEffectByKey(StatusEffectKey.Overcharge_Receive);

        assertThat(shock).isNull();
        assertThat(overcharge).isInstanceOf(OverchargeStatusEffect.class);
    }

    @Test
    void restackingShockExtendsOverchargeDuration() {
        LightningSummonEffectReceiver receiver = new LightningSummonEffectReceiver(lightningSummon());

        receiver.onReceive(Effect.Shock);
        receiver.onReceive(Effect.Shock);

        BaseStatusEffect overcharge = receiver.getEffectByKey(StatusEffectKey.Overcharge_Receive);
        assertThat(overcharge.getRemaining()).isEqualTo(6f);
    }

    @Test
    void commonReceiverKeepsShockImmunityForLightningUnits() {
        CommonEffectReceiver receiver = new CommonEffectReceiver(lightningSummon());

        receiver.onReceive(Effect.Shock);

        BaseStatusEffect shock = receiver.getEffectByKey(StatusEffectKey.Shock_Receive);
        BaseStatusEffect overcharge = receiver.getEffectByKey(StatusEffectKey.Overcharge_Receive);

        assertThat(shock).isNull();
        assertThat(overcharge).isNull();
    }
}
