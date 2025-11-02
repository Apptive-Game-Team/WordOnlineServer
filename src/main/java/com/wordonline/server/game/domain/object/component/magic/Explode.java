//package com.wordonline.server.game.domain.object.component.magic;
//
//import com.wordonline.server.game.domain.AttackInfo;
//import com.wordonline.server.game.domain.object.GameObject;
//import com.wordonline.server.game.domain.object.component.Damageable;
//import com.wordonline.server.game.dto.Status;
//
//import java.util.List;
//
//public class Explode extends MagicComponent {
//    public static final float EXPLODE_DELAY = 0.5f;
//    public static final int EXPLODE_RADIUS = 3;
//
//    private boolean isRunning = false;
//
//    private final int damage;
//    private float counter = 0;
//
//    @Override
//    public void update() {
//        if (!isRunning) {
//            return;
//        }
//
//        if (counter < EXPLODE_DELAY) {
//            counter += gameObject.getGameLoop().deltaTime;
//        } else {
//            List<GameObject> gameObjects = gameObject.getGameLoop().physics.overlapCircleAll(gameObject, EXPLODE_RADIUS);
//            for (GameObject otherObject : gameObjects) {
//                List<Damageable> attackables = otherObject.getComponents(Damageable.class);
//
//                if (attackables.isEmpty()) {
//                    continue;
//                }
//                otherObject.setStatus(Status.Damaged);
//                attackables.forEach(attackable -> attackable.onDamaged(new AttackInfo(damage, gameObject.getElement().total())));
//            }
//
//            gameObject.setStatus(Status.Attack);
//            gameObject.destroy();
//        }
//    }
//
//    public Explode(GameObject gameObject, int damage) {
//        super(gameObject);
//        this.damage = damage;
//        isRunning = true;
//    }
//}
package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.dto.Status;

import java.util.List;

public class Explode extends MagicComponent {
    // 기존 기본값 유지
    public static final float EXPLODE_DELAY = 0.5f;
    public static final float EXPLODE_RADIUS = 3f;

    private final int damage;
    private final float radius;
    private final float delay;

    private boolean isRunning = false;
    private float counter = 0f;

    public Explode(GameObject gameObject, int damage) {
        this(gameObject, damage, EXPLODE_RADIUS, EXPLODE_DELAY);
    }

    public Explode(GameObject gameObject, int damage, float radius, float delay) {
        super(gameObject);
        this.damage = damage;
        this.radius = radius;
        this.delay = delay;
        this.isRunning = true;
    }

    @Override
    public void update() {
        if (!isRunning) return;

        if (counter < delay) {
            counter += getGameContext().getDeltaTime();
            return;
        }

        List<GameObject> gameObjects = getGameContext().overlapSphereAll(gameObject, radius);

        for (GameObject otherObject : gameObjects) {
            if (otherObject == gameObject) continue;

            List<Damageable> attackables = otherObject.getComponents(Damageable.class);
            if (attackables.isEmpty()) continue;

            otherObject.setStatus(Status.Damaged);
            AttackInfo info = new AttackInfo(damage, gameObject.getElement().total());
            attackables.forEach(a -> a.onDamaged(info));
        }

        gameObject.setStatus(Status.Attack);
        isRunning = false;
        gameObject.destroy();
    }
}
