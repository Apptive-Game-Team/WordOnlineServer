package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;

import java.util.Set;

/**
 * One game object as the bot understands it this think pass: the frozen frame snapshot plus the
 * things a rule wants to ask about that a single frame cannot answer on its own.
 *
 * <p>{@code velocity} is estimated by differencing this frame's position against the previous
 * think pass, not read from {@code RigidBody}: the physics system clears the live velocity every
 * frame after integrating it, so a snapshot of it is almost always zero.
 *
 * @param id         game object id, stable across frames, which is what makes the diff possible
 * @param master     owning side, or {@code None}
 * @param type       prefab template
 * @param position   position at observation time
 * @param status     action state at observation time, so a rule can ask whether it is attacking
 * @param hp         remaining hit points
 * @param mob        whether the object carries a mob component, so it counts as a body on the field
 * @param targetable whether the object can still be hit
 * @param tags       gameplay tags of the prefab, shared vocabulary with the counter rules
 * @param velocity   units per second, estimated from the previous think pass; zero when unknown
 */
public record ObservedObject(
        int id,
        Master master,
        PrefabType type,
        Vector3 position,
        Status status,
        int hp,
        boolean mob,
        boolean targetable,
        Set<String> tags,
        Vector3 velocity
) {

    public boolean isAttacking() {
        return status == Status.Attack;
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    public double distanceTo(Vector3 point) {
        return position.distance(point);
    }

    /**
     * How fast the object is closing on {@code point}, in units per second. Negative when it is
     * moving away, zero when it is not moving or sits exactly on the point.
     *
     * <p>This is the component of the velocity along the direction to the point, so an object
     * running past the bot counts far less than one running straight at it.
     */
    public double approachSpeedToward(Vector3 point) {
        double distance = position.distance(point);
        if (distance == 0) {
            return 0;
        }
        return velocity.dot(point.subtract(position)) / distance;
    }
}
