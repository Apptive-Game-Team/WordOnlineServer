package com.wordonline.server.game.domain.object.component.effect.receiver;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.effect.EffectApplyPolicy;
import com.wordonline.server.game.domain.object.component.effect.EffectImmuneChart;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.*;
import com.wordonline.server.game.dto.Effect;
import java.util.function.Supplier;

public class CommonEffectReceiver extends Component implements EffectReceiver {

    private static final float OVERCHARGE_DURATION = 3f;

    public <T extends BaseStatusEffect> T getEffectByKey(StatusEffectKey key) {
        for (Component c : gameObject.getComponents())
            if (c instanceof BaseStatusEffect se && key.equals(se.getKey())) return (T) se;
        for (Component c : gameObject.getComponentsToAdd())
            if (c instanceof BaseStatusEffect se && key.equals(se.getKey())) return (T) se;
        return null;
    }

    public <T extends BaseStatusEffect> void applyEffect(
            StatusEffectKey key,
            Supplier<T> factory, // Supplier<T> : () -> 형식의 팩토리
            EffectApplyPolicy policy,
            float duration // 재적용 시 갱신
    ) {
        T existing = getEffectByKey(key);
        if (existing != null) {
            switch (policy) {
                case IGNORE -> {}
                case REFRESH_DURATION -> existing.refresh(duration);
                case EXTEND_DURATION -> existing.extend(duration); // 2단 상태이상 구현 x
            }
            return;
        }
        T created = factory.get();
        gameObject.addComponent(created);
    }

    @Override
    public void onReceive(Effect effect) {
        if (effect == Effect.Shock && gameObject.getElement().has(ElementType.LIGHTNING)) {
            applyEffect(
                    StatusEffectKey.Overcharge_Receive,
                    () -> new OverchargeStatusEffect(gameObject, OVERCHARGE_DURATION, StatusEffectKey.Overcharge_Receive),
                    EffectApplyPolicy.EXTEND_DURATION,
                    OVERCHARGE_DURATION);
            return;
        }

        if(EffectImmuneChart.isImmuneTo(gameObject, effect)) return;

        switch (effect) {
            case Wet -> {
                applyEffect(
                    StatusEffectKey.Wet_Receive,
                    () -> new WetStatusEffect(gameObject, 3f, StatusEffectKey.Wet_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
                if(gameObject.getElement().has(ElementType.NATURE)){
                    applyEffect(
                            StatusEffectKey.DOTHeal_NatureWithWaterField,
                            () -> new DOTStatusEffect(gameObject, 3f, -3, ElementType.NONE, StatusEffectKey.DOTHeal_NatureWithWaterField),
                            EffectApplyPolicy.REFRESH_DURATION,
                            3f);
                }
            }
            case Burn -> {
                applyEffect(
                    StatusEffectKey.Burn_Receive,
                    () -> new BurnStatusEffect(gameObject, 3f, StatusEffectKey.Burn_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
                applyEffect(
                    StatusEffectKey.DOTDeal_Burn,
                    () -> new DOTStatusEffect(gameObject, 3f, 3, ElementType.FIRE, StatusEffectKey.DOTDeal_Burn),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
            }
            case Shock -> applyEffect(
                    StatusEffectKey.Shock_Receive,
                    () -> new ShockStatusEffect(gameObject, 0.5f, StatusEffectKey.Shock_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    3f);
            case Snared -> applyEffect(
                        StatusEffectKey.Snared_Receive,
                        () -> new SnaredStatusEffect(gameObject, 3f, 5, StatusEffectKey.Snared_Receive),
                        EffectApplyPolicy.REFRESH_DURATION,
                        3f);

            case LeafFieldHeal -> applyEffect(
                        StatusEffectKey.DOTHeal_NatureField,
                        () -> new DOTStatusEffect(gameObject, 3f, -1, ElementType.NONE, StatusEffectKey.DOTHeal_NatureField),
                        EffectApplyPolicy.REFRESH_DURATION,
                        3f);
            case Sandstorm -> applyEffect(
                        StatusEffectKey.DOT_SandStorm,
                        () -> new DOTStatusEffect(gameObject, 0.5f, 1, ElementType.NONE, StatusEffectKey.DOT_SandStorm),
                        EffectApplyPolicy.REFRESH_DURATION,
                        0.5f);
            case Frenzy -> applyEffect(
                        StatusEffectKey.Frenzy_Receive,
                        () -> new FrenzyStatusEffect(gameObject, 10f, StatusEffectKey.Frenzy_Receive),
                        EffectApplyPolicy.REFRESH_DURATION,
                        10f);
            case Bubble -> applyEffect(
                    StatusEffectKey.Bubble_Receive,
                    () -> new BubbleStatusEffect(gameObject, 8f, StatusEffectKey.Bubble_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    8f);
            case Inspired -> applyEffect(
                    StatusEffectKey.Inspired_Receive,
                    () -> new InspiredStatusEffect(gameObject, 10f, StatusEffectKey.Inspired_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    10f);

        }
    }

    @Override
    public void onReceive(Effect effect, float duration) {
        if (effect == Effect.Frenzy) {
            applyEffect(
                    StatusEffectKey.Frenzy_Receive,
                    () -> new FrenzyStatusEffect(gameObject, duration, StatusEffectKey.Frenzy_Receive),
                    EffectApplyPolicy.REFRESH_DURATION,
                    duration);
            return;
        }

        if (effect != Effect.Inspired) {
            onReceive(effect);
            return;
        }

        applyEffect(
                StatusEffectKey.Inspired_Receive,
                () -> new InspiredStatusEffect(gameObject, duration, StatusEffectKey.Inspired_Receive),
                EffectApplyPolicy.REFRESH_DURATION,
                duration);
    }

    @Override
    public void onReceive(Effect effect, Vector3 direction, float prox) {
        applyEffect(
                StatusEffectKey.Knockback_Receive,
                () -> new KnockbackStatusEffect(gameObject, direction, prox, StatusEffectKey.Knockback_Receive),
                EffectApplyPolicy.IGNORE,
                3f);
    }

    @Override
    public void start() { }

    @Override
    public void update() { }

    @Override
    public void onDestroy() { }

    public CommonEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }
}
