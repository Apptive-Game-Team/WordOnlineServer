package com.wordonline.server.game.domain.object.component.mob;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.detector.ClosestEnemyDetector;
import com.wordonline.server.game.domain.object.component.mob.detector.Detector;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class Totem extends Mob {

    private float timer = 0;
    private final AttackInfo attackInfo;
    private final int targetMask;
    private final float healCooldown;
    private final float healRange;

    public Totem(GameObject gameObject, int maxHp, int damage, float attackInterval, float range, int targetMask) {
        super(gameObject, maxHp, 0);
        attackInfo = new AttackInfo(damage, ElementType.NATURE);
        this.targetMask = targetMask;
        healCooldown = attackInterval;
        healRange = range;
    }

    @Override
    public void onDeath() {
        gameObject.destroy();
    }

    @Override
    public void start() {}

    @Override
    public void update() {
        super.update();
        timer += getGameContext().getDeltaTime();
        if (timer >= healCooldown) {
            timer = 0;
            //heal circle prefab
            List<GameObject> objects =  getGameContext().overlapSphereAll(gameObject, healRange);
            for (GameObject object : objects) {
                if (gameObject.getMaster() != object.getMaster()) return;
                Mob mob = object.getComponent(Mob.class);
                if(mob != null){
                    mob.applyDamage(attackInfo);
                }
            }
            gameObject.setStatus(Status.Attack);
        }
    }

    @Override
    public void onDestroy() {

    }
}
