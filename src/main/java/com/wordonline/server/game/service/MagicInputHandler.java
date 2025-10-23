package com.wordonline.server.game.service;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.InputResponseDto;
import com.wordonline.server.game.dto.Master;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MagicInputHandler {

    private final DatabaseMagicParser magicParser;

    public InputResponseDto handleInput(GameContext gameContext, long userId, InputRequestDto inputRequestDto) {
        Master master = gameContext.getSessionObject().getUserSide(userId);
        PlayerData playerData = gameContext.getGameSessionData().getPlayerData(master);

        if (!playerData.validCardsUse(inputRequestDto.getCards())) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            return new InputResponseDto("마나가 부족합니다.", false, playerData.mana, inputRequestDto.getId(), -1);
        }

        // Parse the magic from the input request
        Magic magic = magicParser.parseMagic(inputRequestDto.getCards());

        if (magic == null) {
            // 카드 파사삭
            log.trace("{}: {} is not valid : could not parse", master, inputRequestDto.getCards());
            playerData.useCards(inputRequestDto.getCards());
            gameContext.getGameSessionData().getCardDeck(master).cards.addAll(inputRequestDto.getCards());
            return new InputResponseDto("마법 시전에 실패했습니다.", true, playerData.mana, inputRequestDto.getId(), -1);
        } else if (GameConfig.PLAYER_POSITION.get(master).distance(inputRequestDto.getPosition()) > gameContext.getParameters().getValue(magic.magicType.name(), "range")) {
            log.trace("{}: {} is not valid : too far", master, inputRequestDto.getCards());
            return new InputResponseDto("마법을 시전할 수 없는 곳입니다.", false, playerData.mana, inputRequestDto.getId(), -1);
        }

        // Check if the player has enough mana and card
        boolean valid = playerData.useCards(inputRequestDto.getCards());

        if (!valid) {
            log.trace("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            return new InputResponseDto("마나가 부족합니다.", false, playerData.mana, inputRequestDto.getId(), -1);
        }

        // use the magic
        magic.run(gameContext, master, inputRequestDto.getPosition().toVector3());

        // Add the magic to the deck data
        gameContext.getGameSessionData().getCardDeck(master).cards.addAll(inputRequestDto.getCards());

        return new InputResponseDto(true, playerData.mana, inputRequestDto.getId(), magic.id);
    }
}
