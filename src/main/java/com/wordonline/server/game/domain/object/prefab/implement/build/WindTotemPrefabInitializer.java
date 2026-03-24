package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.build.WindPushComponent;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.component.SelfAttacker;
import com.wordonline.server.game.domain.object.component.mob.simple.DummyMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("wind_totem_prefab")
public class WindTotemPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    
    public WindTotemPrefabInitializer(Parameters parameters) {
        super(PrefabType.WindTotem);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("wind_totem", "mass")));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("wind_totem", "radius"), false));

        float pushForce = 10;
        float pushRangeX = 6;
        float pushRangeY = 3;

        gameObject.addComponent(new DummyMob(gameObject, (int) parameters.getValue("wind_totem", "hp")));
        gameObject.addComponent(new WindPushComponent(gameObject, pushForce, new Vector3(pushRangeX, pushRangeY, 1.0f)));

        gameObject.addComponent(new SelfAttacker(gameObject,
                new AttackInfo((int) parameters.getValue("wind_totem", "damage"), ElementType.WIND),
                (int) parameters.getValue("wind_totem", "attack_interval")));
        gameObject.setElement(ElementType.ROCK);
        gameObject.addComponent(new CommonEffectReceiver(gameObject));
    }
}
