package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.CombatDeathListener;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.prefab.PrefabType;

public class RockDeathRemnant extends Component implements CombatDeathListener {
    private boolean consumed;

    public RockDeathRemnant(GameObject gameObject) {
        super(gameObject);
    }

    @Override
    public void onCombatDeath() {
        if (consumed) {
            return;
        }
        consumed = true;
        new GameObject(
                gameObject.getMaster(),
                PrefabType.MiniRock,
                new Vector3(gameObject.getPosition()),
                getGameContext()
        );
    }

    @Override
    public void start() {
    }

    @Override
    public void update() {
    }

    @Override
    public void onDestroy() {
    }
}
