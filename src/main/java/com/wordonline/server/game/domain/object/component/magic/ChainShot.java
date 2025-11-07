package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;

import java.util.List;

public class ChainShot extends MagicComponent implements Collidable {
    public final int speed;

    private final float chainDistacne;
    @Getter
    private Vector3 direction;
    private final int damage;

    private List<GameObject> victimList;


    public ChainShot(GameObject gameObject, int damage, int speed, float chainDistance) {
        super(gameObject);
        this.damage = damage;
        this.speed = speed;
        this.chainDistacne = chainDistance;
    }

    public void setTarget(Vector3 targetPosition) {
        direction = (targetPosition.subtract(gameObject.getPosition()));
    }

    @Override
    public void update() {
        if (direction == null) return;
        gameObject.setPosition(
                gameObject.getPosition()
                        .plus(direction.multiply(speed * getGameContext().getDeltaTime()))
        );
    }

    @Override
    public void onCollision(GameObject otherObject) {

        List<Damageable> attackables = otherObject.getComponents(Damageable.class);

        if (attackables.isEmpty()) return;
        if (victimList.contains(otherObject)) return;

        otherObject.setStatus(Status.Damaged);
        AttackInfo info = new AttackInfo(damage, gameObject.getElement().total());
        attackables.forEach(a -> a.onDamaged(info));

        victimList.add(otherObject);
        direction = Vector3.ZERO;

        List<GameObject> gameObjects = getGameContext().overlapSphereAll(gameObject, chainDistacne);

        for (GameObject go : gameObjects) {
            if (go == gameObject || go.getMaster() == gameObject.getMaster()) continue;
            if (victimList.contains(go)) continue;

            setTarget(go.getPosition());
            break;
        }
    }
}
