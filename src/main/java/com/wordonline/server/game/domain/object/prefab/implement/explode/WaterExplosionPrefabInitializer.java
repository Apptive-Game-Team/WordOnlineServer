package com.wordonline.server.game.domain.object.prefab.implement.explode;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.WaterExplode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

@Component("water_explosion_prefab")
public class WaterExplosionPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    
    public WaterExplosionPrefabInitializer(Parameters parameters) {
        super(PrefabType.WaterExplosion);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addCollider(new CircleCollider(
                gameObject,
                (float) parameters.getValue("water_explosion", "radius"),
                true
        ));
        gameObject.setElement(ElementType.FIRE);
        gameObject.addComponent(new EffectProvider(gameObject, Effect.Burn));
        gameObject.addComponent(new WaterExplode(
                gameObject,
                (int) parameters.getValue("water_explosion", "damage"),
                (float) parameters.getValue("water_explosion", "radius"),
                (float) parameters.getValue("water_explosion", "z_force")
        ));
    }
}
