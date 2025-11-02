package com.wordonline.server.deck.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.config.WebSecurityConfig;
import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.dto.CardPoolDto;
import com.wordonline.server.deck.dto.DeckRequestDto;
import com.wordonline.server.deck.dto.DeckResponseDto;
import com.wordonline.server.deck.service.DeckService;
import com.wordonline.server.game.domain.magic.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeckController.class)
@Import(WebSecurityConfig.class)
class DeckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DeckService deckService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

    private Authentication authentication;
    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        PrincipalDetails principalDetails = new PrincipalDetails(userId);
        authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
    }

    @Test
    @DisplayName("카드 풀 조회 테스트")
    void getCardPool() throws Exception {
        // given
        CardPoolDto cardPoolDto = new CardPoolDto(Collections.emptyList());
        given(deckService.getCardPool(userId)).willReturn(cardPoolDto);

        // when & then
        mockMvc.perform(get("/api/users/mine/cards")
                        .with(authentication(authentication)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("덱 목록 조회 테스트")
    void getDecks() throws Exception {
        // given
        DeckResponseDto deckResponseDto = new DeckResponseDto(1L, "test-deck", Collections.emptyList());
        given(deckService.getDecks(userId)).willReturn(Collections.singletonList(deckResponseDto));

        // when & then
        mockMvc.perform(get("/api/users/mine/decks")
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("test-deck"));
    }

    @Test
    @DisplayName("덱 저장 테스트")
    void saveDeck() throws Exception {
        // given
        List<Long> cardIds = LongStream.range(1, 11).boxed().collect(Collectors.toList());
        DeckRequestDto requestDto = new DeckRequestDto("new-deck", cardIds);
        DeckResponseDto responseDto = new DeckResponseDto(1L, "new-deck", Collections.emptyList());

        Map<Long, CardDto> cards = new HashMap<>();
        cards.put(1L, new CardDto(1L, CardType.Shoot));
        cards.put(2L, new CardDto(2L, CardType.Build));
        cards.put(3L, new CardDto(3L, CardType.Fire));
        cards.put(4L, new CardDto(4L, CardType.Water));
        cards.put(5L, new CardDto(5L, CardType.Rock));
        cards.put(6L, new CardDto(6L, CardType.Wind));
        cards.put(7L, new CardDto(7L, CardType.Lightning));
        cards.put(8L, new CardDto(8L, CardType.Nature));
        cards.put(9L, new CardDto(9L, CardType.Spawn));
        cards.put(10L, new CardDto(10L, CardType.Explode));

        given(deckService.getCards()).willReturn(cards);
        given(deckService.saveDeck(eq(userId), any(DeckRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(post("/api/users/mine/decks")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("new-deck"));
    }

    @Test
    @DisplayName("덱 수정 테스트")
    void updateDeck() throws Exception {
        // given
        long deckId = 1L;
        List<Long> cardIds = LongStream.range(1, 11).boxed().collect(Collectors.toList());
        DeckRequestDto requestDto = new DeckRequestDto("updated-deck", cardIds);
        DeckResponseDto responseDto = new DeckResponseDto(deckId, "updated-deck", Collections.emptyList());

        Map<Long, CardDto> cards = new HashMap<>();
        cards.put(1L, new CardDto(1L, CardType.Shoot));
        cards.put(2L, new CardDto(2L, CardType.Build));
        cards.put(3L, new CardDto(3L, CardType.Fire));
        cards.put(4L, new CardDto(4L, CardType.Water));
        cards.put(5L, new CardDto(5L, CardType.Rock));
        cards.put(6L, new CardDto(6L, CardType.Wind));
        cards.put(7L, new CardDto(7L, CardType.Lightning));
        cards.put(8L, new CardDto(8L, CardType.Nature));
        cards.put(9L, new CardDto(9L, CardType.Spawn));
        cards.put(10L, new CardDto(10L, CardType.Explode));

        given(deckService.getCards()).willReturn(cards);
        given(deckService.updateDeck(eq(userId), eq(deckId), any(DeckRequestDto.class))).willReturn(responseDto);

        // when & then
        mockMvc.perform(put("/api/users/mine/decks/{deckId}", deckId)
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(deckId))
                .andExpect(jsonPath("$.name").value("updated-deck"));
    }

    @Test
    @DisplayName("덱 선택 테스트")
    void selectDeck() throws Exception {
        // given
        long deckId = 1L;

        // when & then
        mockMvc.perform(post("/api/users/mine/decks/{deckId}", deckId)
                        .with(authentication(authentication)))
                .andExpect(status().isOk())
                .andExpect(content().string("Successfully Saved"));
    }
}
