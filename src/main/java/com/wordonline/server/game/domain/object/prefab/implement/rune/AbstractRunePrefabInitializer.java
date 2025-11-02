package com.wordonline.server.game.domain.object.prefab.implement.rune;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public abstract class AbstractRunePrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;

    public AbstractRunePrefabInitializer(
            Parameters parameters,
            ElementType elementType) {
        super(getPrefabType(elementType));
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("rune", "radius"), true));
        // TODO Rune 개발
        //        gameObject.addComponent(new Rune());
    }

    private static PrefabType getPrefabType(ElementType elementType) {
        return switch (elementType) {
            case ElementType.FIRE -> PrefabType.FireRune;
            case ElementType.WATER -> PrefabType.WaterRune;
            case ElementType.NATURE -> PrefabType.NatureRune;
            case ElementType.ROCK -> PrefabType.RockRune;
            case ElementType.LIGHTNING -> PrefabType.LightningRune;
            case ElementType.WIND -> PrefabType.WindRune;
            default -> null;
        };
    }
}
