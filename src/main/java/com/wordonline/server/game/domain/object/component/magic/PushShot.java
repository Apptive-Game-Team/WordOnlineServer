package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.Damageable;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.dto.Status;

import java.util.ArrayList;
import java.util.List;

public class PushShot extends Shot implements Collidable {


    private final List<GameObject> pushedTargets = new ArrayList<>();
    public PushShot(GameObject gameObject, int damage, float speed) {
        super(gameObject, damage);
        this.speed = speed;
    }

    @Override
    public void update() {
        // 부모의 이동 로직 사용 + 델타만 계산해서 같이 밀기
        if (getDirection() == null) return;

        Vector3 prevPos = gameObject.getPosition();

        // Shot의 update() 호출 → 투사체 실제 이동
        super.update();

        Vector3 newPos = gameObject.getPosition();
        Vector3 delta = newPos.subtract(prevPos);

        // 같이 끌려가는 애들 위치도 델타만큼 이동
        for (GameObject target : pushedTargets) {
            if (target == null) continue;
            if (target.getMaster() == gameObject.getMaster()) continue; // 혹시나 아군이면 스킵
            target.setPosition(target.getPosition().plus(delta));
        }
    }

    @Override
    public void onCollision(GameObject otherObject) {

        if (otherObject == gameObject) return;
        if (otherObject.getMaster() == gameObject.getMaster()) return;

        // 이미 끌고 있는 대상이면 무시
        if (pushedTargets.contains(otherObject)) return;

        List<Damageable> damageables = otherObject.getComponents(Damageable.class);
        if (damageables.isEmpty()) return;

        // 첫 충돌 시 딱 한 번 데미지
        AttackInfo info = new AttackInfo(damage, gameObject.getElement().total());
        otherObject.setStatus(Status.Damaged);
        damageables.forEach(d -> d.onDamaged(info));

        // 이제부터 PushShot 끝날 때까지 같이 밀고 가기
        pushedTargets.add(otherObject);
    }
}
