package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;

public class EdgeCollider extends Collider {

    // related position
    private final Vector2 relatedPoint1, relatedPoint2;

    public Vector2 getPoint1() {
        return relatedPoint1.plus(getPosition().toVector2());
    }

    public Vector2 getPoint2() {
        return relatedPoint2.plus(getPosition().toVector2());
    }


    public EdgeCollider(GameObject gameObject, Vector2 point1, Vector2 point2, boolean isTrigger) {
        super(gameObject, isTrigger);
        this.relatedPoint1 = point1;
        this.relatedPoint2 = point2;
    }

    private boolean segmentsIntersect(Vector2 p1, Vector2 p2, Vector2 q1, Vector2 q2) {
        return (ccw(p1, q1, q2) != ccw(p2, q1, q2)) &&
                (ccw(p1, p2, q1) != ccw(p1, p2, q2));
    }

    private boolean ccw(Vector2 a, Vector2 b, Vector2 c) {
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
    public Vector2 getDisplacement(Collider collider) {
        if (collider instanceof CircleCollider circleCollider) {
            Vector2 projection = getProjection(
                    circleCollider.getPosition().toVector2(), getPoint1(), getPoint2()
            );

            return projection.subtract(collider.getPosition().toVector2());
        }

        return null;
    }

    private boolean isCircleColliding(CircleCollider circle) {
        Vector2 center = circle.getPosition().toVector2();
        double radius = circle.getRadius();

        // 선분과 원 중심 사이 최소 거리 구하기
        double dist = distancePointToSegment(center, getPoint1(), getPoint2());

        return dist <= radius;
    }

    private Vector2 getProjection(Vector2 center, Vector2 point1, Vector2 point2) {
        Vector2 ab = point2.subtract(point1);
        Vector2 ap = center.subtract(point1);

        double abLengthSquared = ab.dot(ab);
        if (abLengthSquared == 0) return point1;

        double t = ap.dot(ab) / abLengthSquared;
        t = Math.max(0, Math.min(1, t));  // t는 0~1 사이로 제한

        return point1.plus(ab.multiply((float)t));
    }

    // 점과 선분 사이 최소 거리 계산 함수
    private double distancePointToSegment(Vector2 center, Vector2 point1, Vector2 point2) {
        Vector2 projection = getProjection(center, point1, point2);

        return center.distance(projection);
    }
}
