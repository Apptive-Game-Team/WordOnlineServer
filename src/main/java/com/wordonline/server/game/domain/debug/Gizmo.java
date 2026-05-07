package com.wordonline.server.game.domain.debug;

import com.wordonline.server.game.domain.object.Vector3;

public record Gizmo(
        Vector3 relativePosition,
        float radius,
        Vector3 boxSize,
        GizmoType type,
        GizmoCategory category
) {
    public static Gizmo circle(Vector3 relativePosition, float radius, GizmoCategory category) {
        return new Gizmo(relativePosition, radius, null, GizmoType.Circle, category);
    }

    public static Gizmo box(Vector3 relativePosition, Vector3 boxSize, GizmoCategory category) {
        return new Gizmo(relativePosition, 0f, boxSize, GizmoType.Box, category);
    }
}
