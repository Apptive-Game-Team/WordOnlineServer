package com.wordonline.server.deck.controller;

import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.deck.dto.CardPoolDto;
import com.wordonline.server.deck.dto.DeckRequestDto;
import com.wordonline.server.deck.dto.DeckResponseDto;
import com.wordonline.server.deck.service.DeckService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/users/mine")
@RestController
@RequiredArgsConstructor
public class DeckController {

    private final DeckService deckService;

    @GetMapping("/cards")
    public ResponseEntity<CardPoolDto> getCardPool(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
            deckService.getCardPool(principalDetails.getUid())
        );
    }

    @GetMapping("/decks")
    public ResponseEntity<List<DeckResponseDto>> getDecks(
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
                deckService.getDecks(principalDetails.getUid())
        );
    }

    @PostMapping("/decks")
    public ResponseEntity<DeckResponseDto> saveDeck(
            @Validated @RequestBody DeckRequestDto deckRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
            ) {
        return ResponseEntity.ok(
            deckService.saveDeck(
                    principalDetails.getUid(),
                    deckRequestDto
            )
        );
    }

    @PutMapping("/decks/{deckId}")
    public ResponseEntity<DeckResponseDto> updateDeck(
            @PathVariable Long deckId,
            @Validated @RequestBody DeckRequestDto deckRequestDto,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        return ResponseEntity.ok(
                deckService.updateDeck(
                        principalDetails.getUid(),
                        deckId,
                        deckRequestDto)
        );
    }

    @PostMapping("/decks/{deckId}")
    public ResponseEntity<String> selectDeck(
            @PathVariable Long deckId,
            @AuthenticationPrincipal PrincipalDetails principalDetails
    ) {
        deckService.selectDeck(
                principalDetails.getUid(),
                deckId
        );
        return ResponseEntity.ok(
                "Successfully Saved"
        );
    }
}
