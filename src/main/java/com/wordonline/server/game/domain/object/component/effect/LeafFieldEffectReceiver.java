package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;

public class LeafFieldEffectReceiver extends Component implements EffectReceiver {
    @Override
    public void onReceive(Effect effect) {
        if (effect == Effect.Burn) {
            new GameObject(gameObject, PrefabType.FireField);
            gameObject.destroy();
        }
    }

    @Override
    public void start() { }

    @Override
    public void update() { }

    @Override
    public void onDestroy() { }

    public LeafFieldEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }
}
