package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.mob.Totem;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;

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
        item = otherObject.getComponent(Totem.class);
    }
}
