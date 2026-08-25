package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.GaugeComponent;
import com.wordonline.server.game.dto.frame.GaugeDto;
import com.wordonline.server.game.dto.frame.SnapshotObjectDto;

import java.util.ArrayList;
import java.util.List;

public final class SnapshotMapper {

    public static SnapshotObjectDto toDto(GameObject g) {
        var p = g.getPosition();

        return new SnapshotObjectDto(
                g.getId(),
                g.getType().name(),
                p.getX(), p.getY(), p.getZ(),
                g.getMaster().toString(),
                g.getStatus(),
                copyOrEmpty(g.getEffects()),
                copyOrEmpty(g.getGizmos()),
                GaugeExtractor.extractGaugeDto(g)
        );
    }

    // effects and gizmos are usually empty and ArrayList.toArray() allocates even then,
    // so hand back the shared empty list instead of copying.
    private static <T> List<T> copyOrEmpty(List<T> source) {
        return source.isEmpty() ? List.of() : List.copyOf(source);
    }
}
