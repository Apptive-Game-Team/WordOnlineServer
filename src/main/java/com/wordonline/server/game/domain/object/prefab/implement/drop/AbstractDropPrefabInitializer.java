package com.wordonline.server.game.domain.object.prefab.implement.drop;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.magic.Drop;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public abstract class AbstractDropPrefabInitializer extends PrefabInitializer {

    private final Parameters parameters;
    private final ElementType elementType;

    public AbstractDropPrefabInitializer(
            ElementType elementType, Parameters parameters) {
        super(getPrefabType(elementType));
        this.elementType = elementType;
        this.parameters = parameters;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("drop", "radius"), true));
        gameObject.setElement(elementType);
        gameObject.getComponents().add(new Drop(gameObject, (int) parameters.getValue("drop", "damage")));
    }

    private static PrefabType getPrefabType(ElementType elementType) {
        return switch (elementType) {
            case ElementType.FIRE -> PrefabType.FireDrop;
            case ElementType.WATER -> PrefabType.WaterDrop;
            case ElementType.NATURE -> PrefabType.NatureDrop;
            case ElementType.ROCK -> PrefabType.RockDrop;
            case ElementType.LIGHTNING -> PrefabType.LightningDrop;
            case ElementType.WIND -> PrefabType.WindDrop;
            default -> null;
        };
    }
}
