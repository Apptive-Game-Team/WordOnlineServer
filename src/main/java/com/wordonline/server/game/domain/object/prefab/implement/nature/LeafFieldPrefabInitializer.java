package com.wordonline.server.game.domain.object.prefab.implement.nature;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.effect.receiver.LeafFieldEffectReceiver;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.object.prefab.implement.field.AbstractFieldPrefabInitializer;
import com.wordonline.server.game.dto.Effect;
import org.springframework.stereotype.Component;

@Component("leaf_field_prefab")
public class LeafFieldPrefabInitializer extends AbstractFieldPrefabInitializer {

    public LeafFieldPrefabInitializer(Parameters parameters) {
        super(PrefabType.LeafField, ElementType.NATURE, Effect.Snared, GameObjectKey.LEAF_FIELD, parameters);
    }

    @Override
    protected void addExtraComponents(GameObject gameObject, Parameters parameters) {
        gameObject.getComponents().add(new EffectProvider(gameObject, Effect.LeafFieldHeal));
        gameObject.getComponents().add(new LeafFieldEffectReceiver(gameObject));
    }
}
