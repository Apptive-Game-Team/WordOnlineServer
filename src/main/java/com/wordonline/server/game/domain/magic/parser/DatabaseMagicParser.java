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

    private final Map<List<CardType>, Magic> magicHashMap = new ConcurrentHashMap<>();

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
                    magicHashMap.put(
                            convertToKey(magicInfoDto.cards()),
                            magic
                    );
                });
    }

    public void invalidateCache() {
        magicHashMap.clear();
    }

    private List<CardType> convertToKey(List<CardType> cards) {
        return List.copyOf(
                        cards.stream()
                                .sorted()
                                .toList()
                );
    }

    @Override
    public Magic parseMagic(List<CardType> cards) {
        if (magicHashMap.isEmpty()) {
            init();
        }

        List<CardType> key = List.copyOf(cards.stream().sorted().toList());
        magicHashMap.forEach((k, value) ->
                log.info("key: {}, magicName: {}, id: {}", k, value.getClass().getSimpleName(), value.id)
        );
        Magic magic = magicHashMap.get(key);

        if (magic == null) {
            log.info("cards: {}, keys: {}", cards, key);
            return null;
        }

        return magic;
    }

    public Collection<List<CardType>> getAllMagicRecipes() {
        if (magicHashMap.isEmpty()) {
            init();
        }
        return magicHashMap.keySet();
    }

}
