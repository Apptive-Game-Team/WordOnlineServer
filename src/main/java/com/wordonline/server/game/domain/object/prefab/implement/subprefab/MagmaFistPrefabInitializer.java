package com.wordonline.server.game.domain.object.prefab.implement.subprefab;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.OnStartAttacker;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("magma_fist_prefab")
public class MagmaFistPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public MagmaFistPrefabInitializer(Parameters parameters) {
        super(PrefabType.MagmaFist);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("magma_fist", "radius"), true));
        gameObject.setElement(EnumSet.of(ElementType.FIRE, ElementType.ROCK));
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Burn));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("magma_fist", "duration")));
        gameObject.addComponent(new OnStartAttacker(
                gameObject,
                (float) parameters.getValue("magma_fist", "radius"),
                (int) parameters.getValue("magma_fist", "damage")
        ));
    }
}
