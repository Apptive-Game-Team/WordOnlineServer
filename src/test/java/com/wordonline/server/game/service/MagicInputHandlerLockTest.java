package com.wordonline.server.game.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.input.MagicUseRequestDto;

// The game loop holds the GameContext monitor while it runs a frame, so every input thread that
// touches game state must hold it too. Both entry points below run off the loop thread.
class MagicInputHandlerLockTest {

    private final MagicInputHandler magicInputHandler = new MagicInputHandler(mock(DatabaseMagicParser.class));

    @Test
    void handleInputHoldsGameContextLock() {
        AtomicBoolean lockHeld = new AtomicBoolean();
        GameContext gameContext = gameContext(lockHeld);

        magicInputHandler.handleInput(gameContext, 1L,
                new MagicUseRequestDto("useMagic", List.of(CardType.Fire), 1, Vector3.ZERO));

        assertThat(lockHeld).isTrue();
    }

    @Test
    void handleBotPlayerInputHoldsGameContextLock() {
        AtomicBoolean lockHeld = new AtomicBoolean();
        GameContext gameContext = gameContext(lockHeld);

        InputRequestDto inputRequestDto = new InputRequestDto();
        inputRequestDto.setType("useMagic");
        inputRequestDto.setCards(List.of(CardType.Fire));
        inputRequestDto.setPosition(Vector3.ZERO);

        magicInputHandler.handleBotPlayerInput(gameContext, Master.LeftPlayer, inputRequestDto);

        assertThat(lockHeld).isTrue();
    }

    // The player holds no cards, so both handlers bail out right after this lookup.
    private GameContext gameContext(AtomicBoolean lockHeld) {
        GameContext gameContext = mock(GameContext.class);
        SessionObject sessionObject = mock(SessionObject.class);
        GameSessionData gameSessionData = mock(GameSessionData.class);
        PlayerData playerData = new PlayerData(null, mock(Parameters.class));

        when(gameContext.getSessionObject()).thenReturn(sessionObject);
        when(sessionObject.getUserSide(1L)).thenReturn(Master.LeftPlayer);
        when(gameSessionData.getPlayerData(Master.LeftPlayer)).thenReturn(playerData);
        when(gameContext.getGameSessionData()).thenAnswer(invocation -> {
            lockHeld.set(Thread.holdsLock(gameContext));
            return gameSessionData;
        });
        return gameContext;
    }
}
