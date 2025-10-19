package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.receiver.LeafFieldEffectReceiver;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("leaf_field_prefab")
public class LeafFieldPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public LeafFieldPrefabInitializer(Parameters parameters) {
        super(PrefabType.LeafField);
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("field", "radius"), true));
        gameObject.setElement(ElementType.NATURE);
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.Snared));
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.LeafFieldHeal));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, (float) parameters.getValue("field", "duration")));
    }
}