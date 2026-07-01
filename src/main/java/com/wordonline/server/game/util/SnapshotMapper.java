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
                List.copyOf(g.getEffects()),
                List.copyOf(g.getGizmos()),
                GaugeExtractor.extractGaugeDto(g)
        );
    }
}
