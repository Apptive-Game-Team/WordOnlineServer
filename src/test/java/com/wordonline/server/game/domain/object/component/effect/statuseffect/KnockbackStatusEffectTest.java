package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.Stat;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.service.GameContext;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class KnockbackStatusEffectTest {

    @Test
    void expirationKeepsSpeedModifierOwnedByAnotherStatusEffect() {
        GameObject owner = mock(GameObject.class);
        GameContext gameContext = mock(GameContext.class);
        Mob mob = mock(Mob.class);
        Stat speed = new Stat(10f);
        when(owner.getGameContext()).thenReturn(gameContext);
        when(owner.getComponent(Mob.class)).thenReturn(mob);
        when(mob.getSpeed()).thenReturn(speed);
        when(gameContext.getDeltaTime()).thenReturn(1f);

        SnaredStatusEffect snare = new SnaredStatusEffect(owner, 3f, 0, StatusEffectKey.Snared_Receive);
        snare.start();
        assertThat(speed.total()).isEqualTo(5f);

        KnockbackStatusEffect knockback =
                new KnockbackStatusEffect(owner, new Vector3(1f, 0f, 0f), 1f, StatusEffectKey.Knockback_Receive);
        knockback.start();
        knockback.update();
        knockback.update();

        verify(owner).removeComponent(knockback);
        assertThat(speed.total()).isEqualTo(5f);
    }
}
