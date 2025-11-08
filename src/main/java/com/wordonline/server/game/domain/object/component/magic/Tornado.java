package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Status;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tornado extends MagicComponent implements Collidable {

    @Getter
    private Vector3 direction;
    private final int damage;
    private static final float BONUS_DAMAGE = 0.5f;
    private static final float HEIGHT = 5f;
    private final float speed;
    private final float radius;
    private final float duration;
    private final float attackInterval;


    // 회전/상승 파라미터 (필요하면 조정)
    private static final float ANGULAR_SPEED = (float) Math.toRadians(360f); // 1rev/s = 360deg/s
    private static final float LIFT_SPEED = 3f;                               // z 상승 속도 (units/s)
    private static final float MIN_ORBIT_RADIUS = 0.1f;                       // 중심 흡착 방지
    // 각 피해자의 현재 각도와 궤도 반경을 저장 (회전력 유지용)
    private final Map<GameObject, Float> angles = new HashMap<>();
    private final Map<GameObject, Float> orbits = new HashMap<>();
    private List<GameObject> victimList;

    public Tornado(GameObject gameObject, float speed, int damage, float radius, float duration, float attackInterval) {
        super(gameObject);
        this.damage = damage;
        this.speed = speed;
        this.radius = radius;
        this.duration = duration;
        this.attackInterval = attackInterval;
        setTarget(new Vector3(GameConfig.X_MID,GameConfig.Y_MID,0));
    }

    public void setTarget(Vector3 targetPosition) {
        direction = (targetPosition.subtract(gameObject.getPosition()));
    }

    @Override
    public void update() {

        if (direction == null) return;

        // 토네이도 이동
        gameObject.setPosition(
                gameObject.getPosition()
                        .plus(direction.multiply(speed * getGameContext().getDeltaTime()))
        );

        // 피해자들 회전 + 상승
        final Vector3 center = gameObject.getPosition();
        for (GameObject victim : new ArrayList<>(victimList)) {
            if (victim == null) continue;

            // 초기 진입 시 state 보정이 안돼있으면 스킵
            if (!angles.containsKey(victim) || !orbits.containsKey(victim)) continue;

            float angle = angles.get(victim);
            float r = orbits.get(victim);

            // 시계방향: 각도 감소
            angle -= ANGULAR_SPEED * getGameContext().getDeltaTime() ;

            // XY 궤도 위치 갱신
            float newX = center.getX() + (float) Math.cos(angle) * r;
            float newY = center.getY() + (float) Math.sin(angle) * r;

            // Z 상승 (최대 center.z + HEIGHT 까지)
            Vector3 vp = victim.getPosition();
            float targetZ = center.getZ() + HEIGHT;
            float newZ = vp.getZ() < targetZ ? Math.min(vp.getZ() + LIFT_SPEED * getGameContext().getDeltaTime(), targetZ) : vp.getZ();

            victim.setPosition(new Vector3(newX, newY, newZ));

            // 각도 갱신 저장
            angles.put(victim, angle);
        }
    }

    @Override
    public void onCollision(GameObject otherObject) {

        List<Damageable> attackables = otherObject.getComponents(Damageable.class);
        if (attackables.isEmpty()) return;

        if (!victimList.contains(otherObject)) {
            victimList.add(otherObject);

            final Vector3 center = gameObject.getPosition();
            final Vector3 op = otherObject.getPosition();

            // 진입 시 현재 위치 기준으로 초기 각/반경 설정
            float angle = (float) Math.atan2(center.getY() - op.getY(), center.getX()- op.getX());
            float orbit = Math.max(MIN_ORBIT_RADIUS, (float) center.distance(op));

            angles.put(otherObject, angle);
            orbits.put(otherObject, orbit);
        }
    }
}
