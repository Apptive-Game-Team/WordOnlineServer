package com.wordonline.server.game.dto.pve;

import java.util.List;

public record PveScriptEventDto(
        String key,
        List<String> lines
) {
}
