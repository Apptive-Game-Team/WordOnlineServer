package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.simple.Totem;
import com.wordonline.server.game.domain.object.component.physic.Collidable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ItemCarrier extends Component implements Collidable {

    private Totem item;

    public ItemCarrier(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void start() { }

    @Override
    public void update() {
        if(item != null)
        {
            item.gameObject.setPosition(gameObject.getPosition());
        }
    }

    @Override
    public void onDestroy() { }

    @Override
    public void onCollision(GameObject otherObject) {
        if(item != null) return;
        Totem totem = otherObject.getComponent(Totem.class);
        if(totem == null) return;
        log.trace(totem.gameObject.toString());
        item = totem;
    }
}
