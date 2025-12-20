package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.PlayerStatusSetter;
import com.wordonline.server.game.domain.object.component.mob.simple.PlayerHealthComponent;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

@Component("player_prefab")
public class PlayerPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public PlayerPrefabInitializer(Parameters parameters) {
        super(PrefabType.Player);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, 1, false));
        gameObject.setElement(ElementType.NONE);
        gameObject.addComponent(new PlayerHealthComponent(gameObject));
        gameObject.addComponent(new PlayerStatusSetter(gameObject));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}