package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("pve_nature_slime_nest_prefab")
public class PveNatureSlimeNestPrefabInitializer extends SimplePveBossInitializer {

    private static final float BOSS_ATTACK_INTERVAL = 5f;
    private static final int BOSS_SPAWN_COUNT = 3;

    public PveNatureSlimeNestPrefabInitializer(Parameters parameters) {
        super(
                PrefabType.PveNatureSlimeNest,
                parameters,
                "pve_nature_slime_nest",
                ElementType.NATURE,
                List.of(new SpawnConfig(PrefabType.LeafSlime, BOSS_ATTACK_INTERVAL, BOSS_SPAWN_COUNT)),
                null
        );
    }
}
