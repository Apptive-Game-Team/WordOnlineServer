package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

import java.util.List;

/**
 * Where a rule puts its spell, expressed against whatever the rule's conditions found.
 *
 * <p>The implementations are nested because the whole vocabulary of "at" is four short answers and
 * reads better in one place. Reach is not one of them: an aim says where the tactic wants to cast,
 * and the engine drops any aim the chosen recipe cannot reach. Keeping the range check out of here
 * means every aim gets it, including the ones added later.
 */
public sealed interface AimSpec {

    /**
     * @return one aim per thing worth casting at, possibly none
     */
    List<Aim> resolve(ConditionContext context, ConditionMatch match);

    /** At each object the conditions found: the seed spirit itself, the attacker itself. */
    record MatchedTarget() implements AimSpec {
        @Override
        public List<Aim> resolve(ConditionContext context, ConditionMatch match) {
            return match.objects().stream()
                    .map(object -> Aim.at(object, context.view()))
                    .toList();
        }
    }

    /** At the middle of each group the conditions found. */
    record ClusterCenter() implements AimSpec {
        @Override
        public List<Aim> resolve(ConditionContext context, ConditionMatch match) {
            return match.clusters().stream()
                    .map(Aim::at)
                    .toList();
        }
    }

    /** On top of the bot, for a spell that defends the position rather than reaching out. */
    record SelfPosition() implements AimSpec {
        @Override
        public List<Aim> resolve(ConditionContext context, ConditionMatch match) {
            return List.of(Aim.at(context.selfPosition(), "its own position"));
        }
    }

    /**
     * A fixed distance in front of the bot, towards the enemy side, for a spell that wants to be
     * put down between the bot and whatever is coming.
     */
    record ForwardOffset(double distance) implements AimSpec {
        @Override
        public List<Aim> resolve(ConditionContext context, ConditionMatch match) {
            float step = context.view().botSide() == Master.RightPlayer
                    ? (float) -distance
                    : (float) distance;
            Vector3 position = context.selfPosition().plus(step, 0, 0);
            return List.of(Aim.at(position, "a point " + distance + " ahead of it"));
        }
    }
}
