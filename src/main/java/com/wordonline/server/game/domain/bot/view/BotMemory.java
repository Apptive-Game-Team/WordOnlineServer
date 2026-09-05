package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.game.domain.bot.BotVisibleObject;
import com.wordonline.server.game.domain.object.Vector3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * What one bot remembers between think passes: where every object stood last time, so this time it
 * can work out how fast each of them is moving and which way.
 *
 * <p>The velocity has to be estimated this way. {@code RigidBody} clears its velocity every frame
 * once it has been integrated into the position, so the value on a live object is essentially
 * always zero and a frame snapshot of it says nothing. Differencing two positions over the time
 * between them is the only reading that survives the snapshot.
 *
 * <p>One instance belongs to one bot agent. It is only ever touched from the bot executor thread,
 * and {@code BotAgentSystem} gates re-entry with a compare-and-set so two think passes for the same
 * bot never overlap. Plain fields are therefore correct here; do not add synchronization for a
 * contention that cannot happen.
 */
public final class BotMemory {

    private static final double MILLIS_PER_SECOND = 1000.0;

    private final Map<Integer, Vector3> lastPositions = new HashMap<>();
    private Map<Integer, Vector3> velocities = Map.of();

    /** Null until the first observation, which is what makes a first sighting stand still. */
    private Long lastObservedAtMillis;

    /**
     * Records where everything is now and estimates how fast it is moving from where it was.
     *
     * <p>Objects that were not in this pass are forgotten, so a long session does not accumulate
     * one entry per body that ever stood on the field.
     *
     * @param objects        everything seen this pass
     * @param observedAtMillis when the pass was observed. Passed in rather than read from the clock
     *                         so the estimate can be reproduced exactly in a test.
     */
    public void observe(List<BotVisibleObject> objects, long observedAtMillis) {
        double elapsedSeconds = lastObservedAtMillis == null
                ? 0
                : (observedAtMillis - lastObservedAtMillis) / MILLIS_PER_SECOND;

        Map<Integer, Vector3> currentPositions = new HashMap<>();
        Map<Integer, Vector3> currentVelocities = new HashMap<>();
        for (BotVisibleObject object : objects) {
            // Vector3 is mutable and the loop thread owns the live one, so the memory keeps a copy.
            Vector3 position = new Vector3(object.position());
            currentVelocities.put(object.id(), estimate(lastPositions.get(object.id()), position, elapsedSeconds));
            currentPositions.put(object.id(), position);
        }

        lastPositions.clear();
        lastPositions.putAll(currentPositions);
        velocities = currentVelocities;
        lastObservedAtMillis = observedAtMillis;
    }

    /**
     * How fast the object was moving at the last {@link #observe}, in units per second. Zero for an
     * object seen for the first time, and zero for one that is not being tracked at all: an unknown
     * object stands still rather than teleporting in from wherever the caller last looked.
     */
    public Vector3 velocityOf(int gameObjectId) {
        Vector3 velocity = velocities.get(gameObjectId);
        return velocity == null ? zero() : new Vector3(velocity);
    }

    /** Whether the object was present at the last {@link #observe}. */
    public boolean knows(int gameObjectId) {
        return velocities.containsKey(gameObjectId);
    }

    /** How many objects are being remembered, so a leak shows up as a number that keeps climbing. */
    public int trackedCount() {
        return lastPositions.size();
    }

    private static Vector3 estimate(Vector3 previousPosition, Vector3 position, double elapsedSeconds) {
        // A zero or backwards elapsed time - the first pass, a repeated timestamp, a clock that
        // stepped back - divides into an infinity or a reversed reading, so it means "no estimate".
        if (previousPosition == null || elapsedSeconds <= 0) {
            return zero();
        }
        return position.subtract(previousPosition).multiply((float) (1.0 / elapsedSeconds));
    }

    /** Never hand out {@link Vector3#ZERO}: it is a shared instance and Vector3 is mutable. */
    private static Vector3 zero() {
        return new Vector3(0, 0, 0);
    }
}
