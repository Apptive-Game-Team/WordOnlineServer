package com.wordonline.server.game.domain.pvebot;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.bot.BotEye;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.frame.FrameInfoDto;
import com.wordonline.server.game.service.GameLoop;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
public final class PveEnemyBot {

    private final PveEnemyAction action;
    private final PveEnemyBrain brain;

    private final SessionObject sessionObject;
    private final GameLoop gameLoop;
    private final Master botSide;

    private final List<Magic> magics;
    private final Map<Long, Integer> cooldownUntilFrame = new ConcurrentHashMap<>();

    public PveEnemyBot(SessionObject sessionObject, DatabaseMagicParser magicParser, Master botSide) {
        this.action = new PveEnemyAction();
        this.brain = new PveEnemyBrain();
        this.sessionObject = sessionObject;
        this.gameLoop = sessionObject.getGameLoop();
        this.botSide = botSide;
        this.magics = List.copyOf(magicParser.getAllMagicRecipeMap().values());
    }

    public void onTick(FrameInfoDto myFrame) {
        BotEye botEye = new BotEye(gameLoop.getGameContext().getGameSessionData(), myFrame, botSide);
        int currentFrame = gameLoop.getGameContext().getFrameNum();

        List<Magic> availableMagics = magics.stream()
                .filter(magic -> isCooldownReady(magic, currentFrame))
                .filter(magic -> manaCostOf(magic) <= botEye.getMana())
                .toList();

        PveEnemyBrain.InputDecision decision = brain.think(
                botEye.getGameObjectList(),
                availableMagics,
                botSide,
                gameLoop);

        if (decision == null) {
            return;
        }

        boolean casted = action.useMagic(sessionObject, decision.magic(), decision.target(), botSide);
        if (casted) {
            setCooldown(decision.magic(), currentFrame);
        }
    }

    private boolean isCooldownReady(Magic magic, int currentFrame) {
        return cooldownUntilFrame.getOrDefault(magic.id, 0) <= currentFrame;
    }

    private void setCooldown(Magic magic, int currentFrame) {
        int cooldownFrames = (int) Math.ceil(resolveCooldownSec(magic) * GameLoop.FPS);
        cooldownUntilFrame.put(magic.id, currentFrame + cooldownFrames);
    }

    private int manaCostOf(Magic magic) {
        return (int) gameLoop.parameters.getValue(magic.magicType.name(), "mana_cost");
    }

    private static double resolveCooldownSec(Magic magic) {
        return switch (magic.magicType) {
            case Shoot -> 1.8;
            case Explode -> 3.2;
            case Drop -> 4.0;
            case Spawn -> 4.8;
            case Build -> 6.0;
            default -> 3.0;
        };
    }
}
