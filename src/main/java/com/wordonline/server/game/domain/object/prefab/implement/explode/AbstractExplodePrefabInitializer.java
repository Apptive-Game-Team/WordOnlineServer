package com.wordonline.server.game.domain.object.prefab.implement.explode;

import java.util.Optional;

import com.wordonline.server.game.domain.Parameters;
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
        gameObject.getColliders().add(new CircleCollider(
                gameObject,
                (float) parameters.getValue("explode", "radius"),
                true
        ));
        gameObject.setElement(elementType);
        getEffect(elementType)
                .ifPresent(
                        effect -> gameObject.getComponents().add(new EffectProvider(gameObject, effect))
                );
        gameObject.getComponents().add(new Explode(
                gameObject,
                (int) parameters.getValue("explode", "damage"),
                (float) parameters.getValue("explode", "radius")
        ));
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
