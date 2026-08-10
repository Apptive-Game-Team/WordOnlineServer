package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.Mob;

import lombok.Getter;
import lombok.Setter;

import java.util.IdentityHashMap;
import java.util.Set;

import static java.util.Collections.newSetFromMap;

public class ZPhysics extends Component {

    private float yVelocity;
    @Getter
    @Setter
    private float groundY = 0f;
    @Getter
    @Setter
    private float gravity = GameConfig.GRAVITY_ACCEL;
    private float floatingVelocity = GameConfig.DEFAULT_FLOATING_VELOCITY;

    private float hoverY;
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
    public ZPhysics(GameObject go, float hoverY) {
        super(go);
        this.hoverY = hoverY;
        this.isHover = true;
    }

    public void addImpulseZ(float force) {
        yVelocity += force;
    }

    public boolean canHover(){
        return isHover && hoverLocks.isEmpty();
    }

    // an aerial object keeps itself above the ground by hovering, so it has to fall down when it dies
    public boolean isAerial(){
        return isHover && hoverY > groundY;
    }

    public void lockHover(Object obj){ hoverLocks.add(obj); }
    public void unlockHover(Object obj){ hoverLocks.remove(obj); }

    public void applyHover()
    {
        Vector3 p = gameObject.getPosition();
        float curY = p.getY();
        if(curY != hoverY) {

            float deltaZ = floatingVelocity * getGameContext().getDeltaTime();

            if (Math.abs(curY - hoverY) < deltaZ) {
                gameObject.setPosition(new Vector3(p.getX(), hoverY, p.getZ()));
            } else {
                float direction = Math.signum(hoverY - curY);
                gameObject.setPosition(new Vector3(p.getX(), curY + deltaZ * direction, p.getZ()));
            }
        }
    }

    public void applyZForce() {
        final float dt = getGameContext().getDeltaTime();

        yVelocity -= gravity * dt;

        Vector3 p = gameObject.getPosition();
        final float curY = p.getY();
        float y = p.getY() + yVelocity * dt;

        if (y < groundY) {
            y = groundY;
            if (yVelocity < 0f && Math.abs(p.getY() - groundY) > fallThreshold) {
                int fallDamage = (int) Math.floor(Math.abs(yVelocity) / fallDamageVelocityUnit);
                Mob mob = gameObject.getComponent(Mob.class);
                if(mob != null){
                    mob.applyDamage(new AttackInfo(fallDamage, ElementType.NONE));
                }
            }
            yVelocity = 0f;
        }
        if(curY != y) gameObject.setPosition(new Vector3(p.getX(), y, p.getZ()));
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void onDestroy() {}
}
