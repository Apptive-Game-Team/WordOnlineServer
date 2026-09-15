package com.wordonline.server.game.domain.magic.parser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.CastKind;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.MagicInfoDto;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.repository.MagicRepository;
import com.wordonline.server.game.service.GameContext;

/**
 * 어느 마법이 bean 으로 가고 어느 마법이 데이터로 만들어지는지 본다.
 */
class DatabaseMagicParserTest {

    /** 등록 키는 카드를 정렬해서 만든다 - 조합 순서가 달라도 같은 마법이 나와야 하기 때문이다. */
    private static final List<CardType> FIRE_SHOOT = List.of(CardType.Shoot, CardType.Fire);
    private static final List<CardType> WATER_SPAWN = List.of(CardType.Spawn, CardType.Water);

    private final MagicRepository magicRepository = mock(MagicRepository.class);
    private final DatabaseMagicFactory databaseMagicFactory = mock(DatabaseMagicFactory.class);
    private final ApplicationContext applicationContext = mock(ApplicationContext.class);
    private final DatabaseMagicParser parser =
            new DatabaseMagicParser(magicRepository, databaseMagicFactory, applicationContext);

    @Test
    void aMagicWithoutACastKindStillComesFromItsBean() {
        Magic bean = bean("spirit_bomb");
        when(magicRepository.getAllMagic()).thenReturn(List.of(
                magicInfo(1L, "spirit_bomb", FIRE_SHOOT, null, "spirit_bomb", "SpiritBomb")));

        Map<List<CardType>, Magic> recipes = parser.getAllMagicRecipeMap();

        assertThat(recipes).containsEntry(FIRE_SHOOT, bean);
        assertThat(bean.id).isEqualTo(1L);
        assertThat(bean.name).isEqualTo("spirit_bomb");
        verify(databaseMagicFactory, never()).create(any(), any());
    }

    @Test
    void aCodeMagicComesFromItsBean() {
        Magic bean = bean("vine_toss");
        when(magicRepository.getAllMagic()).thenReturn(List.of(
                magicInfo(2L, "vine_toss", FIRE_SHOOT, "Code", "vine", "Vine")));

        assertThat(parser.getAllMagicRecipeMap()).containsEntry(FIRE_SHOOT, bean);
        verify(databaseMagicFactory, never()).create(any(), any());
    }

    @Test
    void aCodeMagicWithoutABeanIsLeftOutOfTheRegistration() {
        when(applicationContext.containsBean("vine_toss")).thenReturn(false);
        when(magicRepository.getAllMagic()).thenReturn(List.of(
                magicInfo(2L, "vine_toss", FIRE_SHOOT, "Code", "vine", "Vine")));

        assertThat(parser.getAllMagicRecipeMap()).isEmpty();
    }

    @Test
    void aMagicWithACastKindIsBuiltFromDataWithoutLookingForABean() {
        Magic built = stubMagic();
        MagicInfoDto waterSlimeSwarm =
                magicInfo(3L, "water_slime_swarm", WATER_SPAWN, "Spawn", "water_slime", "WaterSlime");
        when(magicRepository.getAllMagic()).thenReturn(List.of(waterSlimeSwarm));
        when(databaseMagicFactory.create(waterSlimeSwarm, CastKind.Spawn)).thenReturn(Optional.of(built));

        Map<List<CardType>, Magic> recipes = parser.getAllMagicRecipeMap();

        assertThat(recipes).containsEntry(WATER_SPAWN, built);
        assertThat(built.id).isEqualTo(3L);
        assertThat(built.name).isEqualTo("water_slime_swarm");
        verify(applicationContext, never()).containsBean(anyString());
    }

    @Test
    void onlyTheMagicTheFactoryRejectsIsLeftOut() {
        Magic built = stubMagic();
        MagicInfoDto broken =
                magicInfo(4L, "wall_golem", FIRE_SHOOT, "Spawn", "wall_golem", "NoSuchPrefab");
        MagicInfoDto sound =
                magicInfo(5L, "water_slime_swarm", WATER_SPAWN, "Spawn", "water_slime", "WaterSlime");
        when(magicRepository.getAllMagic()).thenReturn(List.of(broken, sound));
        when(databaseMagicFactory.create(broken, CastKind.Spawn)).thenReturn(Optional.empty());
        when(databaseMagicFactory.create(sound, CastKind.Spawn)).thenReturn(Optional.of(built));

        Map<List<CardType>, Magic> recipes = parser.getAllMagicRecipeMap();

        assertThat(recipes).hasSize(1).containsEntry(WATER_SPAWN, built);
    }

    @Test
    void anUnknownCastKindIsSkippedWithoutFallingBackToABean() {
        when(magicRepository.getAllMagic()).thenReturn(List.of(
                magicInfo(6L, "water_slime_swarm", WATER_SPAWN, "Sprinkle", "water_slime", "WaterSlime")));

        assertThat(parser.getAllMagicRecipeMap()).isEmpty();
        verify(applicationContext, never()).containsBean(anyString());
        verify(databaseMagicFactory, never()).create(any(), any());
    }

    @Test
    void aMagicBuiltFromDataIsAlsoFoundByName() {
        Magic built = stubMagic();
        MagicInfoDto waterSlimeSwarm =
                magicInfo(7L, "water_slime_swarm", WATER_SPAWN, "Spawn", "water_slime", "WaterSlime");
        when(magicRepository.getAllMagic()).thenReturn(List.of(waterSlimeSwarm));
        when(databaseMagicFactory.create(waterSlimeSwarm, CastKind.Spawn)).thenReturn(Optional.of(built));

        assertThat(parser.parseMagicForBot("water_slime_swarm")).isSameAs(built);
    }

    private Magic bean(String magicName) {
        Magic magic = stubMagic();
        when(applicationContext.containsBean(magicName)).thenReturn(true);
        when(applicationContext.getBean(magicName, Magic.class)).thenReturn(magic);
        return magic;
    }

    private static Magic stubMagic() {
        return new Magic(CardType.Shoot) {
            @Override
            public void run(GameContext gameContext, Master master, Vector3 position) {
            }
        };
    }

    private static MagicInfoDto magicInfo(long id,
                                          String name,
                                          List<CardType> cards,
                                          String castKind,
                                          String gameObjectName,
                                          String prefabName) {
        return new MagicInfoDto(id, name, cards, castKind, gameObjectName, prefabName);
    }
}
