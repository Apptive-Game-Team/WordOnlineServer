package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.bot.view.ObjectCluster;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * "A group of them is bunched up, and the middle of the group is close enough to hit."
 *
 * <p>Groups are ranked biggest-first, then nearest-first, which is the same preference the
 * hand-written cluster tactic applied when it kept a single best cluster. This one keeps them all
 * and lets the engine offer one candidate per group.
 *
 * @param minSize           how many members the group needs
 * @param minTotalHp        summed hit points the group needs, so a spell is not spent on chaff
 * @param memberFilter      counts only members matching this, or every member when {@code null}
 * @param maxCenterDistance furthest the centre may sit from the bot
 * @param castRangeGated    additionally cap that distance at the candidate recipe's cast range
 */
public record ClusterCondition(
        int minSize,
        int minTotalHp,
        TargetFilter memberFilter,
        double maxCenterDistance,
        boolean castRangeGated
) implements BotCondition {

    @Override
    public Optional<ConditionMatch> match(ConditionContext context) {
        double limit = castRangeGated
                ? Math.min(maxCenterDistance, context.castRange())
                : maxCenterDistance;
        List<ObjectCluster> matched = context.view().enemyClusters().stream()
                .filter(cluster -> countedMembers(cluster, context) >= minSize)
                .filter(cluster -> cluster.totalHp() >= minTotalHp)
                .filter(cluster -> cluster.distanceFrom(context.selfPosition()) <= limit)
                .sorted(Comparator
                        .comparingInt(ObjectCluster::size).reversed()
                        .thenComparingDouble(cluster -> cluster.distanceFrom(context.selfPosition())))
                .toList();
        if (matched.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(ConditionMatch.ofClusters(matched));
    }

    private int countedMembers(ObjectCluster cluster, ConditionContext context) {
        if (memberFilter == null) {
            return cluster.size();
        }
        return (int) cluster.members().stream()
                .filter(member -> memberFilter.matches(member, context))
                .count();
    }
}
