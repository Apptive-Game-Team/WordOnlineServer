package com.wordonline.server.game.domain.pve;

import java.util.List;

public record PveDialogue(
        String key,
        List<String> lines
) {
}
