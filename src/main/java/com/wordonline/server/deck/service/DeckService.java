package com.wordonline.server.deck.service;

import com.wordonline.server.deck.domain.DeckInfo;
import com.wordonline.server.deck.dto.*;
import com.wordonline.server.deck.repository.DeckRepository;
import com.wordonline.server.game.domain.magic.CardType;
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

    public void initializeCard(long userId) {
        for (int i = 1; i <= 9; i++) {
            deckRepository.saveCardToUser(userId, i, 3);
        }

        long deckId = deckRepository.saveDeck(userId, "기본 덱");

        for (int i = 1; i <= 9; i++) {
            deckRepository.saveCardToDeck(deckId, i,
                    i==6?2:1);
        }
        deckRepository.setSelectDeck(userId, deckId);
    }

    @Transactional(readOnly = true)
    public boolean hasSelectedDeck(long userId) {
        return deckRepository.getSelectedDeckId(userId)
                .isPresent();
    }

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

    @Cacheable("cards")
    @Transactional(readOnly = true)
    public Map<Long, CardDto> getCards() {
        return deckRepository.getCards()
                .stream()
                .collect(Collectors.toMap(
                        CardDto::id,
                        Function.identity()
                ));
    }

    @Transactional(readOnly = true)
    public List<DeckResponseDto> getDecks(long userId){
        List<DeckResponseDto> responseDtos = new ArrayList<>();
        deckRepository.getDecks(userId)
            .forEach(
                deckCardDto -> {
                    if (responseDtos.isEmpty() || responseDtos.getLast().id() != deckCardDto.deckId()) {
                        responseDtos.add(
                                new DeckResponseDto(deckCardDto.deckId(), deckCardDto.deckName(), new ArrayList<>())
                        );
                    }

                    responseDtos.getLast().cards().addAll(
                            Stream.generate(() -> new CardDto(deckCardDto.cardId(), deckCardDto.cardName()))
                                    .limit(deckCardDto.count())
                                    .toList()
                    );
                }
            );

        return responseDtos;
    }

    public DeckResponseDto saveDeck(long userId, DeckRequestDto deckRequestDto) {
        long deckId = deckRepository.saveDeck(userId, deckRequestDto.name());

        deckRequestDto.cardIds().stream()
            .collect(Collectors.groupingBy(
                    Long::longValue,
                    Collectors.counting()
            ))
            .forEach((key, value) -> deckRepository.saveCardToDeck(
                    deckId,
                    key,
                    value.intValue()
            ));
        return getDeck(deckId);
    }

    public DeckResponseDto updateDeck(long userId, long deckId, DeckRequestDto deckRequestDto) {

        DeckInfo deckInfo = deckRepository.getDeckInfo(deckId).orElseThrow(
                () -> new IllegalArgumentException("deck not found")
        );
        if (deckInfo.userId() != userId) {
            throw new IllegalArgumentException("Not authorized");
        }

        deckRepository.deleteCardInDeck(deckId);
        deckRequestDto.cardIds().stream()
                .collect(Collectors.groupingBy(
                        Long::longValue,
                        Collectors.counting()
                ))
                .forEach((key, value) -> {
                    deckRepository.saveCardToDeck(
                            deckId,
                            key,
                            value.intValue()
                    );
                });
        return getDeck(deckId);
    }

    public void selectDeck(long userId, long deckId) {
        DeckInfo deckInfo = deckRepository.getDeckInfo(deckId)
                .orElseThrow(() -> new IllegalArgumentException("not found deck"));

        if (deckInfo.userId() != userId)
            throw new IllegalArgumentException("illegal deck");

        deckRepository.setSelectDeck(userId, deckId);
    }

    @Transactional(readOnly = true)
    public DeckResponseDto getDeck(long deckId) {
        List<DeckCardDto> deckCardDtos = deckRepository.getDeck(deckId);
        DeckResponseDto responseDto = new DeckResponseDto(
                deckCardDtos.getFirst().deckId(),
                deckCardDtos.getFirst().deckName(),
                new ArrayList<>());
        deckCardDtos.forEach(
                        deckCardDto -> {
                            responseDto.cards().addAll(
                                    Stream.generate(() -> new CardDto(deckCardDto.cardId(), deckCardDto.cardName()))
                                            .limit(deckCardDto.count())
                                            .toList()
                            );
                        }
                );

        return responseDto;
    }

    @Transactional(readOnly = true)
    public CardPoolDto getCardPool(long userId) {
        CardPoolDto cardPoolDto = new CardPoolDto(new ArrayList<>());
        deckRepository.getCardPool(userId)
            .forEach(
                cardsDto -> {
                    cardPoolDto.cards().addAll(
                            Stream.generate(() -> new CardDto(cardsDto.getId(), cardsDto.getName()))
                                    .limit(cardsDto.getCount())
                                    .toList()
                    );
                }
            );
        return cardPoolDto;
    }
}