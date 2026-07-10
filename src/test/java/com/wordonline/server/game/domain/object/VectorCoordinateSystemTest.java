package com.wordonline.server.game.domain.object;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VectorCoordinateSystemTest {

    @Test
    void groundedDropsHeightAndKeepsXzPlane() {
        Vector3 position = new Vector3(3f, 7f, 11f).grounded();

        assertThat(position.getX()).isEqualTo(3f);
        assertThat(position.getY()).isEqualTo(0f);
        assertThat(position.getZ()).isEqualTo(11f);
    }

    @Test
    void withYChangesHeightOnly() {
        Vector3 position = new Vector3(3f, 0f, 11f).withY(7f);

        assertThat(position.getX()).isEqualTo(3f);
        assertThat(position.getY()).isEqualTo(7f);
        assertThat(position.getZ()).isEqualTo(11f);
    }

    @Test
    void directionConstantsMatchUnityYUpConvention() {
        assertThat(Vector3.FORWARD).isEqualTo(new Vector3(0f, 0f, 1f));
        assertThat(Vector3.BACK).isEqualTo(new Vector3(0f, 0f, -1f));
        assertThat(Vector3.UP).isEqualTo(new Vector3(0f, 1f, 0f));
        assertThat(Vector3.DOWN).isEqualTo(new Vector3(0f, -1f, 0f));
    }
}
