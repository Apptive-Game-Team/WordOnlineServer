package com.wordonline.server.game.domain.magic.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.CastKind;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.implement.build.SummonMagic;
import com.wordonline.server.game.domain.magic.implement.drop.DropMagic;
import com.wordonline.server.game.domain.magic.implement.explode.ExplosionMagic;
import com.wordonline.server.game.domain.magic.implement.shoot.ShotMagic;
import com.wordonline.server.game.domain.magic.implement.spawn.SpawnMagic;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.domain.object.component.magic.Shot;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.domain.parameter.GameObjectParameters;
import com.wordonline.server.game.domain.parameter.ParameterKey;
import com.wordonline.server.game.dto.MagicInfoDto;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.repository.MagicParameterRepository;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.MagicParameterService;

/**
 * 데이터 한 행으로 만든 마법이 지우기 전 클래스와 같은 자리에 같은 것을 놓는지 본다.
 * 높이 값들은 CONTRACT.md 의 "재현해야 하는 예외" 절에서 왔다.
 */
class DatabaseMagicFactoryTest {

    private static final long MAGIC_ID = 42L;

    private final MagicParameterRepository magicParameterRepository = mock(MagicParameterRepository.class);
    private final MagicParameterService magicParameterService =
            new MagicParameterService(magicParameterRepository);
    private final Parameters parameters = mock(Parameters.class);
    private final GameObjectParameters gameObjectParameters = mock(GameObjectParameters.class);
    private final GameContext gameContext = mock(GameContext.class);
    private final DatabaseMagicFactory factory = new DatabaseMagicFactory(parameters, magicParameterService);

    @Test
    void spawnMagicStandsTheUnitOnTheGroundWhenNoSpawnHeightIsStored() {
        noSpawnHeight();
        quantity(1);

        Magic magic = create(CastKind.Spawn, "wind_spirit", "WindSpirit");

        assertThat(magic).isInstanceOf(SpawnMagic.class);
        magic.run(gameContext, Master.LeftPlayer, new Vector3(3, 9, 5));
        GameObject created = created();
        assertThat(created.getType()).isEqualTo(PrefabType.WindSpirit);
        assertThat(created.getPosition()).isEqualTo(new Vector3(3, 0, 5));
    }

    @Test
    void spawnMagicLiftsAnAerialUnitToTheStoredSpawnHeight() {
        spawnHeight(GameConfig.AERIAL_MOB_INIT_HEIGHT);
        quantity(1);

        Magic magic = create(CastKind.Spawn, "wind_spirit", "WindSpirit");

        magic.run(gameContext, Master.LeftPlayer, new Vector3(3, 0, 5));
        assertThat(created().getPosition()).isEqualTo(new Vector3(3, 3, 5));
    }

    @Test
    void spawnMagicReadsQuantityFromTheGameObjectItPointsAt() {
        noSpawnHeight();
        quantity(3);

        create(CastKind.Spawn, "ember_spirit", "EmberSpirit")
                .run(gameContext, Master.LeftPlayer, new Vector3(3, 0, 5));

        verify(gameContext, times(3)).createGameObject(any());
        verify(parameters).objectByName("ember_spirit");
    }

    @Test
    void summonMagicKeepsTheAimPointHeightWhenNoSpawnHeightIsStored() {
        noSpawnHeight();

        Magic magic = create(CastKind.Summon, "mana_well", "ManaWell");

        assertThat(magic).isInstanceOf(SummonMagic.class);
        magic.run(gameContext, Master.LeftPlayer, new Vector3(3, 2, 5));
        assertThat(created().getPosition()).isEqualTo(new Vector3(3, 2, 5));
    }

    @Test
    void summonMagicIsGroundedByAStoredSpawnHeightOfZero() {
        spawnHeight(0f);

        create(CastKind.Summon, "ground_cannon", "GroundCannon")
                .run(gameContext, Master.LeftPlayer, new Vector3(3, 2, 5));

        GameObject created = created();
        assertThat(created.getType()).isEqualTo(PrefabType.GroundCannon);
        assertThat(created.getPosition()).isEqualTo(new Vector3(3, 0, 5));
    }

