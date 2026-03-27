package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("pve_vine_colony_prefab")
public class PveVineColonyPrefabInitializer extends SimplePveBossInitializer {

    public PveVineColonyPrefabInitializer(Parameters parameters) {
        super(
                PrefabType.PveVineColony,
                parameters,
                "vine_colony",
                ElementType.NATURE,
                null,
                List.of("vine", "nature_slime_swarm", "water_slime_swarm", "vine_spirit")
        );
    }
}
