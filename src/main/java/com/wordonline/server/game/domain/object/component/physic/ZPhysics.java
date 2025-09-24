package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import lombok.Getter;
import lombok.Setter;

import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.newSetFromMap;

public class ZPhysics extends Component {

    private float zVelocity;
    @Setter
    private float groundZ = 0f;
    @Setter
    private float gravity = GameConfig.GRAVITY_ACCEL;

    private float hoverZ;
    private boolean isHover;
    private final Set<Object> hoverLocks =
            newSetFromMap(new IdentityHashMap<>());
    @Setter
    private float fallThreshold = GameConfig.FALL_THRESHOLD;
    @Setter
    private float fallDamageVelocityUnit = GameConfig.FALL_THRESHOLD_VELOCITY;

    public ZPhysics(GameObject go) {
        super(go);
    }
    public ZPhysics(GameObject go, float hoverZ) {
        super(go);
        this.hoverZ = hoverZ;
        this.isHover = true;
    }

    public void addImpulseZ(float force) {
        zVelocity += force;
    }

    public boolean canHover(){
        return isHover && hoverLocks.isEmpty();
    }

    public void LockHover(Object obj){ hoverLocks.add(obj); }
    public void UnlockHover(Object obj){ hoverLocks.remove(obj); }

    public void applyHover()
    {
        Vector3 p = gameObject.getPosition();
        float curZ = p.getZ();
        if(curZ != hoverZ) gameObject.setPosition(new Vector3(p.getX(), p.getY(), hoverZ));
    }

    public void applyZForce() {
        final float dt = gameObject.getGameLoop().deltaTime;

        zVelocity -= gravity * dt;

        Vector3 p = gameObject.getPosition();
        final float curZ = p.getZ();
        float z = p.getZ() + zVelocity * dt;

        if (z < groundZ) {
            z = groundZ;
            if (zVelocity < 0f && Math.abs(p.getZ() - groundZ) > fallThreshold) {
                int fallDamage = (int) Math.floor(Math.abs(zVelocity) / fallDamageVelocityUnit);
                Mob mob = gameObject.getComponent(Mob.class);
                if(mob != null){
                    mob.applyDamage(new AttackInfo(fallDamage, ElementType.NONE));
                }
            }
            zVelocity = 0f;
        }
        if(curZ != z) gameObject.setPosition(new Vector3(p.getX(), p.getY(), z));
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void onDestroy() {}
}
