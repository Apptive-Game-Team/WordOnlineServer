package com.wordonline.server.game.service;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.input.InputHandleEvent;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.input.InputResponseDto;
import com.wordonline.server.game.dto.input.InputResultCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.concurrent.SubmissionPublisher;

import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MagicInputHandler {

    private final DatabaseMagicParser magicParser;

    private final SubmissionPublisher<InputHandleEvent> inputEventPublisher = new SubmissionPublisher<>();

    public void subscribe(Flow.Subscriber<InputHandleEvent> subscriber) {
        inputEventPublisher.subscribe(subscriber);
    }

    public InputResponseDto handleInput(GameContext gameContext, long userId, InputRequestDto inputRequestDto) {
        Master master = gameContext.getSessionObject().getUserSide(userId);
        PlayerData playerData = gameContext.getGameSessionData().getPlayerData(master);

        if (!playerData.validCardsUse(inputRequestDto.getCards())) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_LACK_OF_CARD));
            return new InputResponseDto("Cannot use cards.", false, playerData.mana, inputRequestDto.getId(), -1);
        }

        Magic magic = magicParser.parseMagic(userId, inputRequestDto.getCards());

        if (magic == null) {
            log.trace("{}: {} is not valid : could not parse", master, inputRequestDto.getCards());
            playerData.useCards(inputRequestDto.getCards());
            gameContext.getGameSessionData().getCardDeck(master).returnCards(inputRequestDto.getCards());
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_MAGIC));
            return new InputResponseDto("Invalid magic.", true, playerData.mana, inputRequestDto.getId(), -1);
        } else if (GameConfig.PLAYER_POSITION.get(master).distance(inputRequestDto.getPosition()) > gameContext.getParameters().getValue(magic.magicType.name(), "range")) {
            log.trace("{}: {} is not valid : too far", master, inputRequestDto.getCards());
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_PLACE));
            return new InputResponseDto("Target is out of range.", false, playerData.mana, inputRequestDto.getId(), -1);
        }

        boolean valid = playerData.useCards(inputRequestDto.getCards());

        if (!valid) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_INSUFFICIENT_MANA));
            return new InputResponseDto("Insufficient mana.", false, playerData.mana, inputRequestDto.getId(), -1);
        }

        magic.run(gameContext, master, inputRequestDto.getPosition());
        gameContext.getGameSessionData().getCardDeck(master).returnCards(inputRequestDto.getCards());

        inputEventPublisher.submit(new InputHandleEvent(master, InputResultCode.SUCCESS, magic.id));
        return new InputResponseDto(true, playerData.mana, inputRequestDto.getId(), magic.id);
    }

    public InputResponseDto handleBotMagicInput(GameContext gameContext,
                                                Master master,
                                                List<CardType> cards,
                                                Vector3 position) {
        Magic magic = magicParser.parseMagicForBot(cards);
        return handleBotMagicInput(gameContext, master, magic, position);
    }

    public InputResponseDto handleBotMagicInput(GameContext gameContext,
                                                Master master,
                                                Magic magic,
                                                Vector3 position) {
        PlayerData playerData = gameContext.getGameSessionData().getPlayerData(master);

        if (magic == null) {
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_MAGIC));
            return new InputResponseDto("invalid bot magic", false, playerData.mana, -1, -1);
        }

        if (GameConfig.PLAYER_POSITION.get(master).distance(position) > gameContext.getParameters().getValue(magic.magicType.name(), "range")) {
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_PLACE));
            return new InputResponseDto("invalid place", false, playerData.mana, -1, -1);
        }

        int manaCost = (int) gameContext.getParameters().getValue(magic.magicType.name(), "mana_cost");
        if (playerData.mana < manaCost) {
            inputEventPublisher.submit(InputHandleEvent.fail(master, InputResultCode.FAIL_INSUFFICIENT_MANA));
            return new InputResponseDto("insufficient mana", false, playerData.mana, -1, -1);
        }

        playerData.mana -= manaCost;
        magic.run(gameContext, master, position);
        inputEventPublisher.submit(new InputHandleEvent(master, InputResultCode.SUCCESS, magic.id));
        return new InputResponseDto(true, playerData.mana, -1, magic.id);
    }
}
