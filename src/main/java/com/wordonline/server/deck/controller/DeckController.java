package com.wordonline.server.deck.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.deck.dto.CardDto;
import com.wordonline.server.deck.dto.CardPoolDto;
import com.wordonline.server.deck.dto.DeckRequestDto;
import com.wordonline.server.deck.dto.DeckResponseDto;
import com.wordonline.server.game.domain.magic.CardType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/users/mine")
@RestController
public class DeckController {

    @GetMapping("/cards")
    public ResponseEntity<CardPoolDto> getCardPool(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
          new CardPoolDto(
                  List.of(
                          new CardDto(CardType.Fire),
                          new CardDto(CardType.Fire),
                          new CardDto(CardType.Fire),
                          new CardDto(CardType.Explode),
                          new CardDto(CardType.Explode),
                          new CardDto(CardType.Explode),
                          new CardDto(CardType.Leaf),
                          new CardDto(CardType.Leaf),
                          new CardDto(CardType.Leaf),
                          new CardDto(CardType.Lightning),
                          new CardDto(CardType.Lightning),
                          new CardDto(CardType.Lightning),
                          new CardDto(CardType.Rock),
                          new CardDto(CardType.Rock),
                          new CardDto(CardType.Rock),
                          new CardDto(CardType.Shoot),
                          new CardDto(CardType.Shoot),
                          new CardDto(CardType.Shoot)
                  )
          )
        );
    }

    @GetMapping("/decks")
    public ResponseEntity<List<DeckResponseDto>> getDecks(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
                List.of(
                        new DeckResponseDto(
                                1, "Fire Shoot Deck",
                                List.of(
                                        new CardDto(CardType.Fire),
                                        new CardDto(CardType.Fire),
                                        new CardDto(CardType.Fire),
                                        new CardDto(CardType.Shoot),
                                        new CardDto(CardType.Shoot),
                                        new CardDto(CardType.Shoot)
                                )
                        ),
                        new DeckResponseDto(
                                2, "Leaf Shoot Deck",
                                List.of(
                                        new CardDto(CardType.Leaf),
                                        new CardDto(CardType.Leaf),
                                        new CardDto(CardType.Leaf),
                                        new CardDto(CardType.Shoot),
                                        new CardDto(CardType.Shoot),
                                        new CardDto(CardType.Shoot)
                                )
                        )
                )
        );
    }

    @PostMapping("/decks")
    public ResponseEntity<DeckResponseDto> saveDeck(
            @RequestBody DeckRequestDto deckRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
            ) {
        return ResponseEntity.ok(
            new DeckResponseDto(
                    123,
                    deckRequestDto.name(),
                    deckRequestDto.cards()
            )
        );
    }

    @PutMapping("/decks/{deckId}")
    public ResponseEntity<DeckResponseDto> updateDeck(
            @PathVariable Long deckId,
            @RequestBody DeckRequestDto deckRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
                new DeckResponseDto(
                        deckId,
                        deckRequestDto.name(),
                        deckRequestDto.cards()
                )
        );
    }

    @PostMapping("/decks/{deckId}")
    public ResponseEntity<String> selectDeck(
            @PathVariable Long deckId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
                "Successfully Saved"
        );
    }
}
