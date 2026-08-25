package com.wordonline.server.game.domain.bot.rule;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.bot.BotVisibleObject;
import com.wordonline.server.game.domain.bot.ThreatAssessment;
import com.wordonline.server.game.domain.bot.view.BotWorldView;
import com.wordonline.server.game.domain.bot.view.ObjectCluster;
import com.wordonline.server.game.domain.bot.view.ObservedObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Hand-built worlds for the rule tests. The view records are built directly rather than through the
 * factory that fills them in the running server, so a rule test states exactly the field it is
 * about and nothing else.
 */
final class BotRuleTestFixtures {

    static final Vector3 SELF = new Vector3(1, 0, 5);
    static final Master BOT_SIDE = Master.LeftPlayer;
    static final Master ENEMY_SIDE = Master.RightPlayer;

    private BotRuleTestFixtures() {
    }

    static ObservedObject object(int id, Master master, PrefabType type, Vector3 position) {
        return new ObservedObject(id, master, type, position, Status.Idle, 10, true, true, Set.of(), Vector3.ZERO);
    }

    static ObservedObject object(int id,
                                 Master master,
                                 PrefabType type,
                                 Vector3 position,
                                 Status status,
                                 boolean mob,
                                 boolean targetable,
                                 Set<String> tags,
                                 Vector3 velocity) {
        return new ObservedObject(id, master, type, position, status, 10, mob, targetable, tags, velocity);
    }

    static ObjectCluster cluster(Vector3 center, ObservedObject... members) {
        List<ObservedObject> list = List.of(members);
        float radius = 0;
        int totalHp = 0;
        for (ObservedObject member : list) {
            radius = (float) Math.max(radius, member.distanceTo(center));
            totalHp += member.hp();
        }
        return new ObjectCluster(list, center, radius, totalHp);
    }

    static ViewBuilder view() {
        return new ViewBuilder();
    }

    static RecipeOption option(Magic magic, int cost, double castRange, CardType... recipe) {
        List<CardType> cards = recipe.length == 0
                ? List.of(magic.getMagicType(), CardType.Fire)
                : List.of(recipe);
        return new RecipeOption(cards, magic, cards.getFirst(), cost, castRange, 1.5, 20);
    }

    /** Builds a {@link BotWorldView} whose derived lists always agree with the objects put in it. */
    static final class ViewBuilder {

        private final List<ObservedObject> objects = new ArrayList<>();
        private final List<ObjectCluster> clusters = new ArrayList<>();
        private final List<CardType> hand = new ArrayList<>();
        private final List<BotVisibleObject> threatSources = new ArrayList<>();
        private BotTier tier = BotTier.ELITE;
        private int mana = 10;
        private int enemyPlayerHp = 100;

        ViewBuilder with(ObservedObject... added) {
            objects.addAll(Arrays.asList(added));
            return this;
        }

        ViewBuilder withCluster(ObjectCluster... added) {
            clusters.addAll(Arrays.asList(added));
            return this;
        }

        ViewBuilder hand(CardType... cards) {
            hand.addAll(Arrays.asList(cards));
            return this;
        }

        ViewBuilder tier(BotTier tier) {
            this.tier = tier;
            return this;
        }

        ViewBuilder mana(int mana) {
            this.mana = mana;
            return this;
        }

        /** Puts an enemy body this far from the bot, which is what the threat model reads pressure off. */
        ViewBuilder pressureFrom(double distance) {
            threatSources.add(new BotVisibleObject(
                    900 + threatSources.size(),
                    ENEMY_SIDE,
                    PrefabType.RockGolem,
                    SELF.plus((float) distance, 0, 0),
                    Status.Move,
                    10,
                    true,
                    true));
            return this;
        }

        BotWorldView build() {
            List<ObservedObject> enemies = objects.stream()
                    .filter(object -> object.master() == ENEMY_SIDE && object.targetable())
                    .toList();
            List<ObservedObject> allies = objects.stream()
                    .filter(object -> object.master() == BOT_SIDE && object.targetable())
                    .toList();
            ThreatAssessment threats =
                    ThreatAssessment.observe(threatSources, ENEMY_SIDE, SELF, enemyPlayerHp);
            return new BotWorldView(
                    BOT_SIDE,
                    ENEMY_SIDE,
                    tier,
                    SELF,
                    mana,
                    List.copyOf(hand),
                    List.copyOf(objects),
                    enemies,
                    allies,
                    List.copyOf(clusters),
                    threats,
                    enemyPlayerHp);
        }
    }
}
