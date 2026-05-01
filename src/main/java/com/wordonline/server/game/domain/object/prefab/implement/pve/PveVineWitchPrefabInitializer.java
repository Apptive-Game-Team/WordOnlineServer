package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.implement.shoot.VineFanMagic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.PVEBossMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.VineWitchMob;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("pve_vine_witch_prefab")
public class PveVineWitchPrefabInitializer extends SimplePveBossInitializer {

    private static final float BOSS_ATTACK_INTERVAL = 8f;
    private static final int BOSS_SPAWN_COUNT = 2;
    private static final List<String> MAGIC_NAMES = List.of(
            "vine",
            "nature_slime_swarm",
            "water_slime_swarm",
            "vine_colony",
            "vine_spirit"
    );

    public PveVineWitchPrefabInitializer(Parameters parameters) {
        super(
                PrefabType.PveVineWitch,
                parameters,
                "pve_vine_witch",
                ElementType.NATURE,
                0f,
                BOSS_ATTACK_INTERVAL,
                7f,
                List.of(
                        new SpawnConfig(PrefabType.LeafSlime, BOSS_ATTACK_INTERVAL, BOSS_SPAWN_COUNT),
                        new SpawnConfig(PrefabType.WaterSlime, BOSS_ATTACK_INTERVAL, BOSS_SPAWN_COUNT),
                        new SpawnConfig(PrefabType.VineSpirit, BOSS_ATTACK_INTERVAL, BOSS_SPAWN_COUNT)
                ),
                MAGIC_NAMES
        );
    }

    @Override
    protected PVEBossMob createBossMob(GameObject gameObject, int maxHp, List<Magic> magics) {
        Magic vineFanMagic = parseMagic("vinefan");
        return new VineWitchMob(
                gameObject,
                maxHp,
                getBossSpeed(),
                getTargetMask(),
                getBossAttackInterval(),
                getBossAttackRange(),
                magics,
                vineFanMagic instanceof VineFanMagic fanMagic ? fanMagic : null
        );
    }
}
