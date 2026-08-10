package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.simple.Totem;
import com.wordonline.server.game.domain.object.component.physic.Collidable;

import com.wordonline.server.game.dto.Master;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Item extends Component {

    private GameObject parent;

    public Item(GameObject gameObject) {
        super(gameObject);
    }

    public GameObject getParent() {
        return parent;
    }

    @Override
    public void start() { }

    @Override
    public void update() {
        if(parent != null)
        {
            gameObject.setPosition(parent.getPosition());
        } else {
            var circleCollider = gameObject.getFirstCircleCollider();
            if (circleCollider.isEmpty()) return;
            getGameContext().overlapSphereAll(gameObject, circleCollider.get().getRadius()).stream()
                    .filter(overlap -> overlap.getMaster() == Master.None || overlap.getMaster() == gameObject.getMaster())
                    .filter(overlap -> overlap.getId() != gameObject.getId())
                    .findFirst()
                    .ifPresent(overlap -> {
                        parent = overlap;
                        log.info("Item {} is picked up by {}", gameObject.getId(), parent.getId());
                    });
        }
    }

    @Override
    public void onDestroy() { }
}
