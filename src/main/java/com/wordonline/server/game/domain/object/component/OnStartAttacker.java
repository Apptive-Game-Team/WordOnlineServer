package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class OnStartAttacker extends Component {

    private final float attackRange;
    private final int damage;

    public OnStartAttacker(GameObject gameObject, float attackRange, int damage) {
        super(gameObject);
        this.attackRange = attackRange;
        this.damage = damage;
    }

    @Override
    public void start() {
        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total());

        getGameContext().overlapSphereAll(gameObject, attackRange)
                .stream().filter(gameObject1 -> gameObject1.hasComponent(Mob.class))
                .map(gameObject1 -> gameObject1.getComponent(Mob.class))
                .forEach(gameObject2 -> gameObject2.onDamaged(attackInfo));
    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }
}
