package com.wordonline.server.deck.service;

import com.wordonline.server.deck.dto.*;
import com.wordonline.server.deck.repository.DeckRepository;
import com.wordonline.server.bot.domain.BotParticipant;
import com.wordonline.server.bot.service.BotPersonaService;
import com.wordonline.server.game.domain.magic.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class DeckService {

    private final DeckRepository deckRepository;
    private final BotPersonaService botPersonaService;

    @Transactional(readOnly = true)
    public List<CardType> getSelectedCards(long userId) {
        return mapToCardType(
                deckRepository.getSelectedDeck(userId)
        );
    }

    @Transactional(readOnly = true)
    public List<CardType> getParticipantCards(long participantId) {
        if (BotParticipant.isBot(participantId)) {
            return getBotPersonaCards(BotParticipant.personaId(participantId));
        }
        return getSelectedCards(participantId);
    }

    @Transactional(readOnly = true)
    public List<CardType> getBotPersonaCards(long botPersonaId) {
        long deckId = botPersonaService.findOrDefault(botPersonaId).deckId();
        if (deckId <= 0) {
            return List.of();
        }
        return mapToCardType(deckRepository.getDeck(deckId));
    }

    public List<CardDto> getDeckCards(long deckId) {
        return mapToCardDto(
                deckRepository.getDeck(deckId)
        );
    }

    @Transactional(readOnly = true)
    public List<CardDto> getParticipantDeckCards(long participantId) {
        if (BotParticipant.isBot(participantId)) {
            long deckId = botPersonaService.findByParticipantIdOrDefault(participantId).deckId();
            if (deckId <= 0) {
                return List.of();
            }
            return getDeckCards(deckId);
        }
        return getDeckCards(
                deckRepository.getSelectedDeckId(participantId)
                        .orElseThrow(() -> new IllegalArgumentException("Deck Not Found"))
        );
    }

    private List<CardDto> mapToCardDto(List<CardsDto> cardsDtos) {
        List<CardDto> cardDtos = new ArrayList<>();
        cardsDtos.forEach(
                cardsDto ->
                        cardDtos.addAll(
                                Stream.generate(() -> new CardDto(cardsDto))
                                        .limit(cardsDto.getCount())
                                        .toList()
                        )
        );
        return cardDtos;
    }

    private List<CardType> mapToCardType(List<CardsDto> cardDtos) {
        List<CardType> cardTypes = new ArrayList<>();
        cardDtos.forEach(
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
