package com.wordonline.server.game.domain.object.component.physic;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.CollisionChecker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EdgeColliderTest {

    private static final float UNIT_RADIUS = 0.5f;

    private GameObject wallAtOrigin(GameContext gameContext) {
        return new GameObject(Master.None, PrefabType.Wall, new Vector3(0f, 0f, 0f), gameContext);
    }

    // the left map edge, running along x = 0
    private EdgeCollider leftEdge(GameObject wall) {
        EdgeCollider edgeCollider = new EdgeCollider(
                wall,
                new Vector3(0f, 0f, 0f),
                new Vector3(0f, 0f, GameConfig.HEIGHT),
                false);
        wall.addCollider(edgeCollider);
        return edgeCollider;
    }

    @Test
    void cachedEndpointsAreReusedWhileTheOwnerStaysPut() {
        GameContext gameContext = mock(GameContext.class);
        EdgeCollider edgeCollider = leftEdge(wallAtOrigin(gameContext));

        Vector3 firstPoint1 = edgeCollider.getPoint1();
        Vector3 firstPoint2 = edgeCollider.getPoint2();

        assertThat(edgeCollider.getPoint1()).isSameAs(firstPoint1);
        assertThat(edgeCollider.getPoint2()).isSameAs(firstPoint2);
    }

    @Test
    void cachedEndpointsAreRebuiltWhenTheOwnerMoves() {
        GameContext gameContext = mock(GameContext.class);
        GameObject wall = wallAtOrigin(gameContext);
        EdgeCollider edgeCollider = leftEdge(wall);

        assertThat(edgeCollider.getPoint1()).isEqualTo(new Vector3(0f, 0f, 0f));
        assertThat(edgeCollider.getPoint2()).isEqualTo(new Vector3(0f, 0f, GameConfig.HEIGHT));

        wall.setPosition(new Vector3(3f, 0f, 2f));

        assertThat(wall.isDestroyed()).isFalse();
        assertThat(edgeCollider.getPoint1()).isEqualTo(new Vector3(3f, 0f, 2f));
        assertThat(edgeCollider.getPoint2()).isEqualTo(new Vector3(3f, 0f, GameConfig.HEIGHT + 2f));
    }

    // the owner's height must not leak into the endpoints: they stay grounded, which is what
    // makes an airborne object measure as further away than its ground projection
    @Test
    void cachedEndpointsIgnoreTheOwnerHeight() {
        GameContext gameContext = mock(GameContext.class);
        GameObject wall = wallAtOrigin(gameContext);
        EdgeCollider edgeCollider = leftEdge(wall);

        wall.setPosition(new Vector3(1f, 4f, 0f));

        assertThat(edgeCollider.getPoint1().getY()).isZero();
        assertThat(edgeCollider.getPoint2().getY()).isZero();
    }

    // the distance test keeps its y term, so an object flying over the map edge still passes
    // through it exactly as it did before the rewrite
    @Test
    void airborneObjectDoesNotCollideWithTheWallItWouldHitOnTheGround() {
        GameContext gameContext = mock(GameContext.class);
        EdgeCollider edgeCollider = leftEdge(wallAtOrigin(gameContext));

        GameObject grounded = new GameObject(
                Master.LeftPlayer, PrefabType.ZapMouse, new Vector3(0f, 0f, 5f), gameContext);
        CircleCollider groundedCollider = new CircleCollider(grounded, UNIT_RADIUS, false);

        GameObject airborne = new GameObject(
                Master.LeftPlayer, PrefabType.ZapMouse, new Vector3(0f, 2f, 5f), gameContext);
        CircleCollider airborneCollider = new CircleCollider(airborne, UNIT_RADIUS, false);

        // same ground projection: straight on the edge
        assertThat(edgeCollider.isCollidingWish(groundedCollider)).isTrue();
        assertThat(edgeCollider.isCollidingWish(airborneCollider)).isFalse();

        // and the same verdict through the circle's own dispatch
        assertThat(groundedCollider.isCollidingWish(edgeCollider)).isTrue();
        assertThat(airborneCollider.isCollidingWish(edgeCollider)).isFalse();
    }

    @Test
    void airborneObjectWithinRadiusOfTheEdgePlaneStillCollides() {
        GameContext gameContext = mock(GameContext.class);
        GameObject wall = wallAtOrigin(gameContext);
        EdgeCollider edgeCollider = leftEdge(wall);

        GameObject lowFlyer = new GameObject(
                Master.LeftPlayer, PrefabType.ZapMouse, new Vector3(0f, 0.25f, 5f), gameContext);
        lowFlyer.addCollider(new CircleCollider(lowFlyer, UNIT_RADIUS, false));

        assertThat(CollisionChecker.isColliding(lowFlyer, wall)).isTrue();
    }
}
