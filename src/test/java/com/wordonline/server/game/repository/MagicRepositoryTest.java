package com.wordonline.server.game.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.h2.Driver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.dto.MagicInfoDto;

/**
 * 마법 조회가 {@code cast_kind} 와, 그 마법이 가리키는 game object 의 이름·prefab 을 같이
 * 읽는지 본다. 세 값은 모두 비어 있을 수 있고, 비어 있어도 그 마법은 조회에서 빠지지 않는다.
 */
class MagicRepositoryTest {

    private JdbcClient jdbcClient;
    private MagicRepository magicRepository;

    @BeforeEach
    void setUp() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource(
                new Driver(),
                "jdbc:h2:mem:magic_" + System.nanoTime() + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
                "sa",
                ""
        );
        jdbcClient = JdbcClient.create(dataSource);
        jdbcClient.sql("""
                CREATE TABLE game_objects (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(31),
                    prefab VARCHAR(63)
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE magics (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(255),
                    cast_kind VARCHAR(15),
                    game_object_id BIGINT
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE cards (
                    id BIGINT PRIMARY KEY,
                    name VARCHAR(31)
                )
                """).update();
        jdbcClient.sql("""
                CREATE TABLE magic_cards (
                    id BIGINT PRIMARY KEY,
                    magic_id BIGINT,
                    card_id BIGINT
                )
                """).update();
        insertCard(1, "Water");
        insertCard(2, "Spawn");
        insertCard(3, "Fire");
        insertCard(4, "Shoot");
        magicRepository = new MagicRepository(jdbcClient);
    }

    @Test
    void readsCastKindAndTheGameObjectNameAndPrefabTogether() {
        insertGameObject(10, "water_slime", "WaterSlime");
        insertMagic(100, "water_slime_swarm", "Spawn", 10L);
        insertMagicCard(1000, 100, 1);
        insertMagicCard(1001, 100, 2);

        MagicInfoDto magic = magicRepository.getAllMagic().getFirst();

        assertThat(magic.id()).isEqualTo(100L);
        assertThat(magic.name()).isEqualTo("water_slime_swarm");
        assertThat(magic.cards()).containsExactlyInAnyOrder(CardType.Water, CardType.Spawn);
        assertThat(magic.castKind()).isEqualTo("Spawn");
        assertThat(magic.gameObjectName()).isEqualTo("water_slime");
        assertThat(magic.prefabName()).isEqualTo("WaterSlime");
    }

    @Test
    void aMagicWithNoDataYetStillComesBackWithTheThreeValuesEmpty() {
        insertMagic(101, "spirit_bomb", null, null);
        insertMagicCard(1002, 101, 3);
        insertMagicCard(1003, 101, 4);

        MagicInfoDto magic = magicRepository.getAllMagic().getFirst();

        assertThat(magic.name()).isEqualTo("spirit_bomb");
        assertThat(magic.cards()).containsExactlyInAnyOrder(CardType.Fire, CardType.Shoot);
        assertThat(magic.castKind()).isNull();
        assertThat(magic.gameObjectName()).isNull();
        assertThat(magic.prefabName()).isNull();
    }

    @Test
    void aGameObjectWithoutAPrefabLeavesOnlyThePrefabEmpty() {
        insertGameObject(11, "wall_golem", null);
        insertMagic(102, "wall_golem", "Spawn", 11L);
        insertMagicCard(1004, 102, 1);

        MagicInfoDto magic = magicRepository.getAllMagic().getFirst();

        assertThat(magic.gameObjectName()).isEqualTo("wall_golem");
        assertThat(magic.prefabName()).isNull();
    }

    @Test
    void readsEveryMagicThatHasCards() {
        insertGameObject(10, "water_slime", "WaterSlime");
        insertMagic(100, "water_slime_swarm", "Spawn", 10L);
        insertMagicCard(1000, 100, 1);
        insertMagic(101, "spirit_bomb", null, null);
        insertMagicCard(1002, 101, 3);

        List<MagicInfoDto> magics = magicRepository.getAllMagic();

        assertThat(magics).extracting(MagicInfoDto::name)
                .containsExactlyInAnyOrder("water_slime_swarm", "spirit_bomb");
    }

    private void insertCard(long id, String name) {
        jdbcClient.sql("INSERT INTO cards (id, name) VALUES (:id, :name)")
                .param("id", id).param("name", name).update();
    }

    private void insertGameObject(long id, String name, String prefab) {
        jdbcClient.sql("INSERT INTO game_objects (id, name, prefab) VALUES (:id, :name, :prefab)")
                .param("id", id).param("name", name).param("prefab", prefab).update();
    }

    private void insertMagic(long id, String name, String castKind, Long gameObjectId) {
        jdbcClient.sql("""
                        INSERT INTO magics (id, name, cast_kind, game_object_id)
                        VALUES (:id, :name, :castKind, :gameObjectId)
                        """)
                .param("id", id)
                .param("name", name)
                .param("castKind", castKind)
                .param("gameObjectId", gameObjectId)
                .update();
    }

    private void insertMagicCard(long id, long magicId, long cardId) {
        jdbcClient.sql("INSERT INTO magic_cards (id, magic_id, card_id) VALUES (:id, :magicId, :cardId)")
                .param("id", id).param("magicId", magicId).param("cardId", cardId).update();
    }
}
