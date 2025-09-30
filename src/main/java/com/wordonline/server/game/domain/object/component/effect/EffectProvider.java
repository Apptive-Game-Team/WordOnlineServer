package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.receiver.EffectReceiver;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;

public class EffectProvider extends Component implements Collidable {

    protected final Effect effect;

    @Override
    public void onCollision(GameObject otherObject) {
        EffectReceiver effectReceiver = (EffectReceiver) otherObject.getComponent(EffectReceiver.class);
        if (effectReceiver != null) {
            effectReceiver.onReceive(effect);
        }
    }

    @Override
    public void start() { }

    @Override
    public void update() { }

    @Override
    public void onDestroy() { }

    public EffectProvider(GameObject gameObject, Effect effect) {
        super(gameObject);
        this.effect = effect;
    }
}
