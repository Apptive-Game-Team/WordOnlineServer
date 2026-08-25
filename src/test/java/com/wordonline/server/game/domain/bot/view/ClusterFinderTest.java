package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ClusterFinderTest {

    private static final double RADIUS = 2.5;
    private static final int MIN_MOBS = 3;

    @Test
    void aimsAtTheCentreOfACrowd() {
        List<ObservedObject> mobs = List.of(
                mob(1, new Vector3(5, 0, 4)),
                mob(2, new Vector3(5, 0, 5)),
                mob(3, new Vector3(5, 0, 6)));

        List<ObjectCluster> clusters = ClusterFinder.find(mobs, RADIUS, MIN_MOBS);

        // One cluster, not three: every one of the mobs finds the same crowd as anchor.
        assertThat(clusters).hasSize(1);
        ObjectCluster cluster = clusters.getFirst();
        assertThat(cluster.center()).isEqualTo(new Vector3(5, 0, 5));
        assertThat(cluster.size()).isEqualTo(3);
        assertThat(cluster.radius()).isEqualTo(1.0f);
        assertThat(cluster.totalHp()).isEqualTo(30);
    }

    // The anchor stands at the edge of its own group, so the first centroid leans towards it. The
    // second pass is what pulls the aim point back into the crowd, and it changes both the members
    // and the centre - a one-pass search would answer x=1 with three members here.
    @Test
    void reCentringPicksUpBodiesTheAnchorCouldNotReach() {
        List<ObservedObject> mobs = List.of(
                mob(1, new Vector3(0, 0, 0)),
                mob(2, new Vector3(1, 0, 0)),
                mob(3, new Vector3(2, 0, 0)),
                mob(4, new Vector3(3, 0, 0)));

        List<ObjectCluster> clusters = ClusterFinder.find(mobs, RADIUS, MIN_MOBS);

        ObjectCluster fromFirstAnchor = clusters.getFirst();
        assertThat(fromFirstAnchor.center()).isEqualTo(new Vector3(1.5f, 0, 0));
        assertThat(fromFirstAnchor.members()).extracting(ObservedObject::id).containsExactly(1, 2, 3, 4);
    }

    @Test
    void aPairIsNotACrowd() {
        List<ObservedObject> mobs = List.of(
                mob(1, new Vector3(5, 0, 5)),
                mob(2, new Vector3(5, 0, 6)));

        assertThat(ClusterFinder.find(mobs, RADIUS, MIN_MOBS)).isEmpty();
    }

    @Test
    void aLoneObjectIsNotACluster() {
        assertThat(ClusterFinder.find(List.of(mob(1, new Vector3(5, 0, 5))), RADIUS, MIN_MOBS)).isEmpty();
    }

    @Test
    void bodiesScatteredWiderThanTheRadiusFormNoCluster() {
        List<ObservedObject> mobs = List.of(
                mob(1, new Vector3(0, 0, 0)),
                mob(2, new Vector3(9, 0, 0)),
                mob(3, new Vector3(18, 0, 0)));

        assertThat(ClusterFinder.find(mobs, RADIUS, MIN_MOBS)).isEmpty();
    }

    // The aim point of a flying crowd is where it flies, so height is averaged like the ground axes.
    @Test
    void averagesHeightAsWellAsTheGroundAxes() {
        List<ObservedObject> mobs = List.of(
                mob(1, new Vector3(5, 0, 5)),
                mob(2, new Vector3(5, 1, 5)),
                mob(3, new Vector3(5, 2, 5)));

        assertThat(ClusterFinder.find(mobs, RADIUS, MIN_MOBS).getFirst().center())
                .isEqualTo(new Vector3(5, 1, 5));
    }

    private static ObservedObject mob(int id, Vector3 position) {
        return new ObservedObject(id, Master.RightPlayer, PrefabType.WaterSlime, position,
                Status.Idle, 10, true, true, Set.of(), new Vector3(0, 0, 0));
    }
}
