package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Effect;

public class FireEffectReceiver extends CommonEffectReceiver {

    public FireEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onReceive(Effect effect) {
        switch (effect) {
            case Wet -> {
                if (gameObject.getComponent(WetStatusEffect.class) == null) {
                    gameObject.addComponent(new WetStatusEffect(gameObject, 5.0f));
                }
            }
            case Burn ->  {
            }
            case Shock -> {
                if (gameObject.getComponent(ShockStatusEffect.class) == null) {
                    gameObject.addComponent(new ShockStatusEffect(gameObject));
                }
            }
            case Snared ->  {
                if (gameObject.getComponent(SnaredStatusEffect.class) == null) {
                    gameObject.addComponent(new SnaredStatusEffect(gameObject,5.0f,5));
                }
            }
        }
    }
    
}
