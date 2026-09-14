package com.wordonline.server.game.domain.magic.parser;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CastKind;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.implement.build.SummonMagic;
import com.wordonline.server.game.domain.magic.implement.drop.DropMagic;
import com.wordonline.server.game.domain.magic.implement.explode.ExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.ShotMagic;
import com.wordonline.server.game.domain.magic.implement.spawn.SpawnMagic;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.MagicInfoDto;
import com.wordonline.server.game.service.MagicParameterService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * {@code magics} 한 행으로 계열별 {@code Abstract*Magic} 을 만든다. {@link CastKind#Code} 는
 * 여기로 오지 않는다 — Java 클래스가 필요한 마법이라 bean 으로 찾는다.
 *
 * <p>prefab 이름이 {@link PrefabType} 에 없거나 game object 가 비어 있으면 경고하고 비어 있는
 * Optional 을 돌려준다. 그 마법만 등록에서 빠지고 나머지는 그대로 올라간다 — bean 이 없을 때와
 * 같은 처리다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMagicFactory {

    private static final float DEFAULT_SPAWN_HEIGHT = 0f;

    private final Parameters parameters;
    private final MagicParameterService magicParameterService;

    public Optional<Magic> create(MagicInfoDto magicInfo, CastKind castKind) {
        if (magicInfo.gameObjectName() == null || magicInfo.gameObjectName().isBlank()) {
            log.warn("[Magic:Loading] magic ({}) has cast_kind {} but points at no game object",
                    magicInfo.name(), castKind);
            return Optional.empty();
        }

        Optional<PrefabType> prefabType = findPrefabType(magicInfo.prefabName());
        if (prefabType.isEmpty()) {
            log.warn("[Magic:Loading] magic ({}) has prefab ({}) that is not a PrefabType",
                    magicInfo.name(), magicInfo.prefabName());
            return Optional.empty();
        }

        return Optional.of(create(magicInfo, castKind, prefabType.get()));
    }

    private Magic create(MagicInfoDto magicInfo, CastKind castKind, PrefabType prefabType) {
        return switch (castKind) {
            case Spawn -> new SpawnMagic(
                    prefabType,
                    parameters.objectByName(magicInfo.gameObjectName()),
                    spawnHeight(magicInfo, DEFAULT_SPAWN_HEIGHT));
            case Summon -> new SummonMagic(prefabType, spawnHeightOrNull(magicInfo));
            case Drop -> new DropMagic(
                    prefabType,
                    spawnHeight(magicInfo, GameConfig.DROP_MAGIC_INITIAL_HEIGHT));
            case Shot -> new ShotMagic(prefabType);
            case Explosion -> new ExplosionMagic(prefabType);
            case Code -> throw new IllegalArgumentException(
                    "Code magic is built from its bean, not from data: " + magicInfo.name());
        };
    }

    private float spawnHeight(MagicInfoDto magicInfo, float defaultHeight) {
        return (float) magicParameterService.getValueOrDefault(
                magicInfo.id(), ParameterKey.SPAWN_HEIGHT, defaultHeight);
    }

    /** Summon 은 spawn_height 가 없으면 조준점의 y 를 그대로 쓰기 때문에 null 을 돌려준다. */
    private Float spawnHeightOrNull(MagicInfoDto magicInfo) {
        return magicParameterService.findValue(magicInfo.id(), ParameterKey.SPAWN_HEIGHT)
                .map(Double::floatValue)
                .orElse(null);
    }

    private Optional<PrefabType> findPrefabType(String prefabName) {
        if (prefabName == null || prefabName.isBlank()) {
            return Optional.empty();
        }

        for (PrefabType prefabType : PrefabType.values()) {
            if (prefabType.name().equals(prefabName.trim())) {
                return Optional.of(prefabType);
            }
        }
        return Optional.empty();
    }
}
