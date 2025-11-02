package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.physic.Collidable;

public class Rune extends Component implements Collidable {

    public Rune(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onCollision(GameObject otherObject) {
        explode();
        gameObject.destroy();
    }

    private void explode() {
        float radius = (float) getGameContext().getParameters()
                .getValue("rune", "distance");
        int damage = (int) getGameContext().getParameters()
                .getValue("rune", "distance");
        AttackInfo attackInfo = new AttackInfo(damage, gameObject.getElement().total());
        getGameContext().getPhysics()
                .overlapCircleAll(gameObject, radius)
                .forEach(target -> {
                    target.getComponent(Mob.class)
                            .onDamaged(attackInfo);
                });
    }
}
