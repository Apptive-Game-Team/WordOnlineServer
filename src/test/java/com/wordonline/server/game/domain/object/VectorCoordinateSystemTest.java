package com.wordonline.server.game.domain.object;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VectorCoordinateSystemTest {

    @Test
    void vector2UsesXzGroundPlaneWhenCreatedFromVector3() {
        Vector2 groundPosition = new Vector2(new Vector3(3f, 7f, 11f));

        assertThat(groundPosition.getX()).isEqualTo(3f);
        assertThat(groundPosition.getY()).isEqualTo(11f);
    }

    @Test
    void vector2ConvertsBackToVector3WithYAsHeight() {
        Vector3 position = new Vector2(3f, 11f).toVector3(7f);

        assertThat(position.getX()).isEqualTo(3f);
        assertThat(position.getY()).isEqualTo(7f);
        assertThat(position.getZ()).isEqualTo(11f);
    }

    @Test
    void vector3DistanceToVector2IgnoresHeightAndUsesXzPlane() {
        Vector3 position = new Vector3(3f, 99f, 4f);
        Vector2 other = new Vector2(6f, 8f);

        assertThat(position.distance(other)).isEqualTo(5.0);
    }

    @Test
    void directionConstantsMatchUnityYUpConvention() {
        assertThat(Vector3.FORWARD).isEqualTo(new Vector3(0f, 0f, 1f));
        assertThat(Vector3.BACK).isEqualTo(new Vector3(0f, 0f, -1f));
        assertThat(Vector3.UP).isEqualTo(new Vector3(0f, 1f, 0f));
        assertThat(Vector3.DOWN).isEqualTo(new Vector3(0f, -1f, 0f));
    }
}
