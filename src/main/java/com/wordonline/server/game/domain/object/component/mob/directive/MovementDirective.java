package com.wordonline.server.game.domain.object.component.mob.directive;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;

import java.util.Optional;

public interface MovementDirective {
    int priority();

    Optional<Vector3> getMoveTarget(GameObject self);

    float getArrivalDistance();

    boolean suppressCombat();
}
