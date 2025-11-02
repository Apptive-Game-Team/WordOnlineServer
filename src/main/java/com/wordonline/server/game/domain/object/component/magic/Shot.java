package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;

import java.util.List;

public class Shot extends MagicComponent implements Collidable {
    public static final int SPEED = 2;

    // 충돌 즉시 터지는 작은 폭발 반경(원하는 값으로 조정)
    private static final float IMPACT_EXPLOSION_RADIUS = 1f;

    @Getter
    private Vector3 direction;
    private final int damage;

    private boolean explosionTriggered = false;

    public Shot(GameObject gameObject, int damage) {
        super(gameObject);
        this.damage = damage;
    }

    public void setTarget(Vector3 targetPosition) {
        direction = (targetPosition.subtract(gameObject.getPosition()));
    }

    @Override
    public void update() {
        if (direction == null) return;
        gameObject.setPosition(
                gameObject.getPosition()
                        .plus(direction.multiply(SPEED * getGameContext().getDeltaTime()))
        );
    }

    @Override
    public void onCollision(GameObject otherObject) {
        // 이미 폭발 시도했다면 무시(다중 충돌 방지)
        if (explosionTriggered) return;

        // 이전 로직과 동일하게 "피해 가능 대상"에 부딪힌 경우만 폭발
        List<Damageable> attackables = otherObject.getComponents(Damageable.class);
        if (attackables.isEmpty()) return;

        explosionTriggered = true;

        // 발사체 이동 정지 및 폭발 트리거
        direction = Vector3.ZERO;
        gameObject.setStatus(Status.Attack);
        otherObject.setStatus(Status.Damaged);
        attackables.forEach(attackable -> attackable.onDamaged(new AttackInfo(damage, gameObject.getElement().total())));

        // 지연 0f로 즉시 폭발; 피해/상태이상은 Explode가 처리
        new Explode(gameObject, damage, IMPACT_EXPLOSION_RADIUS, 0f);
    }
}
