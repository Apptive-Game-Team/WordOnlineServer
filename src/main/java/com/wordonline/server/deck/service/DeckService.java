package com.wordonline.server.deck.service;

import com.wordonline.server.deck.domain.DeckInfo;
import com.wordonline.server.deck.dto.*;
import com.wordonline.server.deck.repository.DeckRepository;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.service.LocalizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;

    @Transactional(readOnly = true)
    public List<CardType> getSelectedCards(long userId) {
        List<CardType> cardTypes = new ArrayList<>();
        deckRepository.getSelectedDeck(userId)
                .forEach(
                        cardsDto ->
                        cardTypes.addAll(
                                Stream.generate(cardsDto::getName)
                                        .limit(cardsDto.getCount())
                                        .toList()
                        )
                );
        return cardTypes;
    }
}