package com.wordonline.server.game.domain.object.component.effect.receiver;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.PrefabType;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;

public class WaterFieldEffectReceiver extends Component implements EffectReceiver {
    @Override
    public void onReceive(Effect effect) {
        if (effect == Effect.Shock) {
            new GameObject(gameObject, Master.None, PrefabType.ElectricField);
            gameObject.destroy();
        }
    }

    @Override
    public void onReceive(Effect effect, Vector2 direction, float prox) {

    }


    @Override
    public void start() { }

    @Override
    public void update() { }

    @Override
    public void onDestroy() { }

    public WaterFieldEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }
}
