package com.wordonline.server.game.domain.object.prefab.implement.explode;

import java.util.Optional;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.parameter.GameObjectKey;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.effect.EffectProvider;
import com.wordonline.server.game.domain.object.component.magic.Explode;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;

public abstract class AbstractExplodePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    private final ElementType elementType;

    public AbstractExplodePrefabInitializer(
            ElementType elementType, Parameters parameters) {
        super(getPrefabType(elementType));
        this.elementType = elementType;
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        var explodeParameters = parameters.object(getGameObjectKey(elementType));
        gameObject.addCollider(new CircleCollider(
                gameObject,
                explodeParameters.floatValue(ParameterKey.RADIUS),
                true
        ));
        gameObject.setElement(elementType);
        getEffect(elementType)
                .ifPresent(
                        effect -> gameObject.getComponents().add(new EffectProvider(gameObject, effect))
                );
        gameObject.getComponents().add(new Explode(
                gameObject,
                explodeParameters.intValue(ParameterKey.DAMAGE),
                explodeParameters.floatValue(ParameterKey.RADIUS)
        ));
    }

    public static GameObjectKey getGameObjectKey(ElementType elementType) {
        return switch (elementType) {
            case FIRE -> GameObjectKey.FIRE_EXPLODE;
            case ROCK -> GameObjectKey.ROCK_EXPLODE;
            case WIND -> GameObjectKey.WIND_EXPLODE;
            case WATER -> GameObjectKey.WATER_EXPLODE;
            case NATURE -> GameObjectKey.LEAF_EXPLODE;
            case LIGHTNING -> GameObjectKey.ELECTRIC_EXPLODE;
            default -> GameObjectKey.EXPLODE;
        };
    }

    public static PrefabType getPrefabType(ElementType elementType) {
        return switch (elementType) {
            case FIRE -> PrefabType.FireExplode;
            case ROCK -> PrefabType.RockExplode;
            case WIND -> PrefabType.WindExplode;
            case WATER -> PrefabType.WaterExplode;
            case NATURE -> PrefabType.LeafExplode;
            case LIGHTNING -> PrefabType.ElectricExplode;
            default -> null;
        };
    }

    public static Optional<Effect> getEffect(ElementType elementType) {
        return Optional.ofNullable(switch (elementType) {
            case FIRE -> Effect.Burn;
            case WATER -> Effect.Wet;
            case NATURE -> Effect.Snared;
            case LIGHTNING -> Effect.Shock;
            default -> null;
        });
    }
}
