package com.wordonline.server.game.domain.object.component.magic;

import java.util.HashSet;
import java.util.Set;

public class VineHitTracker {

    private final Set<Integer> damagedObjectIds = new HashSet<>();

    public boolean markIfFirstHit(int gameObjectId) {
        return damagedObjectIds.add(gameObjectId);
    }

    public int hitCount() {
        return damagedObjectIds.size();
    }
}
