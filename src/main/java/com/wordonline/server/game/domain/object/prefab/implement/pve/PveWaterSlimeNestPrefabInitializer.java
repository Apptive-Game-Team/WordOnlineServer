package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("pve_water_slime_nest_prefab")
public class PveWaterSlimeNestPrefabInitializer extends SimplePveBossInitializer {

    private static final float BOSS_ATTACK_INTERVAL = 10f;
    private static final int BOSS_SPAWN_COUNT = 1;

    public PveWaterSlimeNestPrefabInitializer(Parameters parameters) {
        super(
                PrefabType.PveWaterSlimeNest,
                parameters,
                "pve_water_slime_nest",
                ElementType.WATER,
                List.of(new SpawnConfig(PrefabType.WaterSlime, BOSS_ATTACK_INTERVAL, BOSS_SPAWN_COUNT)),
                null
        );
    }
}
