package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.AlliedAreaEffectProvider;
import com.wordonline.server.game.domain.object.component.effect.receiver.BuildingEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("bubble_generator_prefab")
public class BubbleGeneratorPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public BubbleGeneratorPrefabInitializer(Parameters parameters) {
        super(PrefabType.BubbleGenerator);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("bubble_generator", "mass")));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("bubble_generator", "radius"), false));
        gameObject.addComponent(new DummyMob(gameObject, (int) parameters.getValue("bubble_generator", "hp")));
        gameObject.addComponent(new AlliedAreaEffectProvider(
                gameObject,
                (float) parameters.getValue("bubble_generator", "effect_interval"),
                (float) parameters.getValue("bubble_generator", "effect_radius"),
                Effect.Bubble
        ));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                (float) parameters.getValue("bubble_generator", "duration")
        ));
        gameObject.setElement(ElementType.WATER);
        gameObject.addComponent(new BuildingEffectReceiver(gameObject));
    }
}
