package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class MagicMetadataService {

    private final DatabaseMagicParser magicParser;
    private final TagRepository tagRepository;
    private final Map<Long, Set<String>> magicTagsCache = new ConcurrentHashMap<>();

    public Map<List<CardType>, Magic> getRecipeMap() {
        return magicParser.getAllMagicRecipeMap();
    }

    public Optional<Magic> findMagic(List<CardType> cards) {
        return Optional.ofNullable(magicParser.parseMagicForBot(cards));
    }

    public Set<String> getMagicTags(long magicId) {
        return magicTagsCache.computeIfAbsent(magicId, tagRepository::getMagicTags);
    }
}
