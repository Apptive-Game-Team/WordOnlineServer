// src/main/java/com/wordonline/server/game/domain/object/component/effect/StatusEffectComponent.java
package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.domain.AttackInfo;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.status.StatusEffect;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.domain.object.component.mob.Mob;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StatusEffectComponent extends Component {
    private final List<StatusEffect> list = new ArrayList<>();

    public StatusEffectComponent(GameObject go) {
        super(go);
    }

    public void apply(Effect type, float duration) {
        for (StatusEffect e : list) {
            if (e.getType() == type) {
                e.setRemaining(duration);
                return;
            }
        }
        list.add(new StatusEffect(type, duration));
        gameObject.setEffect(type);
    }

    @Override
    public void update() {
        float dt = gameObject.getGameLoop().deltaTime;
        Iterator<StatusEffect> it = list.iterator();

        while (it.hasNext()) {
            StatusEffect e = it.next();
            e.tick(dt);

            // ─── 실제 상태이상 효과 적용 ───
            switch (e.getType()) {
                case Burn -> {
                    gameObject.getComponent(Mob.class)
                              .onDamaged(new AttackInfo(1));
                }
                case Wet -> {
                }
                case Shock -> {
                    if (!e.isExpired()) {
                        //stun
                    }
                }
                case Snared -> {
                    //slow
                }
                default -> { }
            }

            if (e.isExpired()) {
                it.remove();
                if (list.isEmpty()) {
                    gameObject.setEffect(Effect.None);
                }
            }
        }
    }

    @Override
    public void start() {
    }

    @Override
    public void onDestroy() {
    }
}
