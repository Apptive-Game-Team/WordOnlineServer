package com.wordonline.server.game.domain.object.prefab.implement.field;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.TimedSelfDestroyer;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

/**
 * Common scaffold for all elemental-field prefabs.
 *
 * <p>Subclasses must supply the {@link ElementType}, its primary {@link Effect},
 * and the {@link GameObjectKey} that holds RADIUS / DURATION parameters.
 * Additional components (e.g. element-specific effect receivers) can be registered
 * by overriding {@link #addExtraComponents(GameObject, Parameters)}.
 */
public abstract class AbstractFieldPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    private final ElementType elementType;
    private final Effect primaryEffect;
    private final GameObjectKey gameObjectKey;

    protected AbstractFieldPrefabInitializer(
            PrefabType prefabType,
            ElementType elementType,
            Effect primaryEffect,
            GameObjectKey gameObjectKey,
            Parameters parameters) {
        super(prefabType);
        this.elementType = elementType;
        this.primaryEffect = primaryEffect;
        this.gameObjectKey = gameObjectKey;
        this.parameters = parameters;
    }

    @Override
    public final void initialize(GameObject gameObject) {
        gameObject.setPosition(gameObject.getPosition().grounded());

        var fieldParameters = parameters.object(gameObjectKey);
        gameObject.addCollider(new CircleCollider(gameObject, fieldParameters.floatValue(ParameterKey.RADIUS), true));
        gameObject.setElement(elementType);
        gameObject.getComponents().add(new EffectProvider(gameObject, primaryEffect));
        addExtraComponents(gameObject, parameters);
        gameObject.getComponents().add(new TimedSelfDestroyer(gameObject, fieldParameters.floatValue(ParameterKey.DURATION)));
    }

    /**
     * Hook for subclasses to register additional components (e.g. effect receivers).
     * The default implementation does nothing.
     */
    protected void addExtraComponents(GameObject gameObject, Parameters parameters) {
        // no-op by default
    }
}
