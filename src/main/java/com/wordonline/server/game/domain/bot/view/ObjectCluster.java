package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.object.Vector3;

import java.util.List;

/**
 * A group of objects standing close enough together that one area spell reaches all of them.
 *
 * @param members  the grouped objects, never empty
 * @param center   centroid of the members, the natural aim point for an area spell
 * @param radius   distance from the centre to the furthest member
 * @param totalHp  summed hit points, so a rule can avoid committing a big spell to dying chaff
 */
public record ObjectCluster(List<ObservedObject> members, Vector3 center, float radius, int totalHp) {

    public int size() {
        return members.size();
    }

    public double distanceFrom(Vector3 point) {
        return center.distance(point);
    }
}
