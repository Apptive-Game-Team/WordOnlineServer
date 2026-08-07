package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.input.InputResponseDto;
import com.wordonline.server.game.dto.input.InputResultCode;
import com.wordonline.server.game.dto.input.MagicUseRequestDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MagicInputHandlerPayloadTest {

    private final MagicInputHandler handler = new MagicInputHandler(mock(DatabaseMagicParser.class));
    private final PlayerData playerData = new PlayerData(mock(ManaCharger.class), mock(Parameters.class));

    @Test
    void missingPositionIsRejectedWithoutTouchingCards() {
        MagicUseRequestDto request = new MagicUseRequestDto("useMagic", List.of(CardType.Fire), 7, null);

        InputResponseDto response = handler.handleInput(gameContext(), 1L, request);

        assertThat(response.resultCode()).isEqualTo(InputResultCode.FAIL_INVALID_MAGIC);
        assertThat(response.valid()).isFalse();
        assertThat(playerData.cards).containsExactly(CardType.Fire);
    }

    @Test
    void missingCardsIsRejectedWithoutTouchingCards() {
        MagicUseRequestDto request = new MagicUseRequestDto("useMagic", null, 7, new Vector3(1f, 0f, 1f));

        InputResponseDto response = handler.handleInput(gameContext(), 1L, request);

        assertThat(response.resultCode()).isEqualTo(InputResultCode.FAIL_INVALID_MAGIC);
        assertThat(response.valid()).isFalse();
        assertThat(playerData.cards).containsExactly(CardType.Fire);
    }

    private GameContext gameContext() {
        playerData.cards.add(CardType.Fire);
        GameContext gameContext = mock(GameContext.class, RETURNS_DEEP_STUBS);
        when(gameContext.getSessionObject().getUserSide(1L)).thenReturn(Master.LeftPlayer);
        when(gameContext.getGameSessionData().getPlayerData(any())).thenReturn(playerData);
        return gameContext;
    }
}
