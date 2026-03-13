package com.wordonline.server.game.domain.object.prefab.implement.pve;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.mob.detector.TargetMask;
import com.wordonline.server.game.domain.object.component.mob.statemachine.attacker.PVEBossMob;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.RigidBody;
import com.wordonline.server.game.domain.object.component.physic.ZPhysics;
import com.wordonline.server.game.domain.object.prefab.PrefabInitializer;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component("pve_water_slime_nest_prefab")
public class PveWaterSlimeNestPrefabInitializer extends PrefabInitializer {

    private static final float BOSS_SPEED = 0f;
    private static final float BOSS_ATTACK_INTERVAL = 2.5f;
    private static final float BOSS_ATTACK_RANGE = 7f;

    private final Parameters parameters;
    private final DatabaseMagicParser magicParser;

    public PveWaterSlimeNestPrefabInitializer(Parameters parameters, DatabaseMagicParser magicParser) {
        super(PrefabType.PveWaterSlimeNest);
        this.parameters = parameters;
        this.magicParser = magicParser;
    }

    @Override
    public void initialize(GameObject gameObject) {
        gameObject.addComponent(new RigidBody(gameObject, (int) parameters.getValue("pve_water_slime_nest", "mass")));
        gameObject.addComponent(new ZPhysics(gameObject));
        gameObject.getColliders().add(new CircleCollider(gameObject, (float) parameters.getValue("pve_water_slime_nest", "radius"), true));
        gameObject.setElement(ElementType.WATER);

        int maxHp = (int) parameters.getValue("pve_water_slime_nest", "hp");
        List<Magic> magics = resolveMagics("water_slime_swarm");
        gameObject.addComponent(new PVEBossMob(
                gameObject,
                maxHp,
                BOSS_SPEED,
                TargetMask.GROUND.bit,
                BOSS_ATTACK_INTERVAL,
                BOSS_ATTACK_RANGE,
                magics
        ));
    }

    private List<Magic> resolveMagics(String... magicNames) {
        return Arrays.stream(magicNames)
                .map(magicParser::parseMagicForBot)
                .filter(Objects::nonNull)
                .toList();
    }
}
