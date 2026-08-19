package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.VineHitTracker;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.util.CombatRange;

public class OnStartAttacker extends Component {

    private final float attackRange;
    private final int damage;
    private final VineHitTracker hitTracker;

    public OnStartAttacker(GameObject gameObject, float attackRange, int damage) {
        this(gameObject, attackRange, damage, null);
    }

    public OnStartAttacker(GameObject gameObject, float attackRange, int damage, VineHitTracker hitTracker) {
        super(gameObject);
        this.attackRange = attackRange;
        this.damage = damage;
        this.hitTracker = hitTracker;
    }

    @Override
    public void start() {
        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total()).withAttacker(gameObject);
        Master owner = gameObject.getMaster();

        getGameContext().getGameSessionData().gameObjects.stream()
                .filter(gameObject1 -> gameObject1 != gameObject)
                .filter(GameObject::isActive)
                .filter(gameObject1 -> CombatRange.contains(gameObject, gameObject1, attackRange))
                .filter(gameObject1 -> gameObject1.hasComponent(Mob.class))
                .filter(gameObject1 -> owner == Master.None || gameObject1.getMaster() != owner)
                .filter(gameObject1 -> hitTracker == null || hitTracker.markIfFirstHit(gameObject1.getId()))
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
