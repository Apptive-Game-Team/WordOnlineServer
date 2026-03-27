package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.PVEBossMob;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.VineWitchMob;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("pve_vine_witch_prefab")
public class PveVineWitchPrefabInitializer extends SimplePveBossInitializer {

    private static final float BOSS_ATTACK_INTERVAL = 2f;
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
                "vine_spirit",
                ElementType.NATURE,
                0f,
                BOSS_ATTACK_INTERVAL,
                7f,
                null,
                MAGIC_NAMES
        );
    }

    @Override
    protected PVEBossMob createBossMob(GameObject gameObject, int maxHp, List<Magic> magics) {
        return new VineWitchMob(
                gameObject,
                maxHp,
                getBossSpeed(),
                getTargetMask(),
                getBossAttackInterval(),
                getBossAttackRange(),
                magics,
                parseMagic("vine")
        );
    }
}
