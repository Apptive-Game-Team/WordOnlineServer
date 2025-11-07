package com.wordonline.server.game.domain.object.prefab.implement.misc;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.SelfAttacker;
import com.wordonline.server.game.domain.object.component.effect.receiver.CommonEffectReceiver;
import com.wordonline.server.game.domain.object.component.mob.ManaWellMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.EnumSet;

@Component("mana_well_prefab")
public class ManaWellPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public ManaWellPrefabInitializer(Parameters parameters) {
        super(PrefabType.ManaWell);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("mana_well", "radius"), false));
        gameObject.getComponents().add(new ManaWellMob(gameObject,
                (int) parameters.getValue("mana_well", "hp")
        ));
        gameObject.addComponent(new SelfAttacker(
                gameObject,
                new AttackInfo((int) parameters.getValue("mana_well", "damage"), ElementType.NONE),
                (float) parameters.getValue("mana_well", "attack_interval")
                ));
        gameObject.setElement(EnumSet.of(ElementType.LIGHTNING, ElementType.NATURE));
        gameObject.getComponents().add(new CommonEffectReceiver(gameObject));
    }
}