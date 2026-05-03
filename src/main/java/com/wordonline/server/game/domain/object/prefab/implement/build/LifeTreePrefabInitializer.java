package com.wordonline.server.game.domain.object.prefab.implement.build;

import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.simple.Totem;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("life_tree_prefab")
public class LifeTreePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public LifeTreePrefabInitializer(Parameters parameters) {
        super(PrefabType.LifeTree);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("life_tree", "mass")));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("life_tree", "radius"), true));
        gameObject.addComponent(new Totem(gameObject,
                (int) parameters.getValue("life_tree", "hp"),
                (int) parameters.getValue("life_tree", "damage"),
                (float)parameters.getValue("life_tree", "attack_interval"),
                (float)parameters.getValue("life_tree", "range"),
                TargetMask.GROUND.bit));
        gameObject.setElement(EnumSet.of(ElementType.NATURE));
        gameObject.addComponent(new TimedSelfDestroyer(gameObject, (int) parameters.getValue("life_tree", "duration")));
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
