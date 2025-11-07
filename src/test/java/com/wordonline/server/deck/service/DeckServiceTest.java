package com.wordonline.server.deck.service;

import com.wordonline.server.deck.domain.DeckInfo;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.dto.CardsDto;
import com.wordonline.server.deck.dto.DeckCardDto;
import com.wordonline.server.deck.dto.DeckRequestDto;
import com.wordonline.server.deck.dto.DeckResponseDto;
import com.wordonline.server.deck.repository.DeckRepository;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.service.LocalizationService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DeckServiceTest {

    @InjectMocks
    private DeckService deckService;

    @Mock
    private DeckRepository deckRepository;

    @Mock
    private LocalizationService localizationService;

@Disabled
    @Test
    @DisplayName("카드 초기화 테스트")
    void initializeCard() {
        // given
        long userId = 1L;
        ArgumentCaptor<Long> deckIdCaptor = ArgumentCaptor.forClass(Long.class);
        given(deckRepository.saveDeck(userId, "기본 덱")).willReturn(1L);

        // when
        deckService.initializeCard(userId);

        // then
        verify(deckRepository).setSelectDeck(eq(userId), deckIdCaptor.capture());
        assertThat(deckIdCaptor.getValue()).withFailMessage("Captured deckId was %d", deckIdCaptor.getValue()).isEqualTo(1L);
    }

    @Test
    @DisplayName("선택된 덱 존재 여부 확인 테스트 - 존재할 경우")
    void hasSelectedDeck_true() {
        // given
        long userId = 1L;
        given(deckRepository.getSelectedDeckId(userId)).willReturn(Optional.of(1L));

        // when
        boolean result = deckService.hasSelectedDeck(userId);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("선택된 덱 존재 여부 확인 테스트 - 존재하지 않을 경우")
    void hasSelectedDeck_false() {
        // given
        long userId = 1L;
        given(deckRepository.getSelectedDeckId(userId)).willReturn(Optional.empty());

        // when
        boolean result = deckService.hasSelectedDeck(userId);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("선택된 카드의 카드 타입 목록 조회 테스트")
    void getSelectedCards() {
        // given
        long userId = 1L;
        List<CardsDto> cardsDtos = List.of(new CardsDto(1L, CardType.Shoot, 2), new CardsDto(2L, CardType.Fire, 3));
        given(deckRepository.getSelectedDeck(userId)).willReturn(cardsDtos);

        // when
        List<CardType> cardTypes = deckService.getSelectedCards(userId);

        // then
        assertThat(cardTypes).hasSize(5);
        assertThat(cardTypes).contains(CardType.Shoot, CardType.Fire);
    }

    @Test
    @DisplayName("모든 카드 정보 조회 테스트")
    void getCards() {
        // given
        List<CardDto> cardDtos = List.of(new CardDto(1L, CardType.Shoot), new CardDto(2L, CardType.Fire));
        given(deckRepository.getCards()).willReturn(cardDtos);

        // when
        Map<Long, CardDto> cards = deckService.getCards();

        // then
        assertThat(cards).hasSize(2);
        assertThat(cards.get(1L).name()).isEqualTo(CardType.Shoot);
    }

    @Test
    @DisplayName("사용자의 모든 덱 정보 조회 테스트")
    void getDecks() {
        // given
        long userId = 1L;
        List<DeckCardDto> deckCardDtos = List.of(
                new DeckCardDto(1L, 1L, 1, "deck1", CardType.Shoot, CardType.Type.Magic),
                new DeckCardDto(1L, 2L, 1, "deck1", CardType.Fire, CardType.Type.Type),
                new DeckCardDto(2L, 3L, 1, "deck2", CardType.Water, CardType.Type.Type)
        );
        given(deckRepository.getDecks(userId)).willReturn(deckCardDtos);

        // when
        List<DeckResponseDto> decks = deckService.getDecks(userId);

        // then
        assertThat(decks).hasSize(2);
        assertThat(decks.get(0).name()).isEqualTo("deck1");
        assertThat(decks.get(0).cards()).hasSize(2);
        assertThat(decks.get(1).name()).isEqualTo("deck2");
        assertThat(decks.get(1).cards()).hasSize(1);
    }

    @Test
    @DisplayName("덱 저장 테스트")
    void saveDeck() {
        // given
        long userId = 1L;
        long deckId = 1L;
        DeckRequestDto requestDto = new DeckRequestDto("new-deck", List.of(1L, 1L, 2L));
        given(deckRepository.saveDeck(userId, "new-deck")).willReturn(deckId);
        given(deckRepository.getDeck(deckId)).willReturn(List.of(new DeckCardDto(deckId, 1L, 2, "new-deck", CardType.Shoot, CardType.Type.Magic), new DeckCardDto(deckId, 2L, 1, "new-deck", CardType.Fire, CardType.Type.Type)));

        // when
        DeckResponseDto responseDto = deckService.saveDeck(userId, requestDto);

        // then
        verify(deckRepository).saveCardToDeck(deckId, 1L, 2);
        verify(deckRepository).saveCardToDeck(deckId, 2L, 1);
        assertThat(responseDto.name()).isEqualTo("new-deck");
        assertThat(responseDto.cards()).hasSize(3);
    }

    @Test
    @DisplayName("덱 수정 테스트")
    void updateDeck() {
        // given
        long userId = 1L;
        long deckId = 1L;
        DeckRequestDto requestDto = new DeckRequestDto("updated-deck", List.of(3L, 4L));
        DeckInfo deckInfo = new DeckInfo(deckId, userId, "old-deck");
        given(deckRepository.getDeckInfo(deckId)).willReturn(Optional.of(deckInfo));
        given(deckRepository.getDeck(deckId)).willReturn(List.of(new DeckCardDto(deckId, 3L, 1, "updated-deck", CardType.Water, CardType.Type.Type), new DeckCardDto(deckId, 4L, 1, "updated-deck", CardType.Rock, CardType.Type.Type)));

        // when
        DeckResponseDto responseDto = deckService.updateDeck(userId, deckId, requestDto);

        // then
        verify(deckRepository).deleteCardInDeck(deckId);
        verify(deckRepository).setDeckName(deckId, "updated-deck");
        verify(deckRepository).saveCardToDeck(deckId, 3L, 1);
        verify(deckRepository).saveCardToDeck(deckId, 4L, 1);
        assertThat(responseDto.name()).isEqualTo("updated-deck");
        assertThat(responseDto.cards()).hasSize(2);
    }

    @Test
    @DisplayName("덱 수정 실패 테스트 - 권한 없음")
    void updateDeck_unauthorized() {
        // given
        long userId = 1L;
        long otherUserId = 2L;
        long deckId = 1L;
        DeckRequestDto requestDto = new DeckRequestDto("updated-deck", Collections.emptyList());
        DeckInfo deckInfo = new DeckInfo(deckId, otherUserId, "old-deck");
        given(deckRepository.getDeckInfo(deckId)).willReturn(Optional.of(deckInfo));
        given(localizationService.getMessage("error.not.authorized")).willReturn("Not authorized");

        // when & then
        assertThatThrownBy(() -> deckService.updateDeck(userId, deckId, requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Not authorized");
    }

    @Test
    @DisplayName("덱 선택 테스트")
    void selectDeck() {
        // given
        long userId = 1L;
        long deckId = 1L;
        DeckInfo deckInfo = new DeckInfo(deckId, userId, "test-deck");
        given(deckRepository.getDeckInfo(deckId)).willReturn(Optional.of(deckInfo));

        // when
        deckService.selectDeck(userId, deckId);

        // then
        verify(deckRepository).setSelectDeck(userId, deckId);
    }

    @Test
    @DisplayName("덱 선택 실패 테스트 - 존재하지 않는 덱")
    void selectDeck_notFound() {
        // given
        long userId = 1L;
        long deckId = 1L;
        given(deckRepository.getDeckInfo(deckId)).willReturn(Optional.empty());
        given(localizationService.getMessage("error.deck.not.found")).willReturn("not found deck");

        // when & then
        assertThatThrownBy(() -> deckService.selectDeck(userId, deckId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("not found deck");
    }

    @Test
    @DisplayName("덱 선택 실패 테스트 - 권한 없음")
    void selectDeck_unauthorized() {
        // given
        long userId = 1L;
        long otherUserId = 2L;
        long deckId = 1L;
        DeckInfo deckInfo = new DeckInfo(deckId, otherUserId, "test-deck");
        given(deckRepository.getDeckInfo(deckId)).willReturn(Optional.of(deckInfo));
        given(localizationService.getMessage("error.deck.illegal")).willReturn("illegal deck");

        // when & then
        assertThatThrownBy(() -> deckService.selectDeck(userId, deckId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("illegal deck");
    }

    @Test
    @DisplayName("덱 정보 조회 테스트")
    void getDeck() {
        // given
        long deckId = 1L;
        List<DeckCardDto> deckCardDtos = List.of(
                new DeckCardDto(deckId, 1L, 2, "test-deck", CardType.Shoot, CardType.Type.Magic),
                new DeckCardDto(deckId, 2L, 1, "test-deck", CardType.Fire, CardType.Type.Type)
        );
        given(deckRepository.getDeck(deckId)).willReturn(deckCardDtos);

        // when
        DeckResponseDto deck = deckService.getDeck(deckId);

        // then
        assertThat(deck.id()).isEqualTo(deckId);
        assertThat(deck.name()).isEqualTo("test-deck");
        assertThat(deck.cards()).hasSize(3);
    }

    @Test
    @DisplayName("카드 풀 조회 테스트")
    void getCardPool() {
        // given
        long userId = 1L;
        List<CardsDto> cardsDtos = List.of(new CardsDto(1L, CardType.Shoot, 3), new CardsDto(2L, CardType.Fire, 5));
        given(deckRepository.getCardPool(userId)).willReturn(cardsDtos);

        // when
        com.wordonline.server.deck.dto.CardPoolDto cardPool = deckService.getCardPool(userId);

        // then
        assertThat(cardPool.cards()).hasSize(8);
    }
}