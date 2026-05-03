package com.wordonline.server.game.domain.object.component.build;

import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.dto.Master;

import java.util.List;

public class WindPushComponent extends Component {
    private final float pushForce;
    private final Vector3 boxSize;


    public WindPushComponent(GameObject gameObject, float pushForce, Vector3 boxSize) {
        super(gameObject);
        this.pushForce = pushForce;
        this.boxSize = boxSize;
    }

    @Override
    public void start() {
        Master master = gameObject.getMaster();
        Vector3 direction = (master == Master.LeftPlayer) ? Vector3.RIGHT : Vector3.LEFT;
        Vector3 centerOffset = direction.multiply(boxSize.getX() / 2);
        gameObject.drawBox(centerOffset, boxSize, GizmoCategory.AreaOfEffect);
    }

    @Override
    public void update() {
        Master master = gameObject.getMaster();
        if (master == Master.None) return;

        // Determine direction towards opponent player
        Vector3 direction = (master == Master.LeftPlayer) ? Vector3.RIGHT : Vector3.LEFT;
        
        // Calculate box center in front of the totem
        // The totem's position is the origin, we move the center forward by half the box length
        Vector3 center = gameObject.getPosition().plus(direction.multiply(boxSize.getX() / 2));
        
        List<GameObject> targets = getGameContext().getPhysics().overlapBoxAll(center, boxSize);
        
        for (GameObject target : targets) {
            if (target == gameObject) continue;
            
            // Push opponent's minions (Mobs)
            if (target.getMaster() != master && target.hasComponent(Mob.class)) {
                RigidBody rb = target.getComponent(RigidBody.class);
                if (rb != null) {
                    rb.addVelocity(direction.multiply(pushForce));
                }
            }
        }
    }

    @Override
    public void onDestroy() {
    }
}
