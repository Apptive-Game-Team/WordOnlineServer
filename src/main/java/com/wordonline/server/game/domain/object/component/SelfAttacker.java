package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;

public class SelfAttacker extends Component {

    private final float attackInterval;
    private final AttackInfo attackInfo;
    private Mob self;
    private float counter = 0;

    public SelfAttacker(GameObject gameObject, AttackInfo attackInfo, float interval) {
        super(gameObject);
        attackInterval = interval;
        this.attackInfo = attackInfo;
    }

    @Override
    public void start() {
        self = gameObject.getComponent(Mob.class);
    }

    @Override
    public void update() {
        counter += getGameContext().getDeltaTime();

        if (counter > attackInterval) {
            counter = 0;
            self.onDamaged(attackInfo);
        }
    }

    @Override
    public void onDestroy() {

    }
}
