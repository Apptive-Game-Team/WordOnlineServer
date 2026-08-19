package com.wordonline.server.game.domain.bot;

import com.wordonline.server.bot.domain.BotPersona;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.parser.MagicParser;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;
import com.wordonline.server.game.service.bot.BotCounterEvaluator;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@Slf4j
public final class BotAgent {

    private final BotAction botAction;
    private final BotBrain botBrain;

    private final SessionObject sessionObject;
    private final GameLoop gameLoop;
    private final Master botSide;
    private final BotPersona persona;

    // Written by the bot executor thread in onTick and read by the loop thread in shouldProcess.
    // Only one onTick runs at a time (BotAgentSystem gates it with a CAS), so the two threads never
    // write it concurrently and a plain volatile reference is enough. A shouldProcess that reads a
    // stale value at worst submits a tick that returns immediately, or skips one reaction interval.
    private volatile PendingDecision pendingDecision;

    private static final AtomicInteger NEXT_ID = new AtomicInteger(0);

    public BotAgent(SessionObject sessionObject,
                    MagicParser magicParser,
                    Master botSide,
                    BotPersona persona,
                    BotCounterEvaluator counterEvaluator) {
        this.botAction = new BotAction();
        this.botBrain = new BotBrain(magicParser, counterEvaluator, persona);
        this.sessionObject = sessionObject;
        this.gameLoop = sessionObject.getGameLoop();
        this.botSide = botSide;
        this.persona = persona;
        log.debug("BotAgent initialized for side: {}, persona: {}", botSide, persona.name());
    }

    public boolean shouldProcess(int currentFrame) {
        return hasReadyPendingDecision() || (pendingDecision == null && shouldThink(currentFrame));
    }

    private boolean shouldThink(int currentFrame) {
        return currentFrame % persona.normalizedReactionIntervalFrames() == 0;
    }

    // Reads the field once: the bot thread can null it between a check and a dereference.
    private boolean hasReadyPendingDecision() {
        PendingDecision pending = pendingDecision;
        return pending != null && System.currentTimeMillis() >= pending.readyAtMillis();
    }

    // Runs on the bot executor thread against an immutable snapshot taken by the loop thread.
    public void onTick(BotEye botEye) {
        log.trace("[BotAgent {}] Tick start", botSide);

        if (dispatchPendingDecisionIfReady()) {
            return;
        }
        if (pendingDecision != null) {
            return;
        }

        log.debug("[BotAgent {}] State: Mana={}, Cards={}, VisibleObjects={}",
                botSide, botEye.mana(), botEye.cardList(), botEye.gameObjectList().size());

        BotBrain.InputDecision decision = botBrain.think(botEye, gameLoop.parameters, botSide);
        
        if(decision != null)
        {
            long readyAtMillis = System.currentTimeMillis() + persona.normalizedThinkingTimeMs();
            pendingDecision = new PendingDecision(decision, readyAtMillis);
            log.debug("[BotAgent {}] Decision scheduled: {} at {}, readyAt={}", botSide, decision.playCards(), decision.target(), readyAtMillis);
            dispatchPendingDecisionIfReady();
        } else {
            log.trace("[BotAgent {}] No action decided", botSide);
        }
    }

    private boolean dispatchPendingDecisionIfReady() {
        if (!hasReadyPendingDecision()) {
            return false;
        }

        BotBrain.InputDecision decision = pendingDecision.decision();
        pendingDecision = null;

        log.debug("[BotAgent {}] Dispatching decision: {} at {}", botSide, decision.playCards(), decision.target());
        InputRequestDto inputRequestDto = new InputRequestDto();
        inputRequestDto.setType("useMagic");
        inputRequestDto.setId(NEXT_ID.getAndIncrement());
        inputRequestDto.setCards(decision.playCards());
        inputRequestDto.setPosition(decision.target());
        botAction.useCard(sessionObject, inputRequestDto, botSide);
        return true;
    }

    private record PendingDecision(BotBrain.InputDecision decision, long readyAtMillis) {
    }
}
