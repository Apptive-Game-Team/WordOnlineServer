package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;

import java.util.function.Supplier;

public class EffectProvider extends Component implements Collidable {

    protected final Effect effect;

    public <T extends BaseStatusEffect> T getEffectByKey(String key) {
        for (Component c : gameObject.getComponents()) {
            if (c instanceof BaseStatusEffect se && key.equals(se.getKey())) return (T) se;
        }
        return null;
    }

    public <T extends BaseStatusEffect> T addOrReapplyEffect(
            String key,
            Supplier<T> factory,
            EffectApplyPolicy policy,
            float duration // 재적용 시 갱신용
    ) {
        T existing = getEffectByKey(key);
        if (existing != null) {
            switch (policy) {
                case IGNORE -> {}
                case REFRESH_DURATION -> existing.refresh(duration);
                case EXTEND_DURATION -> existing.extend(duration);
            }
            return existing;
        }
        T created = factory.get();
        gameObject.addComponent(created);
        return created;
    }

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
