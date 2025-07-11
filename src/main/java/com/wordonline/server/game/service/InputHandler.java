package com.wordonline.server.game.service;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.dto.InputRequestDto;
import com.wordonline.server.game.dto.InputResponseDto;
import com.wordonline.server.game.dto.Master;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class InputHandler {
    private final GameLoop gameLoop;

    public InputResponseDto handleInput(long userId, InputRequestDto inputRequestDto) {
        Master master = gameLoop.sessionObject.getUserSide(userId);
        PlayerData playerData = gameLoop.gameSessionData.getPlayerData(master);

        // Parse the magic from the input request
        Magic magic = gameLoop.magicParser.parseMagic(List.copyOf(inputRequestDto.getCards()), master, inputRequestDto.getPosition());

        if (magic == null) {
            log.info("{}: {} is not valid : could not parse", master, inputRequestDto.getCards());
            return new InputResponseDto(false, playerData.mana, inputRequestDto.getId());
        } else if (GameConfig.PLAYER_POSITION.get(master).distance(inputRequestDto.getPosition()) > magic.magicType.getRange()) {
            log.info("{}: {} is not valid : too far", master, inputRequestDto.getCards());
            return new InputResponseDto(false, playerData.mana, inputRequestDto.getId());
        }

        // Check if the player has enough mana and card
        boolean valid = playerData.useCards(inputRequestDto.getCards());

        if (!valid) {
            log.info("{}: {} is not valid : cannot use", master, inputRequestDto.getCards());
            return new InputResponseDto(false, playerData.mana, inputRequestDto.getId());
        }

        // use the magic
        magic.run(gameLoop);

        // Add the magic to the deck data
        gameLoop.gameSessionData.getCardDeck(master).cards.addAll(inputRequestDto.getCards());

        return new InputResponseDto(true, playerData.mana, inputRequestDto.getId());
    }
}
