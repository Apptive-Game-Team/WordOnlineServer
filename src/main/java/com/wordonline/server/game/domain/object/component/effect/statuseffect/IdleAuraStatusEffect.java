package com.wordonline.server.game.domain.object.component.effect.statuseffect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.dto.StatusChangeEvent;
import lombok.Getter;

import java.util.concurrent.Flow;
import java.util.concurrent.Flow.Subscription;
import java.util.stream.Stream;

public class IdleAuraStatusEffect extends BaseStatusEffect implements Flow.Subscriber<StatusChangeEvent> {
    private static final float ATTACK_AURA_DURATION = 0.3f;

    @Getter
    private final ElementType element;
    private Subscription subscription;
    private boolean expired;

    public IdleAuraStatusEffect(GameObject owner, ElementType element) {
        super(owner, Float.POSITIVE_INFINITY, StatusEffectKey.Default, toIdleAura(element));
        this.element = element;
        gameObject.subscribeStatusChange(this);
    }

    @Override
    public void update() {
    }

    @Override
    public void onAttacked(ElementType attackType) {
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        this.subscription = subscription;
        if (expired) {
            subscription.cancel();
            this.subscription = null;
            return;
        }
        subscription.request(1);
    }

    @Override
    public void onNext(StatusChangeEvent event) {
        subscription.request(1);
        if (event.current() == Status.Attack) {
            addAttackAura();
            expire();
            return;
        }

        if (event.current() == Status.Hindered) {
            expire();
        }
    }

    @Override
    public void onError(Throwable throwable) {
    }

    @Override
    public void onComplete() {
    }

    @Override
    public void onDestroy() {
        cancelSubscription();
        super.onDestroy();
    }

    @Override
    protected void expire() {
        expired = true;
        cancelSubscription();
        super.expire();
    }

    private void cancelSubscription() {
        if (subscription != null) {
            subscription.cancel();
            subscription = null;
        }
    }

    public void cancel() {
        expire();
    }

    private void addAttackAura() {
        if (findAttackAuras()
                .filter(effect -> effect.getElement() == element)
                .findAny()
                .isEmpty()) {
            gameObject.addComponent(new AttackAuraStatusEffect(gameObject, element, ATTACK_AURA_DURATION));
        }
    }

    private Stream<AttackAuraStatusEffect> findAttackAuras() {
        return Stream.concat(
                gameObject.getComponents(AttackAuraStatusEffect.class).stream(),
                gameObject.getComponentsToAdd().stream()
                        .filter(AttackAuraStatusEffect.class::isInstance)
                        .map(AttackAuraStatusEffect.class::cast)
        );
    }

    private static Effect toIdleAura(ElementType element) {
        return switch (element) {
            case FIRE -> Effect.FireIdleAura;
            case WATER -> Effect.WaterIdleAura;
            case NATURE -> Effect.NatureIdleAura;
            case LIGHTNING -> Effect.LightningIdleAura;
            case ROCK -> Effect.RockIdleAura;
            case WIND -> Effect.WindIdleAura;
            case NONE -> Effect.None;
        };
    }
}
