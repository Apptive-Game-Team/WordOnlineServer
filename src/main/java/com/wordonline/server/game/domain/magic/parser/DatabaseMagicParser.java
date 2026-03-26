package com.wordonline.server.game.domain.magic.parser;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.annotation.PostConstruct;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
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

    private final MagicRepository magicRepository;
    private final ApplicationContext applicationContext;

    @PostConstruct
    private void init() {
        magicRepository.getAllMagic()
                .forEach(magicInfoDto -> {
                    if (!applicationContext.containsBean(magicInfoDto.name())) {
                        return;
                    }

                    Magic magic = applicationContext.getBean(magicInfoDto.name(), Magic.class);
                    magic.id = magicInfoDto.id();
                    magicHashMap.put(convertToKey(magicInfoDto.cards()), magic);
                    magicIdMap.put(magic.id, magic);
                });
    }

    public void invalidateCache() {
        magicHashMap.clear();
        magicIdMap.clear();
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
