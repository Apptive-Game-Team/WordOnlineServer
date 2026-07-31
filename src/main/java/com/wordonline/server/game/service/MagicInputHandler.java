package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.input.*;
import com.wordonline.server.game.util.SynchronousFlowPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Flow;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Scope("prototype")
@Component
public class MagicInputHandler {

    private final DatabaseMagicParser magicParser;

    private final SynchronousFlowPublisher<InputHandleEvent> inputEventPublisher = new SynchronousFlowPublisher<>();

    public void subscribe(Flow.Subscriber<InputHandleEvent> subscriber) {
        inputEventPublisher.subscribe(subscriber);
    }

    // Runs on the STOMP inbound thread: take the session lock the game loop holds during update()
    // so card/mana deduction and the whole magic execution cannot interleave with a frame.
    public InputResponseDto handleInput(GameContext gameContext, long userId, MagicUseRequestDto inputRequestDto) {
        synchronized (gameContext) {
            return handleInputLocked(gameContext, userId, inputRequestDto);
        }
    }

    private InputResponseDto handleInputLocked(GameContext gameContext, long userId, MagicUseRequestDto inputRequestDto) {
        Master master = gameContext.getSessionObject().getUserSide(userId);
        PlayerData playerData = gameContext.getGameSessionData().getPlayerData(master);

        if (!playerData.validCardsUse(inputRequestDto.getCards())) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_LACK_OF_CARD));
            return new InputResponseDto("Cannot use cards.", false, InputResultCode.FAIL_LACK_OF_CARD, playerData.mana, inputRequestDto.getId(), -1);
        }

        Magic magic = magicParser.parseMagic(userId, inputRequestDto.getCards());

        if (magic == null) {
            log.trace("{}: {} is not valid : could not parse", master, inputRequestDto.getCards());
            playerData.useCards(inputRequestDto.getCards());
            gameContext.getGameSessionData().getCardDeck(master).returnCards(inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_MAGIC));
            return new InputResponseDto("Invalid magic.", true, InputResultCode.FAIL_INVALID_MAGIC, playerData.mana, inputRequestDto.getId(), -1);
        }

        Vector3 castOrigin = gameContext.findPlayerGameObject(master)
                .map(gameObject -> new Vector3(gameObject.getPosition()))
                .orElse(null);
        if (castOrigin == null) {
            log.trace("{}: {} is not valid : caster not found", master, inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_PLACE));
            return new InputResponseDto("Caster is not found.", false, InputResultCode.FAIL_INVALID_PLACE, playerData.mana, inputRequestDto.getId(), -1);
        }
        Vector3 castPosition = clampToRange(
                castOrigin,
                clampToMapBounds(inputRequestDto.getPosition()),
                gameContext.getParameters().getValue(magic.magicType.name(), "range")
        );

        boolean valid = playerData.useCards(inputRequestDto.getCards());

        if (!valid) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INSUFFICIENT_MANA));
            return new InputResponseDto("Insufficient mana.", false, InputResultCode.FAIL_INSUFFICIENT_MANA, playerData.mana, inputRequestDto.getId(), -1);
        }

        magic.run(gameContext, master, castOrigin, castPosition);
        gameContext.getGameSessionData().getCardDeck(master).returnCards(inputRequestDto.getCards());

        inputEventPublisher.publish(new InputHandleEvent(master, InputResultCode.SUCCESS, magic.id));
        return new InputResponseDto(true, InputResultCode.SUCCESS, playerData.mana, inputRequestDto.getId(), magic.id);
    }

    // Runs on the bot executor thread (BotAgentSystem submits ticks off the loop thread).
    public InputResponseDto handleBotPlayerInput(GameContext gameContext, Master master, InputRequestDto inputRequestDto) {
        synchronized (gameContext) {
            return handleBotPlayerInputLocked(gameContext, master, inputRequestDto);
        }
    }

    private InputResponseDto handleBotPlayerInputLocked(GameContext gameContext, Master master, InputRequestDto inputRequestDto) {
        PlayerData playerData = gameContext.getGameSessionData().getPlayerData(master);

        if (!playerData.validCardsUse(inputRequestDto.getCards())) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_LACK_OF_CARD));
            return new InputResponseDto("Cannot use cards.", false, InputResultCode.FAIL_LACK_OF_CARD,  playerData.mana, inputRequestDto.getId(), -1);
        }

        Magic magic = magicParser.parseMagicForBot(inputRequestDto.getCards());

        if (magic == null) {
            log.trace("{}: {} is not valid : could not parse", master, inputRequestDto.getCards());
            playerData.useCards(inputRequestDto.getCards());
            gameContext.getGameSessionData().getCardDeck(master).returnCards(inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_MAGIC));
            return new InputResponseDto("Invalid magic.", true, InputResultCode.FAIL_INVALID_MAGIC, playerData.mana, inputRequestDto.getId(), -1);
        }

        Vector3 castOrigin = gameContext.findPlayerGameObject(master)
                .map(gameObject -> new Vector3(gameObject.getPosition()))
                .orElse(null);
        if (castOrigin == null) {
            log.trace("{}: {} is not valid : caster not found", master, inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_PLACE));
            return new InputResponseDto("Caster is not found.", false, InputResultCode.FAIL_INVALID_PLACE, playerData.mana, inputRequestDto.getId(), -1);
        }
        Vector3 castPosition = clampToRange(
                castOrigin,
                clampToMapBounds(inputRequestDto.getPosition()),
                gameContext.getParameters().getValue(magic.magicType.name(), "range")
        );

        boolean valid = playerData.useCards(inputRequestDto.getCards());

        if (!valid) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INSUFFICIENT_MANA));
            return new InputResponseDto("Insufficient mana.", false, InputResultCode.FAIL_INSUFFICIENT_MANA, playerData.mana, inputRequestDto.getId(), -1);
        }

        magic.run(gameContext, master, castOrigin, castPosition);
        gameContext.getGameSessionData().getCardDeck(master).returnCards(inputRequestDto.getCards());

        inputEventPublisher.publish(new InputHandleEvent(master, InputResultCode.SUCCESS, magic.id));
        return new InputResponseDto(true, InputResultCode.SUCCESS, playerData.mana, inputRequestDto.getId(), magic.id);
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
                                                List<CardType> cards,
                                                Vector3 position,
                                                Vector3 castOrigin) {
        Magic magic = magicParser.parseMagicForBot(cards);
        return handleBotMagicInput(gameContext, master, magic, position, castOrigin);
    }

    public InputResponseDto handleBotMagicInput(GameContext gameContext,
                                                Master master,
                                                Magic magic,
                                                Vector3 position) {
        return handleBotMagicInput(gameContext, master, magic, position, null);
    }

    public InputResponseDto handleBotMagicInput(GameContext gameContext,
                                                Master master,
                                                Magic magic,
                                                Vector3 position,
                                                Vector3 castOrigin) {
        PlayerData playerData = gameContext.getGameSessionData().getPlayerData(master);

        if (magic == null) {
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_MAGIC));
            return new InputResponseDto("invalid bot magic", false, InputResultCode.FAIL_INVALID_MAGIC, playerData.mana, -1, -1);
        }

        Vector3 rangeOrigin = castOrigin == null
                ? gameContext.findPlayerGameObject(master)
                        .map(gameObject -> new Vector3(gameObject.getPosition()))
                        .orElse(null)
                : castOrigin;
        if (rangeOrigin == null) {
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INVALID_PLACE));
            return new InputResponseDto("invalid place", false, InputResultCode.FAIL_INVALID_PLACE, playerData.mana, -1, -1);
        }
        Vector3 castPosition = clampToRange(
                rangeOrigin,
                clampToMapBounds(position),
                gameContext.getParameters().getValue(magic.magicType.name(), "range")
        );

        int manaCost = (int) gameContext.getParameters().getValue(magic.magicType.name(), "mana_cost");
        if (!playerData.spendMana(manaCost)) {
            inputEventPublisher.publish(InputHandleEvent.fail(master, InputResultCode.FAIL_INSUFFICIENT_MANA));
            return new InputResponseDto("insufficient mana", false, InputResultCode.FAIL_INSUFFICIENT_MANA, playerData.mana, -1, -1);
        }

        magic.run(gameContext, master, rangeOrigin, castPosition);
        inputEventPublisher.publish(new InputHandleEvent(master, InputResultCode.SUCCESS, magic.id));
        return new InputResponseDto(true, InputResultCode.SUCCESS, playerData.mana, -1, magic.id);
    }

    private Vector3 clampToRange(Vector3 origin, Vector3 position, double range) {
        double distance = origin.distance(position);
        if (distance <= range || distance == 0) {
            return position;
        }

        return origin.plus(position.subtract(origin).normalize().multiply((float) range));
    }

    private static final float MAP_MIN_X = 0f;
    private static final float MAP_MAX_X = 18f;
    private static final float MAP_MIN_Z = 0f;
    private static final float MAP_MAX_Z = 10f;

    private Vector3 clampToMapBounds(Vector3 position) {
        return new Vector3(
                Math.clamp(position.getX(), MAP_MIN_X, MAP_MAX_X),
                position.getY(),
                Math.clamp(position.getZ(), MAP_MIN_Z, MAP_MAX_Z)
        );
    }
}
