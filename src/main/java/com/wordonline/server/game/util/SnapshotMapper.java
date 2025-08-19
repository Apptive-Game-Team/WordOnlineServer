package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.frame.SnapshotObjectDto;

//public final class SnapshotMapper {
//    public static SnapshotObjectDto toDto(GameObject g) {
//        var p = g.getPosition();
//        return new SnapshotObjectDto(
//                g.getId(),
//                g.getType().name(),
//                p.getX(), p.getY(),
//                g.getStatus().name()),
//                g.getEffect() == null ? null : g.getEffect().name()
//        );
//    }
//}