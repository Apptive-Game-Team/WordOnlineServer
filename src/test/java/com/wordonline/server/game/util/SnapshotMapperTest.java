package com.wordonline.server.game.util;

import com.wordonline.server.game.domain.debug.Gizmo;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.GaugeComponent;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.dto.frame.SnapshotObjectDto;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SnapshotMapperTest {

    private GameObject gameObject(List<Effect> effects, List<Gizmo> gizmos) {
        GameObject gameObject = mock(GameObject.class);
        when(gameObject.getId()).thenReturn(42);
        when(gameObject.getType()).thenReturn(PrefabType.FireShot);
        when(gameObject.getPosition()).thenReturn(new Vector3(1f, 2f, 3f));
        when(gameObject.getMaster()).thenReturn(Master.LeftPlayer);
        when(gameObject.getStatus()).thenReturn(Status.Move);
        when(gameObject.getEffects()).thenReturn(effects);
        when(gameObject.getGizmos()).thenReturn(gizmos);
        when(gameObject.getComponents(GaugeComponent.class)).thenReturn(List.of());
        return gameObject;
    }

    @Test
    void emptyEffectsAndGizmosMapToTheSharedEmptyList() {
        SnapshotObjectDto dto = SnapshotMapper.toDto(gameObject(new ArrayList<>(), new ArrayList<>()));

        assertThat(dto.id()).isEqualTo(42);
        assertThat(dto.prefab()).isEqualTo("FireShot");
        assertThat(dto.x()).isEqualTo(1f);
        assertThat(dto.y()).isEqualTo(2f);
        assertThat(dto.z()).isEqualTo(3f);
        assertThat(dto.master()).isEqualTo("LeftPlayer");
        assertThat(dto.status()).isEqualTo(Status.Move);
        assertThat(dto.effects()).isSameAs(List.of());
        assertThat(dto.gizmos()).isSameAs(List.of());
        assertThat(dto.gauges()).isEmpty();
    }

    @Test
    void nonEmptyEffectsAndGizmosAreSnapshottedAndDoNotAliasTheLiveLists() {
        List<Effect> effects = new ArrayList<>(List.of(Effect.Burn));
        List<Gizmo> gizmos = new ArrayList<>(List.of(Gizmo.circle(Vector3.ZERO, 1f, GizmoCategory.Collider)));

        SnapshotObjectDto dto = SnapshotMapper.toDto(gameObject(effects, gizmos));

        assertThat(dto.effects()).containsExactly(Effect.Burn);
        assertThat(dto.gizmos()).containsExactlyElementsOf(gizmos);

        effects.clear();
        gizmos.clear();

        assertThat(dto.effects()).containsExactly(Effect.Burn);
        assertThat(dto.gizmos()).hasSize(1);
    }
}
