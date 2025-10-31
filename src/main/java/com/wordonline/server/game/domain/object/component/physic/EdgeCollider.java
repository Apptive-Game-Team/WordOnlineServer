package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;

public class EdgeCollider extends Collider {

    // related position
    private final Vector3 relatedPoint1, relatedPoint2;

    public Vector3 getPoint1() {
        return relatedPoint1.plus(getPosition().toVector2());
    }

    public Vector3 getPoint2() {
        return relatedPoint2.plus(getPosition().toVector2());
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
        return (c.getY() - a.getY()) * (b.getX() - a.getX()) >
                (b.getY() - a.getY()) * (c.getX() - a.getX());
    }

    @Override
    public boolean isCollidingWish(Collider collider) {
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
        Vector3 center = circle.getPosition();
        double radius = circle.getRadius();

        // 선분과 원 중심 사이 최소 거리 구하기
        double dist = distancePointToSegment(center, getPoint1(), getPoint2());

        return dist <= radius;
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

    // 점과 선분 사이 최소 거리 계산 함수
    private double distancePointToSegment(Vector3 center, Vector3 point1, Vector3 point2) {
        Vector3 projection = getProjection(center, point1, point2);

        return center.distance(projection);
    }
}
