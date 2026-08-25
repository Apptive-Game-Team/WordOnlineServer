package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;

public class EdgeCollider extends Collider {

    // related position
    private final Vector3 relatedPoint1, relatedPoint2;

    // Absolute endpoints, rebuilt only when the owner moves. GameObject.setPosition replaces
    // the Vector3 instance rather than mutating it, and no code mutates a Vector3 that is
    // already installed as an object's position, so comparing the current position by
    // identity is both correct and cheap. Map walls never move, so this rebuilds once.
    //
    // CONSTRAINT: getPoint1 and getPoint2 hand out the cached instances, not copies, and
    // Vector3 is mutable. Every caller must treat a returned point as read-only. All
    // current callers do - ccw, segmentsIntersect and squaredDistanceToPoint only read
    // components, and getProjection builds new vectors. A caller that needs to mutate a
    // point must copy it first with new Vector3(point).
    private Vector3 cachedPositionReference;
    private Vector3 cachedPoint1, cachedPoint2;

    public Vector3 getPoint1() {
        refreshAbsolutePoints();
        return cachedPoint1;
    }

    public Vector3 getPoint2() {
        refreshAbsolutePoints();
        return cachedPoint2;
    }

    private void refreshAbsolutePoints() {
        Vector3 position = getPosition();
        if (position == cachedPositionReference) {
            return;
        }
        // equivalent to relatedPoint.plus(position.grounded()), without the throwaway vector
        cachedPoint1 = relatedPoint1.plus(position.getX(), 0f, position.getZ());
        cachedPoint2 = relatedPoint2.plus(position.getX(), 0f, position.getZ());
        cachedPositionReference = position;
    }


    public EdgeCollider(GameObject gameObject, Vector3 point1, Vector3 point2, boolean isTrigger) {
        super(gameObject, isTrigger);
        this.relatedPoint1 = point1;
        this.relatedPoint2 = point2;
    }

    private boolean segmentsIntersect(Vector3 p1, Vector3 p2, Vector3 q1, Vector3 q2) {
        return (ccw(p1, q1, q2) != ccw(p2, q1, q2)) &&
                (ccw(p1, p2, q1) != ccw(p1, p2, q2));
    }

    private boolean ccw(Vector3 a, Vector3 b, Vector3 c) {
        return (c.getZ() - a.getZ()) * (b.getX() - a.getX()) >
                (b.getZ() - a.getZ()) * (c.getX() - a.getX());
    }

    @Override
    public boolean isCollidingWish(Colliderable collider) {
        if (collider instanceof EdgeCollider edgeCollider) {
            return segmentsIntersect(getPoint1(), getPoint2(), edgeCollider.getPoint1(), edgeCollider.getPoint2());
        }

        if (collider instanceof CircleCollider circleCollider) {
            return isCircleColliding(circleCollider);
        }

        return false;
    }

    @Override
    public Vector3 getDisplacement(Collider collider) {
        if (collider instanceof CircleCollider circleCollider) {
            Vector3 projection = getProjection(
                    circleCollider.getPosition(), getPoint1(), getPoint2()
            );

            return projection.subtract(collider.getPosition());
        }

        return null;
    }

    private boolean isCircleColliding(CircleCollider circle) {
        float radius = circle.getRadius();

        // 선분과 원 중심 사이 최소 거리의 제곱 구하기
        float distanceSquared = squaredDistanceToPoint(circle.getPosition());

        return distanceSquared <= radius * radius;
    }

    private Vector3 getProjection(Vector3 center, Vector3 point1, Vector3 point2) {
        Vector3 ab = point2.subtract(point1);
        Vector3 ap = center.subtract(point1);

        double abLengthSquared = ab.dot(ab);
        if (abLengthSquared == 0) return point1;

        double t = ap.dot(ab) / abLengthSquared;
        t = Math.max(0, Math.min(1, t));  // t는 0~1 사이로 제한

        return point1.plus(ab.multiply((float)t));
    }

    // 점과 선분 사이 최소 거리의 제곱 계산 함수
    //
    // Scalar float arithmetic: the vector form allocated eight Vector3 per test, and the
    // wall runs one test per non-wall object per edge every frame.
    //
    // The y term is kept on purpose. The endpoints are grounded, so an airborne circle
    // measures as further from the wall than its ground projection is, and airborne objects
    // pass through walls today. Dropping the y term would start colliding them.
    private float squaredDistanceToPoint(Vector3 center) {
        Vector3 point1 = getPoint1();
        Vector3 point2 = getPoint2();

        float abX = point2.getX() - point1.getX();
        float abY = point2.getY() - point1.getY();
        float abZ = point2.getZ() - point1.getZ();

        float apX = center.getX() - point1.getX();
        float apY = center.getY() - point1.getY();
        float apZ = center.getZ() - point1.getZ();

        float abLengthSquared = abX * abX + abY * abY + abZ * abZ;

        // a degenerate edge collapses to its first endpoint, as getProjection does
        float t = 0f;
        if (abLengthSquared != 0f) {
            t = (apX * abX + apY * abY + apZ * abZ) / abLengthSquared;
            t = Math.clamp(t, 0f, 1f);
        }

        float dx = apX - abX * t;
        float dy = apY - abY * t;
        float dz = apZ - abZ * t;

        return dx * dx + dy * dy + dz * dz;
    }
}
