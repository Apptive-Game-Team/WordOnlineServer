package com.wordonline.server.game.domain.debug;

import com.wordonline.server.game.domain.object.Vector3;

public record Gizmo(
        Vector3 relativePosition,
        float radius,
        GizmoType type
) {

}

