package com.wordonline.server.game.domain.object.prefab.implement.build;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.receiver.BuildingEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.simple.LightningTowerMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

@Component("electric_tower_prefab")
public class ElectricTowerPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ElectricTowerPrefabInitializer(Parameters parameters) {
        super(PrefabType.ElectricTower);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("electric_tower", "mass")));
        gameObject.addCollider(new CircleCollider(gameObject, (float) parameters.getValue("electric_tower", "radius"), false));
        gameObject.addComponent(new LightningTowerMob(
                gameObject,
                (int) parameters.getValue("electric_tower", "hp"),
                (int) parameters.getValue("electric_tower", "damage"),
                (int) parameters.getValue("electric_tower", "chain_damage"),
                (int) parameters.getValue("electric_tower", "chain_count"),
                (float) parameters.getValue("electric_tower", "attack_interval"),
                (float) parameters.getValue("electric_tower", "attack_range"),
                (float) parameters.getValue("electric_tower", "chain_radius")
        ));
        gameObject.addComponent(new TimedSelfDestroyer(
                gameObject,
                (float) parameters.getValue("electric_tower", "duration")
        ));
        gameObject.setElement(ElementType.LIGHTNING);
        gameObject.addComponent(new BuildingEffectReceiver(gameObject));
    }
}
