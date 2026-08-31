package com.wordonline.server.game.domain.object.component.mob.statemachine.attacker;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.mob.Mob;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.TimedMassPush;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.dto.frame.projectile.ProjectileDto;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.ObjectsInfoDtoBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EvilEntMobTest {

    private static final float DELTA_TIME = 0.1f;
    private static final float SUB_ATTACK_INTERVAL = 0.1f;
    private static final float PULL_MASS_LIMIT = 5f;
    private static final int UPDATE_LIMIT = 60;

    private final List<ProjectileDto> sentProjectiles = new ArrayList<>();

    private GameContext gameContext;
    private GameSessionData sessionData;
    private ObjectsInfoDtoBuilder objectsInfoDtoBuilder;

    @BeforeEach
    void setUp() {
        gameContext = mock(GameContext.class);
        sessionData = new GameSessionData(mock(PlayerData.class), mock(PlayerData.class));
        objectsInfoDtoBuilder = new ObjectsInfoDtoBuilder(gameContext);
        when(gameContext.getGameSessionData()).thenReturn(sessionData);
        when(gameContext.getDeltaTime()).thenReturn(DELTA_TIME);
        when(gameContext.getObjectsInfoDtoBuilder()).thenReturn(objectsInfoDtoBuilder);
    }

    @Test
    void grabArmDragsALightVictimTowardTheEnt() {
        GameObject victim = victim(5f, 1);
        EvilEntMob mob = evilEntTargeting(victim, SUB_ATTACK_INTERVAL);

        assertThat(updateUntil(mob, () -> pushOn(victim) != null)).isTrue();

        Vector3 direction = (Vector3) ReflectionTestUtils.getField(pushOn(victim), "direction");
        assertThat(direction.getX()).isCloseTo(-1f, within(0.0001f));
        assertThat(direction.getY()).isZero();
        assertThat(direction.getZ()).isZero();
        assertThat(sentProjectileTypes()).contains("EvilEntGrabArm");
    }

    @Test
    void armImpactFractionsMirrorTheClientMotionTable() {
        // The authority is the PunchMotion / GrabMotion / FireFistMotion table at the top of
        // StretchProjectile.cs in the client repository. These three literals are its extend
        // fractions; if this fails, one side moved without the other and the arms no longer
        // connect on the frame the server applies the hit.
        assertThat(impactFraction("PUNCH_IMPACT_FRACTION")).isEqualTo(0.55f);
        assertThat(impactFraction("GRAB_IMPACT_FRACTION")).isEqualTo(0.22f);
        assertThat(impactFraction("FIRE_FIST_IMPACT_FRACTION")).isEqualTo(0.45f);
    }

    @Test
    void grabArmProjectileStaysOutForTheWholeDrag() {
        GameObject victim = victim(5f, 1);
        EvilEntMob mob = evilEntTargeting(victim, SUB_ATTACK_INTERVAL);

        mob.update();
        mob.update();

        Object grabState = currentState(mob);
        assertThat(grabState).isInstanceOf(EvilEntMob.GrabState.class);
        float reachTime = (Float) ReflectionTestUtils.getField(grabState, "reachTime");
        float armDuration = sentProjectiles().stream()
                .filter(projectile -> "EvilEntGrabArm".equals(projectile.getType()))
                .findFirst()
                .orElseThrow()
                .getDuration();

        // The client spends this fraction of the duration extending the arm, so the pull has to
        // start at exactly that point rather than once the arm is already coming back in.
        assertThat(reachTime)
                .isCloseTo(armDuration * impactFraction("GRAB_IMPACT_FRACTION"), within(0.0001f));
        // What is left after the arm arrives has to outlast the drag, because the client holds the
        // arm at full extension and re-aims it while the victim is reeled in.
        assertThat(armDuration).isGreaterThanOrEqualTo(reachTime + pullDuration());
    }

    @Test
    void golemMassVictimNeverOpensAGrabAndIsPunchedInstead() {
        assertPunchesWithoutEverGrabbing(victim(5f, 10));
    }

    @Test
    void immovableVictimNeverOpensAGrab() {
        assertPunchesWithoutEverGrabbing(victim(5f, -1));
    }

    @Test
    void victimWithoutARigidBodyNeverOpensAGrab() {
        // The player is exactly this shape: PlayerPrefabInitializer gives it a collider, a health
        // component, a status setter and an effect receiver, and no RigidBody at all. The null
        // check inside canBeDragged is the only thing keeping the grab off it, so it gets a test
        // of its own rather than riding on the mass checks.
        assertPunchesWithoutEverGrabbing(playerShapedVictim(5f));
    }

    @Test
    void grabThatLosesItsDragAbortsWithoutAFireFist() {
        GameObject victim = victim(5f, 1);
        EvilEntMob mob = evilEntTargeting(victim, 30f);
        ReflectionTestUtils.setField(mob, "subAttackTimer", 30f);

        mob.update();
        mob.update();
        assertThat(currentState(mob)).isInstanceOf(EvilEntMob.GrabState.class);

        // Draggable when the grab opened, not draggable by the time the arm lands.
        victim.getComponents().removeIf(RigidBody.class::isInstance);

        assertThat(updateUntil(mob, () -> currentState(mob) instanceof BehaviorMob.IdleState)).isTrue();
        assertThat(sentProjectileTypes())
                .contains("EvilEntGrabArm")
                .doesNotContain("EvilEntFireFist");
        // Abandoned the same way a dead victim is: the short retry window, not the full interval.
        assertThat(subAttackTimer(mob)).isCloseTo(29f + DELTA_TIME, within(0.0001f));
    }

    @Test
    void destroyedVictimAbortsTheGrabSequenceWithoutAFireFist() {
        GameObject victim = victim(5f, 1);
        EvilEntMob mob = evilEntTargeting(victim, SUB_ATTACK_INTERVAL);

        mob.update();
        mob.update();
        victim.destroy();
        mob.update();

        assertThat(currentState(mob)).isInstanceOf(BehaviorMob.IdleState.class);
        assertThat(sentProjectileTypes()).doesNotContain("EvilEntFireFist");
    }

    @Test
    void stateStolenMidSequenceLetsTheSpecialRecover() {
        GameObject victim = victim(5f, 1);
        EvilEntMob mob = evilEntTargeting(victim, 1f);
        ReflectionTestUtils.setField(mob, "subAttackTimer", 1f);

        mob.update();
        assertThat(currentState(mob)).isInstanceOf(EvilEntMob.GrabState.class);

        // A sequence that is genuinely running still holds the cooldown at zero.
        mob.update();
        assertThat(subAttackTimer(mob)).isZero();

        // What BehaviorMob.update does to us when the ent changes master, or when a movement
        // directive such as a rallying totem claims the state machine: the sequence is dropped
        // where it stands and never gets to clean up after itself.
        mob.setState(mob.new IdleState());

        mob.update();
        assertThat(subAttackTimer(mob)).isCloseTo(DELTA_TIME, within(0.0001f));
        assertThat(updateUntil(mob, () -> currentState(mob) instanceof EvilEntMob.GrabState)).isTrue();
    }

    @Test
    void abortedGrabLeavesOnlyAShortRetryWindowOnTheCooldown() {
        GameObject victim = victim(5f, 1);
        EvilEntMob mob = evilEntTargeting(victim, 30f);
        ReflectionTestUtils.setField(mob, "subAttackTimer", 30f);

        mob.update();
        victim.destroy();
        mob.update();

        // A thirty second interval leaves a one second retry window, and the aborting frame has
        // already ticked one delta back onto it.
        assertThat(subAttackTimer(mob)).isCloseTo(29f + DELTA_TIME, within(0.0001f));
    }

    private void assertPunchesWithoutEverGrabbing(GameObject victim) {
        EvilEntMob mob = evilEntTargeting(victim, SUB_ATTACK_INTERVAL);
        mob.start();

        assertThat(updateUntil(mob, () -> sentProjectileTypes().contains("EvilEntPunchArm"))).isTrue();
        assertThat(sentProjectileTypes()).doesNotContain("EvilEntGrabArm", "EvilEntFireFist");
        assertThat(pushOn(victim)).isNull();
    }

    private EvilEntMob evilEntTargeting(GameObject victim, float subAttackInterval) {
        GameObject entObject = evilEnt();
        EvilEntMob mob = new EvilEntMob(
                entObject,
                180,
                0.45f,
                TargetMask.GROUND.bit,
                9,
                1.8f,
                5f,
                14f,
                28,
                6f,
                4f,
                subAttackInterval,
                PULL_MASS_LIMIT);
        entObject.getComponents().add(mob);
        mob.target = victim;
        return mob;
    }

    private boolean updateUntil(EvilEntMob mob, BooleanSupplier reached) {
        for (int i = 0; i < UPDATE_LIMIT; i++) {
            mob.update();
            if (reached.getAsBoolean()) {
                return true;
            }
        }
        return false;
    }

    private float subAttackTimer(EvilEntMob mob) {
        return (Float) ReflectionTestUtils.getField(mob, "subAttackTimer");
    }

    private Object currentState(EvilEntMob mob) {
        return ReflectionTestUtils.getField(mob, "currentState");
    }

    private TimedMassPush pushOn(GameObject victim) {
        TimedMassPush pending = victim.getComponentsToAdd().stream()
                .filter(TimedMassPush.class::isInstance)
                .map(TimedMassPush.class::cast)
                .findFirst()
                .orElse(null);
        return pending != null ? pending : victim.getComponent(TimedMassPush.class);
    }

    /** The builder hands out and clears its frame's projectiles, so drain it into one running list. */
    private List<ProjectileDto> sentProjectiles() {
        sentProjectiles.addAll(objectsInfoDtoBuilder.getObjectsInfoDto().projectile());
        return sentProjectiles;
    }

    private List<String> sentProjectileTypes() {
        return sentProjectiles().stream()
                .map(ProjectileDto::getType)
                .toList();
    }

    private float impactFraction(String name) {
        return (Float) ReflectionTestUtils.getField(EvilEntMob.class, name);
    }

    private float pullDuration() {
        return (Float) ReflectionTestUtils.getField(EvilEntMob.class, "PULL_DURATION");
    }

    private GameObject evilEnt() {
        GameObject entObject = new GameObject(
                Master.LeftPlayer, PrefabType.EvilEnt, new Vector3(0f, 0f, 0f), gameContext);
        entObject.setStatus(Status.Idle);
        entObject.addCollider(new CircleCollider(entObject, 1.2f, false));
        entObject.getComponents().add(new RigidBody(entObject, 10));
        sessionData.gameObjects.add(entObject);
        return entObject;
    }

    private GameObject victim(float x, int mass) {
        GameObject victim = target(PrefabType.RockSlime, x);
        victim.getComponents().add(new RigidBody(victim, mass));
        return victim;
    }

    /** A target with no RigidBody at all, the way the player object is actually built. */
    private GameObject playerShapedVictim(float x) {
        return target(PrefabType.Player, x);
    }

    private GameObject target(PrefabType prefabType, float x) {
        GameObject target = new GameObject(
                Master.RightPlayer, prefabType, new Vector3(x, 0f, 0f), gameContext);
        target.setStatus(Status.Idle);
        target.addCollider(new CircleCollider(target, 0.5f, false));
        target.getComponents().add(new TargetDummy(target));
        sessionData.gameObjects.add(target);
        return target;
    }

    /** Just enough of a mob for the ent's detector to see the victim and for damage to land. */
    private static final class TargetDummy extends Mob {

        private TargetDummy(GameObject gameObject) {
            super(gameObject, 1000, 0f);
        }

        @Override
        public void onDeath() {
        }

        @Override
        public void start() {
        }

        @Override
        public void onDestroy() {
        }
    }
}
