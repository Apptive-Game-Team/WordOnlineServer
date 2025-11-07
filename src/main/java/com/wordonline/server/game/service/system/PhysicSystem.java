package com.wordonline.server.game.service.system;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.physic.Collider;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.util.Pair;
import com.wordonline.server.game.domain.object.component.physic.Collidable;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.util.CollisionChecker;
import com.wordonline.server.game.util.CollisionSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Scope("prototype")
public class PhysicSystem implements CollisionSystem, GameSystem {

    private static final float SAME_PLACE_THRESHOLD = 1e-6f;

    private final Set<Pair<GameObject>> collidedPairs = new HashSet<>();

    @Override
    public void update(GameContext gameContext) {
        handleCollisions(gameContext.getActiveGameObjects());
        checkAndHandleCollisions(gameContext.getActiveGameObjects());
        onUpdateEnd(gameContext.getActiveGameObjects());
    }

    private void handleCollisions(List<GameObject> gameObjects) {
        calculateCollisions(gameObjects);
        applyCollisionsResponses();
    }

    private void calculateCollisions(List<GameObject> gameObjects) {
        for (int i = 0; i < gameObjects.size(); i++) {
            GameObject a = gameObjects.get(i);
            List<Collidable> collidableAList = a.getComponents(Collidable.class);
            if (collidableAList.isEmpty() || a.isDestroyed()) continue;

            for (int j = i + 1; j < gameObjects.size(); j++) {
                GameObject b = gameObjects.get(j);
                List<Collidable> collidableBList = b.getComponents(Collidable.class);
                if (collidableBList.isEmpty() || a.isDestroyed()) continue;

                if (CollisionChecker.isColliding(a, b)) {
                    collidedPairs.add(new Pair<>(a, b));
                }
            }
        }
    }

    private void applyCollisionsResponses() {
        collidedPairs.forEach(
                gameObjectPair -> {
                    GameObject a = gameObjectPair.a();
                    GameObject b = gameObjectPair.b();

                    RigidBody rigidBodyA = a.getComponent(RigidBody.class);
                    RigidBody rigidBodyB = b.getComponent(RigidBody.class);

                    gameObjectPair.a().getColliders().stream().filter(Collider::isNotTrigger).forEach(
                            colliderA -> {
                                gameObjectPair.b().getColliders().stream().filter(Collider::isNotTrigger).forEach(
                                    colliderB -> {

                                        float invMassA = colliderA.getInvMass();
                                        float invMassB = colliderB.getInvMass();

                                        Vector3 normal = getNormalizedDisplacement(colliderA, colliderB);

                                        if (normal == null) {
                                            return;
                                        }

                                        Vector3 relativeVelocity = colliderA.getVelocity().subtract(colliderB.getVelocity());
                                        float separatingVelocity = relativeVelocity.dot(normal);

                                        // 이미 멀어지는 중이면 무시
                                        if (separatingVelocity > 0) return;

                                        // 반사량 계산
                                        float restitution = 1.0f; // 탄성 계수
                                        float totalInvMass = invMassA + invMassB;
                                        if (totalInvMass == 0) return;

                                        float impulseMag = - (1 + restitution) * separatingVelocity /
                                              totalInvMass;
                                        impulseMag = Math.clamp(impulseMag, -1.0f, 1.0f);

                                        Vector3 impulse = normal.multiply(impulseMag);
                                        if (invMassA > 0) {
                                          rigidBodyA.addVelocity(impulse.multiply(invMassA));
                                        }
                                        if (invMassB > 0) {
                                          rigidBodyB.addVelocity(impulse.multiply(-invMassB));
                                        }
                                    }
                                );
                            }
                    );
                }
        );
    }

    private Vector3 getNormalizedDisplacement(Collider a, Collider b) {
        Vector3 displacement = a.getDisplacement(b);

        if (displacement == null) {
            return null;
        }

        if (displacement.getY() <= SAME_PLACE_THRESHOLD && displacement.getX() <= SAME_PLACE_THRESHOLD) {
            return Vector3.randomUnitVector();
        }

        return displacement.normalize();
    }

    @Override
    public void checkAndHandleCollisions(List<GameObject> gameObjects) {
        collidedPairs.forEach(
                gameObjectPair -> {

                    if (gameObjectPair.a().getMaster() == gameObjectPair.b().getMaster() &&
                            gameObjectPair.b().getMaster() != Master.None) {
                        return;
                    }

                    if (gameObjectPair.a().isDestroyed() || gameObjectPair.b().isDestroyed()) {
                        return;
                    }

                    gameObjectPair.a().getComponents(Collidable.class).forEach(collidable -> collidable.onCollision(gameObjectPair.b()));
                    gameObjectPair.b().getComponents(Collidable.class).forEach(collidable -> collidable.onCollision(gameObjectPair.a()));
                }
        );
    }

    // apply rigidbody velocity and clear velocity
    private void onUpdateEnd(List<GameObject> gameObjects) {
        gameObjects.forEach(
                gameObject -> {
                    handleApplyingRigidBody(gameObject);
                    handleApplyingZPhysics(gameObject);
                }
        );
        collidedPairs.clear();
    }

    private void handleApplyingRigidBody(GameObject gameObject) {
        RigidBody rigidBody = gameObject.getComponent(RigidBody.class);
        if (rigidBody == null) {
            return;
        }
        rigidBody.applyVelocity();
    }

    private void handleApplyingZPhysics(GameObject gameObject) {
        ZPhysics zPhysics = gameObject.getComponent(ZPhysics.class);
        if (zPhysics == null) {
            return;
        }
        if(zPhysics.canHover()) zPhysics.applyHover();
        else zPhysics.applyZForce();
    }
}
