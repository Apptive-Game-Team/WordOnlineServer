package com.wordonline.server.game.domain.object.component;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.dto.Master;

public class PlayerHealthComponent extends Mob {
    private static final int MAX_HEALTH = 100;
    PlayerData playerData;

    @Override
    public void onDamaged(AttackInfo attackInfo) {
        super.onDamaged(attackInfo);
        playerData.hp = hp;
    }

    @Override
    public void start() {}

    @Override
    public void update() {}

    @Override
    public void onDestroy() {}

    @Override
    public void onDeath() {
        getGameContext().setLoser(gameObject.getMaster());
    }

    public PlayerHealthComponent(GameObject gameObject) {
        super(gameObject, MAX_HEALTH, 0);

        if (gameObject.getMaster() == Master.LeftPlayer) {
            playerData = getGameContext().getGameSessionData().leftPlayerData;
        }
        if (gameObject.getMaster() == Master.RightPlayer) {
            playerData = getGameContext().getGameSessionData().rightPlayerData;
        }
    }
}