    @Test
    void dropMagicFallsFromTheFamilyDefaultHeight() {
        noSpawnHeight();

        Magic magic = create(CastKind.Drop, "rock_drop", "RockDrop");

        assertThat(magic).isInstanceOf(DropMagic.class);
        magic.run(gameContext, Master.LeftPlayer, new Vector3(3, 0, 5));
        assertThat(created().getPosition())
                .isEqualTo(new Vector3(3, GameConfig.DROP_MAGIC_INITIAL_HEIGHT, 5));
    }

    @Test
    void dropMagicUsesTheStoredSpawnHeightInsteadOfTheDefault() {
        spawnHeight(0f);

        create(CastKind.Drop, "lightning_cloud", "LightningCloud")
                .run(gameContext, Master.LeftPlayer, new Vector3(3, 7, 5));

        assertThat(created().getPosition()).isEqualTo(new Vector3(3, 0, 5));
    }

    @Test
    void explosionMagicCreatesOneObjectAtTheAimPoint() {
        Magic magic = create(CastKind.Explosion, "water_explosion", "WaterExplosion");

        assertThat(magic).isInstanceOf(ExplosionMagic.class);
        magic.run(gameContext, Master.LeftPlayer, new Vector3(4, 0, 6));
        GameObject created = created();
        assertThat(created.getType()).isEqualTo(PrefabType.WaterExplosion);
        assertThat(created.getPosition()).isEqualTo(new Vector3(4, 0, 6));
    }

    @Test
    void shotMagicCreatesTheProjectileAtTheCastOrigin() {
        doAnswer(invocation -> {
            GameObject gameObject = invocation.getArgument(0);
            gameObject.getComponents().add(new Shot(gameObject, 1, 1f));
            return null;
        }).when(gameContext).createGameObject(any());

        Magic magic = create(CastKind.Shot, "fire_shot", "FireShot");

        assertThat(magic).isInstanceOf(ShotMagic.class);
        magic.run(gameContext, Master.LeftPlayer, new Vector3(1, 0, 5), new Vector3(8, 0, 5));
        GameObject created = created();
        assertThat(created.getType()).isEqualTo(PrefabType.FireShot);
        assertThat(created.getPosition()).isEqualTo(new Vector3(1, 0, 5));
    }

    @Test
    void skipsAMagicWhosePrefabIsNotAPrefabType() {
        Optional<Magic> magic = factory.create(
                magicInfo("wall_golem", "wall_golem", "NoSuchPrefab"), CastKind.Spawn);

        assertThat(magic).isEmpty();
        verify(parameters, never()).objectByName(anyString());
    }

    @Test
    void skipsAMagicThatPointsAtNoGameObject() {
        assertThat(factory.create(magicInfo("wall_golem", null, null), CastKind.Spawn)).isEmpty();
        assertThat(factory.create(magicInfo("wall_golem", " ", "WallGolem"), CastKind.Spawn)).isEmpty();
    }

    @Test
    void skipsAMagicWhoseGameObjectCarriesNoPrefab() {
        assertThat(factory.create(magicInfo("wall_golem", "wall_golem", null), CastKind.Spawn)).isEmpty();
    }

    private Magic create(CastKind castKind, String gameObjectName, String prefabName) {
        when(parameters.objectByName(gameObjectName)).thenReturn(gameObjectParameters);
        return factory.create(magicInfo("a_magic", gameObjectName, prefabName), castKind).orElseThrow();
    }

    private static MagicInfoDto magicInfo(String name, String gameObjectName, String prefabName) {
        return new MagicInfoDto(
                MAGIC_ID, name, List.of(CardType.Fire, CardType.Spawn), null, gameObjectName, prefabName);
    }

    private void spawnHeight(float value) {
        when(magicParameterRepository.getParameterValue(MAGIC_ID, ParameterKey.SPAWN_HEIGHT.dbName()))
                .thenReturn(Optional.of((double) value));
    }

    private void noSpawnHeight() {
        when(magicParameterRepository.getParameterValue(MAGIC_ID, ParameterKey.SPAWN_HEIGHT.dbName()))
                .thenReturn(Optional.empty());
    }

    private void quantity(int value) {
        when(gameObjectParameters.intValueOrDefault(ParameterKey.QUANTITY, 1)).thenReturn(value);
    }

    private GameObject created() {
        ArgumentCaptor<GameObject> captor = ArgumentCaptor.forClass(GameObject.class);
        verify(gameContext, atLeastOnce()).createGameObject(captor.capture());
        return captor.getValue();
    }
}
