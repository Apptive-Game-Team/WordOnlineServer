package com.wordonline.server.game.domain.magic.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.CastKind;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.dto.MagicInfoDto;
import com.wordonline.server.game.repository.MagicRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMagicParser implements MagicParser {

    public static final long INVALID_MAGIC_ID = 0;

    private final Map<List<CardType>, Magic> magicHashMap = new ConcurrentHashMap<>();
    private final Map<Long, Magic> magicIdMap = new ConcurrentHashMap<>();
    private final Map<String, Magic> magicNameMap = new ConcurrentHashMap<>();

    private final MagicRepository magicRepository;
    private final DatabaseMagicFactory databaseMagicFactory;
    private final ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        magicRepository.getAllMagic()
                .forEach(magicInfoDto -> resolve(magicInfoDto)
                        .ifPresent(magic -> {
                            magic.id = magicInfoDto.id();
                            magic.name = magicInfoDto.name();
                            magicHashMap.put(convertToKey(magicInfoDto.cards()), magic);
                            magicIdMap.put(magic.id, magic);
                            magicNameMap.put(magic.name, magic);
                        }));
        log.info("[Magic:Loaded]: {}", magicHashMap.values().stream().map(magic -> magic.name).toList());
    }

    /**
     * {@code cast_kind} 가 비어 있거나 {@link CastKind#Code} 면 지금처럼 {@code magics.name} 과
     * 같은 이름의 bean 을 찾는다. 그 밖이면 bean 을 찾지 않고 데이터로 계열별 마법을 만든다.
     *
     * <p>모르는 {@code cast_kind} 는 계열이 없으므로 경고하고 그 마법만 건너뛴다. bean 이 없을
     * 때와 같은 처리다.
     */
    private Optional<Magic> resolve(MagicInfoDto magicInfoDto) {
        String castKindName = magicInfoDto.castKind();
        if (castKindName == null || castKindName.isBlank()) {
            return findBean(magicInfoDto.name());
        }

        Optional<CastKind> castKind = CastKind.of(castKindName);
        if (castKind.isEmpty()) {
            log.warn("[Magic:Loading] magic ({}) has unknown cast_kind ({})",
                    magicInfoDto.name(), castKindName);
            return Optional.empty();
        }

        if (castKind.get() == CastKind.Code) {
            return findBean(magicInfoDto.name());
        }

        return databaseMagicFactory.create(magicInfoDto, castKind.get());
    }

    private Optional<Magic> findBean(String magicName) {
        if (!applicationContext.containsBean(magicName)) {
            log.warn("[Magic:Loading] magic ({}) bean is not available", magicName);
            return Optional.empty();
        }
        return Optional.of(applicationContext.getBean(magicName, Magic.class));
    }

    public void invalidateCache() {
        magicHashMap.clear();
        magicIdMap.clear();
        magicNameMap.clear();
    }

    private List<CardType> convertToKey(List<CardType> cards) {
        return List.copyOf(cards.stream().sorted().toList());
    }

    @Override
    public Magic parseMagic(long userId, List<CardType> cards) {
        Magic magic = getMagicByCards(cards);
        if (magic == null) {
            return null;
        }

        if (!magicRepository.existUserMagic(userId, magic.id)) {
            log.info("[Magic:NotOwned] User {} does not have Magic(id:{}) yet. Rejecting magic cast.", userId, magic.id);
            return null;
        }

        return magic;
    }

    public Magic parseMagicForBot(List<CardType> cards) {
        return getMagicByCards(cards);
    }

    public Magic parseMagicForBot(String magicName) {
        if (magicHashMap.isEmpty()) {
            init();
        }

        if (magicName == null || magicName.isBlank()) {
            return null;
        }

        // 계열이 데이터로 옮겨간 마법은 bean 이 없다. 등록된 마법을 이름으로 먼저 찾고,
        // 없을 때만 지금처럼 bean 을 찾는다 - DB 에 행이 없는 bean 이 아직 남아 있다.
        Magic registered = magicNameMap.get(magicName);
        if (registered != null) {
            return registered;
        }

        if (!applicationContext.containsBean(magicName)) {
            log.warn("[MagicNotFound] No magic bean found for name: {}", magicName);
            return null;
        }

        Magic magic = applicationContext.getBean(magicName, Magic.class);
        if (magic.id <= 0) {
            log.warn("[MagicIdMissing] Magic '{}' has non-positive id ({}).", magicName, magic.id);
        }
        return magic;
    }

    public Magic parseMagicForBot(long magicId) {
        if (magicIdMap.isEmpty()) {
            init();
        }

        if (magicId <= INVALID_MAGIC_ID) {
            return null;
        }

        Magic magic = magicIdMap.get(magicId);
        if (magic == null) {
            log.warn("[MagicNotFound] No magic mapped for id: {}", magicId);
        }
        return magic;
    }

    public Collection<List<CardType>> getAllMagicRecipes() {
        if (magicHashMap.isEmpty()) {
            init();
        }
        return magicHashMap.keySet();
    }

    public Map<List<CardType>, Magic> getAllMagicRecipeMap() {
        if (magicHashMap.isEmpty()) {
            init();
        }
        return Map.copyOf(magicHashMap);
    }

    private Magic getMagicByCards(List<CardType> cards) {
        if (magicHashMap.isEmpty()) {
            init();
        }

        List<CardType> key = cards.stream().sorted().toList();
        Magic magic = magicHashMap.get(key);

        if (magic == null) {
            log.warn("[MagicNotFound] No magic mapped for cards: {} (sorted keys: {})", cards, key);
        }

        return magic;
    }
}
