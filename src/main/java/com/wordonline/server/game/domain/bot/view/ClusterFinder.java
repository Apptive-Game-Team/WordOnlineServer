package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.object.Vector3;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Finds groups of objects standing close enough together that one area spell reaches all of them.
 *
 * <p>The search is the two-pass one the brain has always used: every object is tried as an anchor,
 * everything within the radius of that anchor is gathered, and the group is then re-selected around
 * its own centroid. The second pass matters. The anchor sits at the edge of its own group, so the
 * first centroid leans towards the anchor; re-selecting around that centroid picks up bodies the
 * anchor could not reach and drops ones that only the anchor was close to, which is what makes the
 * aim point the centre of the crowd rather than the centre of whatever the anchor happened to see.
 *
 * <p>Unlike the brain's old private version this returns every distinct cluster instead of the
 * single best one: which cluster is worth a spell depends on the spell, so the choice belongs to
 * the rule that is casting. Ordering is the order of the anchors that produced them, so the same
 * input list always gives the same output - nothing here iterates a hash container.
 */
public final class ClusterFinder {

    private ClusterFinder() {
        // Utility class - prevent instantiation
    }

    /**
     * @param objects     the candidates to group; the caller decides what belongs in here, for
     *                    example only targetable enemy mobs
     * @param radius      how far from the centre a member may stand
     * @param minimumSize the smallest group worth calling a cluster
     * @return every distinct cluster found, in anchor order, never null
     */
    public static List<ObjectCluster> find(List<ObservedObject> objects, double radius, int minimumSize) {
        int required = Math.max(1, minimumSize);
        if (objects.size() < required) {
            return List.of();
        }

        // Keyed by the member ids so the same crowd found from three different anchors is reported
        // once. Insertion ordered, so the result order follows the input order.
        Map<List<Integer>, ObjectCluster> found = new LinkedHashMap<>();
        for (ObservedObject anchor : objects) {
            List<ObservedObject> group = within(objects, anchor.position(), radius);
            if (group.size() < required) {
                continue;
            }

            group = within(objects, averagePosition(group), radius);
            if (group.size() < required) {
                continue;
            }

            found.putIfAbsent(memberIds(group), toCluster(group));
        }
        return List.copyOf(found.values());
    }

    private static List<ObservedObject> within(List<ObservedObject> objects, Vector3 center, double radius) {
        List<ObservedObject> group = new ArrayList<>();
        for (ObservedObject object : objects) {
            if (object.position().distance(center) <= radius) {
                group.add(object);
            }
        }
        return group;
    }

    private static ObjectCluster toCluster(List<ObservedObject> members) {
        Vector3 center = averagePosition(members);
        double furthest = 0;
        int totalHp = 0;
        for (ObservedObject member : members) {
            furthest = Math.max(furthest, member.position().distance(center));
            totalHp += member.hp();
        }
        return new ObjectCluster(List.copyOf(members), center, (float) furthest, totalHp);
    }

    /** Averages x, y and z: a cluster of aerial mobs is aimed at where they fly, not at the ground. */
    private static Vector3 averagePosition(List<ObservedObject> objects) {
        float x = 0;
        float y = 0;
        float z = 0;
        for (ObservedObject object : objects) {
            x += object.position().getX();
            y += object.position().getY();
            z += object.position().getZ();
        }
        float count = objects.size();
        return new Vector3(x / count, y / count, z / count);
    }

    private static List<Integer> memberIds(List<ObservedObject> members) {
        return members.stream().map(ObservedObject::id).toList();
    }
}
