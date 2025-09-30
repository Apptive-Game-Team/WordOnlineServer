package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Effect;

public class BuildingEffectReceiver extends CommonEffectReceiver {

    public BuildingEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onReceive(Effect effect) {
        switch (effect) {
            case Wet -> {
                if (gameObject.getComponent(WetStatusEffect.class) == null && !gameObject.getElement().has(ElementType.WATER)) {
                    gameObject.addComponent(new WetStatusEffect(gameObject, 5.0f));
                }
            }
            case Burn ->  {
                if (gameObject.getComponent(BurnStatusEffect.class) == null && !gameObject.getElement().has(ElementType.FIRE)) {
                    gameObject.addComponent(new BurnStatusEffect(gameObject, 5.0f));
                }
            }
            case Snared ->  {
                if (gameObject.getComponent(SnaredStatusEffect.class) == null && !gameObject.getElement().has(ElementType.NATURE)) {
                    gameObject.addComponent(new SnaredStatusEffect(gameObject, 5.0f, 5));
                }
            }
        }
    }
    
}
