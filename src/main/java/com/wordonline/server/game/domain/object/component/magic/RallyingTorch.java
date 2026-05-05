package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.effect.StatusEffectKey;
import com.wordonline.server.game.domain.object.component.effect.statuseffect.RallyingTorchStatusEffect;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.simple.Cannon;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.mob.simple.ManaWellMob;
import com.wordonline.server.game.domain.object.component.mob.simple.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.mob.simple.Totem;
import com.wordonline.server.game.domain.object.component.mob.simple.Turret;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;

public class RallyingTorch extends MagicComponent {

    private final float interval;
    private final float range;
    private final float buffDuration;
    private final float buffPercent;
    private float counter;

    public RallyingTorch(GameObject gameObject, float interval, float range, float buffDuration, float buffPercent) {
        super(gameObject);
        this.interval = interval;
        this.range = range;
        this.buffDuration = buffDuration;
        this.buffPercent = buffPercent;
    }

    @Override
    public void start() {
        gameObject.drawCircle(Vector3.ZERO, range, GizmoCategory.AreaOfEffect);
    }

    @Override
    public void update() {
        counter += getGameContext().getDeltaTime();
        if (counter < interval) {
            return;
        }

        counter = 0f;
        gameObject.setStatus(Status.Attack);
        getGameContext().overlapSphereAll(gameObject, range).stream()
                .filter(this::canRally)
                .forEach(this::applyRally);
    }

    @Override
    public void onDestroy() {
    }

    private boolean canRally(GameObject target) {
        if (target == gameObject) return false;
        if (gameObject.getMaster() == Master.None || target.getMaster() != gameObject.getMaster()) return false;
        if (!target.hasComponent(Mob.class)) return false;

        return !target.hasComponent(PlayerHealthComponent.class)
                && !target.hasComponent(Cannon.class)
                && !target.hasComponent(DummyMob.class)
                && !target.hasComponent(ManaWellMob.class)
                && !target.hasComponent(Totem.class)
                && !target.hasComponent(Turret.class);
    }

    private void applyRally(GameObject target) {
        RallyingTorchStatusEffect existing = target.getComponent(RallyingTorchStatusEffect.class);
        if (existing == null) {
            existing = target.getComponentsToAdd().stream()
                    .filter(RallyingTorchStatusEffect.class::isInstance)
                    .map(RallyingTorchStatusEffect.class::cast)
                    .findFirst()
                    .orElse(null);
        }

        if (existing != null) {
            existing.refresh(buffDuration);
            return;
        }

        target.addComponent(new RallyingTorchStatusEffect(
                target,
                buffDuration,
                buffPercent,
                StatusEffectKey.RallyingTorch_Receive));
    }
}
