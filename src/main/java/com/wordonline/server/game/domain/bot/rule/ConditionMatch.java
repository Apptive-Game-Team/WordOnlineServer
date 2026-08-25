package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.ObjectCluster;
import com.wordonline.server.game.domain.bot.view.ObservedObject;

import java.util.ArrayList;
import java.util.List;

/**
 * What a condition found, not merely that it found something.
 *
 * <p>This is the whole reason {@link BotCondition} does not return a boolean. "A seed spirit is
 * nearby" is useless to an action; "this seed spirit, at this position, is nearby" is what lets the
 * next clause say "cast at it". A boolean would force every aim to re-run the search the condition
 * just did, and the two searches would drift apart the first time either changed.
 *
 * <p>A condition that is about the bot rather than the field - mana, pressure, what is in hand -
 * matches with {@link #empty()}: true, but with nothing to aim at.
 *
 * @param objects  matching objects, in the order the condition ranked them
 * @param clusters matching groups, in the order the condition ranked them
 */
public record ConditionMatch(List<ObservedObject> objects, List<ObjectCluster> clusters) {

    private static final ConditionMatch EMPTY = new ConditionMatch(List.of(), List.of());

    public ConditionMatch {
        objects = List.copyOf(objects);
        clusters = List.copyOf(clusters);
    }

    /** Matched, with nothing to aim at. */
    public static ConditionMatch empty() {
        return EMPTY;
    }

    public static ConditionMatch ofObjects(List<ObservedObject> objects) {
        return new ConditionMatch(objects, List.of());
    }

    public static ConditionMatch ofClusters(List<ObjectCluster> clusters) {
        return new ConditionMatch(List.of(), clusters);
    }

    /**
     * Both sets of subjects, in this match's order first. Used by the and/or combinators: a rule
     * that names several subjects gets to aim at all of them, and duplicates are dropped so a
     * subject named twice does not become two identical casts.
     */
    public ConditionMatch merge(ConditionMatch other) {
        if (other == null || other == EMPTY) {
            return this;
        }
        if (this == EMPTY) {
            return other;
        }
        return new ConditionMatch(union(objects, other.objects), union(clusters, other.clusters));
    }

    public boolean hasSubjects() {
        return !objects.isEmpty() || !clusters.isEmpty();
    }

    private static <T> List<T> union(List<T> first, List<T> second) {
        List<T> merged = new ArrayList<>(first);
        for (T element : second) {
            if (!merged.contains(element)) {
                merged.add(element);
            }
        }
        return merged;
    }
}
