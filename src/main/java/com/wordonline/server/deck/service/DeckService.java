package com.wordonline.server.deck.service;

import com.wordonline.server.deck.domain.DeckInfo;
import com.wordonline.server.deck.dto.*;
import com.wordonline.server.deck.repository.DeckRepository;
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
        deckRepository.saveCardToUser(userId, 1, 3);
        deckRepository.saveCardToUser(userId, 2, 3);
        deckRepository.saveCardToUser(userId, 3, 3);
        deckRepository.saveCardToUser(userId, 4, 3);
        deckRepository.saveCardToUser(userId, 5, 3);
        deckRepository.saveCardToUser(userId, 6, 3);
        deckRepository.saveCardToUser(userId, 7, 3);
        deckRepository.saveCardToUser(userId, 8, 3);
        deckRepository.saveCardToUser(userId, 9, 3);

        deckRepository.getCardPool(userId);
    }

    @Cacheable("cards")
    public Map<Long, CardDto> getCards() {
        return deckRepository.getCards()
                .stream()
                .collect(Collectors.toMap(
                        CardDto::id,
                        Function.identity()
                ));
    }

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
            throw new IllegalArgumentException("not authorization");
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