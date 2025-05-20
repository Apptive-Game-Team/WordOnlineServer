package com.wordonline.server.game.domain.object.component.effect;

import com.wordonline.server.game.dto.Effect;

public interface EffectReceiver {
    public void onReceive(Effect effect);
}
