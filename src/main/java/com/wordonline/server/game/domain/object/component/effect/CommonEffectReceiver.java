package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;

public class CommonEffectReceiver extends Component implements EffectReceiver {
    private boolean isWet = false;

    @Override
    public void onReceive(Effect effect) {
        switch (effect) {
            case Wet -> {
                if (gameObject.getComponent(WetStatusEffect.class) == null) {
                    gameObject.addComponent(new WetStatusEffect(gameObject, 5.0f));
                }
            }
            case Burn ->  {
                if (gameObject.getComponent(BurnStatusEffect.class) == null) {
                    gameObject.addComponent(new BurnStatusEffect(gameObject, 5.0f));
                }
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

    @Override
    public void start() { }

    @Override
    public void update() { }

    @Override
    public void onDestroy() { }

    public CommonEffectReceiver(GameObject gameObject) {
        super(gameObject);
    }
}