package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class LightningStrike extends MagicComponent {

    private final AttackInfo attackInfo;
    private final Vector3 boxSize;

    public LightningStrike(GameObject gameObject, int damage, float radius) {
        super(gameObject);
        this.attackInfo = new AttackInfo(damage, gameObject.getElement().total());
        this.boxSize = new Vector3(radius * 2, radius * 2, GameConfig.DROP_MAGIC_INITIAL_HEIGHT);
    }

    @Override
    public void start() {
        gameObject.drawBox(new Vector3(0, 0, boxSize.getZ() / 2), boxSize, GizmoCategory.AreaOfEffect);
        Vector3 position = gameObject.getPosition();
        Vector3 boxCenter = new Vector3(
                position.getX(),
                position.getY(),
                boxSize.getZ() / 2
        );

        List<GameObject> targets = getGameContext().getPhysics().overlapBoxAll(boxCenter, boxSize);
        Master owner = gameObject.getMaster();

        for (GameObject target : targets) {
            if (target == gameObject || target.isDestroyed()) continue;
            if (owner != Master.None && target.getMaster() == owner) continue;

            List<Damageable> damageables = target.getComponents(Damageable.class);
            if (damageables.isEmpty()) continue;

            target.setStatus(Status.Damaged);
            damageables.forEach(damageable -> damageable.onDamaged(attackInfo));
        }
    }
}
