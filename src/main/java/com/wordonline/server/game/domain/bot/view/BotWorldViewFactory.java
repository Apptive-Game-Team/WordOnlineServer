package com.wordonline.server.game.domain.bot.view;

import com.wordonline.server.bot.domain.BotTier;
import com.wordonline.server.game.domain.bot.BotEye;
import com.wordonline.server.game.domain.bot.BotSideUtil;
import com.wordonline.server.game.domain.bot.BotVisibleObject;
import com.wordonline.server.game.domain.bot.ThreatAssessment;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the frame snapshot the loop thread handed over into the world the rules read.
 *
 * <p>This is the only place that answers "what is out there": it adds to each frozen object the two
 * things one frame cannot say on its own - the tags that give the rules a shared vocabulary with
 * the counter tables, and the velocity that says whether the object is coming at the bot - and then
 * groups the enemy mobs that stand close enough for one area spell.
 *
 * <p>It does not compute pressure or the contested lane. {@link ThreatAssessment} already does
 * that from the same snapshot and the view carries its result, so there is one definition of how
 * hard the bot is being pushed rather than two that can drift apart.
 */
public final class BotWorldViewFactory {

    /** How far apart enemy mobs may stand and still count as one target for an area spell. */
    public static final double DEFAULT_CLUSTER_RADIUS = 2.5;

    /** How many mobs have to be in one place before it is worth aiming an area spell at them. */
    public static final int DEFAULT_CLUSTER_MIN_MOBS = 3;

    private final GameObjectTags gameObjectTags;
    private final double clusterRadius;
    private final int clusterMinimumMobs;

    public BotWorldViewFactory(GameObjectTags gameObjectTags) {
        this(gameObjectTags, DEFAULT_CLUSTER_RADIUS, DEFAULT_CLUSTER_MIN_MOBS);
    }

    public BotWorldViewFactory(GameObjectTags gameObjectTags,
                               double clusterRadius,
                               int clusterMinimumMobs) {
        this.gameObjectTags = gameObjectTags;
        this.clusterRadius = clusterRadius;
        this.clusterMinimumMobs = clusterMinimumMobs;
    }

    /** Builds the view as of now. Use the overload in tests, where "now" has to be decided. */
    public BotWorldView build(BotEye botEye, Master botSide, BotTier tier, BotMemory memory) {
        return build(botEye, botSide, tier, memory, System.currentTimeMillis());
    }

    /**
     * @param memory           the acting bot's own memory; it is updated here, which is what gives
     *                         the observed objects their velocities
     * @param observedAtMillis when the snapshot was taken, the other end of the velocity estimate
     */
    public BotWorldView build(BotEye botEye,
                              Master botSide,
                              BotTier tier,
                              BotMemory memory,
                              long observedAtMillis) {
        Master enemySide = BotSideUtil.getEnemySide(botSide);
        // Copied: BotSideUtil hands back the shared GameConfig constant and Vector3 is mutable.
        Vector3 selfPosition = new Vector3(BotSideUtil.getPlayerPosition(botSide));

        List<BotVisibleObject> snapshot = botEye.gameObjectList();
        memory.observe(snapshot, observedAtMillis);

        List<ObservedObject> objects = new ArrayList<>(snapshot.size());
        List<ObservedObject> enemies = new ArrayList<>();
        List<ObservedObject> allies = new ArrayList<>();
        List<ObservedObject> enemyMobs = new ArrayList<>();
        for (BotVisibleObject visible : snapshot) {
            ObservedObject observed = observe(visible, memory);
            objects.add(observed);
            if (!observed.targetable()) {
                // A dying or inactive body cannot be hit and cannot be helped, so no rule that
                // picks a target should ever see it in a side list.
                continue;
            }
            if (observed.master() == enemySide) {
                enemies.add(observed);
                if (observed.mob()) {
                    enemyMobs.add(observed);
                }
            } else if (observed.master() == botSide) {
                allies.add(observed);
            }
        }

        return new BotWorldView(
                botSide,
                enemySide,
                tier,
                selfPosition,
                botEye.mana(),
                botEye.cardList(),
                List.copyOf(objects),
                List.copyOf(enemies),
                List.copyOf(allies),
                ClusterFinder.find(enemyMobs, clusterRadius, clusterMinimumMobs),
                ThreatAssessment.observe(snapshot, enemySide, selfPosition, botEye.enemyPlayerHp()),
                botEye.enemyPlayerHp());
    }

    private ObservedObject observe(BotVisibleObject visible, BotMemory memory) {
        return new ObservedObject(
                visible.id(),
                visible.master(),
                visible.type(),
                new Vector3(visible.position()),
                visible.status(),
                visible.hp(),
                visible.mob(),
                visible.targetable(),
                gameObjectTags.tagsOf(visible.type()),
                memory.velocityOf(visible.id()));
    }
}
