package com.wordonline.server.game.domain.status;
import com.wordonline.server.game.dto.Effect;

import lombok.Getter;
import lombok.Setter;

public class StatusEffect {
    @Getter
    private final Effect type;
    @Setter
    private float remaining;

    public StatusEffect(Effect type, float duration) {
        this.type = type;
        this.remaining = duration;
    }

    public void tick(float dt) {
        remaining -= dt;
    }

    public boolean isExpired() {
        return remaining <= 0;
    }
}