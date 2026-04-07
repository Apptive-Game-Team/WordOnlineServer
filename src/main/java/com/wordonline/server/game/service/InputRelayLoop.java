package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.SessionType;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.dto.lockstep.ConfirmedFrameDto;
import com.wordonline.server.game.dto.lockstep.InitialObjectDto;
import com.wordonline.server.game.dto.lockstep.PveEventDto;
import com.wordonline.server.game.dto.lockstep.SessionStartDto;
import com.wordonline.server.game.service.system.BotAgentSystem;
import com.wordonline.server.game.service.system.InputBufferSystem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Lockstep Phase 3: the server no longer simulates.
 * It collects inputs from all players each frame, then broadcasts the
 * confirmed input set so both clients can execute identical simulation steps.
 *
 * Responsibilities:
 * - Collect per-frame inputs (human + bot)
 * - Broadcast ConfirmedFrameDto to all players/spectators
 * - Detect disconnect/timeout → set result
 * - Persist game result via handleGameEnd()
 *
 * Bot agents still run on the server; their inputs are injected into the
 * InputBufferSystem just like human inputs.
 */
@Slf4j
@Getter
@Scope("prototype")
@Service
public class InputRelayLoop extends GameLoop {

    private final DatabaseMagicParser magicParser;
    private final BotAgentSystem botSystem;

    private BotAgent leftBotAgent;
    private BotAgent rightBotAgent;

    @Override public BotAgent getLeftBotAgent() { return leftBotAgent; }
    @Override public BotAgent getRightBotAgent() { return rightBotAgent; }

    /** Seed shared with clients for deterministic RNG */
    protected long rngSeed;

    public InputRelayLoop(MmrService mmrService,
                          UserService userService,
                          GameContext gameContext,
                          Parameters parameters,
                          DatabaseMagicParser magicParser,
                          BotAgentSystem botSystem) {
        super(mmrService, userService, gameContext, parameters);
        this.magicParser = magicParser;
        this.botSystem = botSystem;
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.init(sessionObject, this);
        super.initializeLoop(sessionObject, onTerminated);

        this.rngSeed = ThreadLocalRandom.current().nextLong();

        if (sessionObject.getSessionType() == SessionType.Practice) {
            initializeBotAgents(sessionObject);
        }

        // Send sessionStart to both players (no PVE extras for PVP/Practice)
        sendSessionStart(sessionObject, List.of(
                new InitialObjectDto("", "Player", "LeftPlayer", 1, 5, 0),
                new InitialObjectDto("", "Player", "RightPlayer", 17, 5, 0)
        ), null);
    }

    private void initializeBotAgents(SessionObject sessionObject) {
        if (sessionObject.isLeftBot()) {
            leftBotAgent = new BotAgent(sessionObject, magicParser, Master.LeftPlayer);
        }
        if (sessionObject.isRightBot()) {
            rightBotAgent = new BotAgent(sessionObject, magicParser, Master.RightPlayer);
        }
    }

    protected void sendSessionStart(SessionObject sessionObject,
                                    List<InitialObjectDto> initialObjects,
                                    List<PveEventDto> scenarioEvents) {
        List<CardType> leftCards = sessionObject.getLeftUserCardDeck().peekAll();
        List<CardType> rightCards = sessionObject.getRightUserCardDeck().peekAll();

        // Gather balance parameters so both clients use the same values
        Map<String, Map<String, Double>> params = parameters.getAllValues();

        SessionStartDto dto = new SessionStartDto(
                rngSeed,
                sessionObject.getLeftUserId(),
                sessionObject.getRightUserId(),
                leftCards,
                rightCards,
                params,
                sessionObject.getSessionType().name(),
                initialObjects,
                scenarioEvents
        );

        sessionObject.sendFrameInfo(sessionObject.getLeftUserId(), dto);
        sessionObject.sendFrameInfo(sessionObject.getRightUserId(), dto);
        sessionObject.broadcastFrameInfo(dto);
    }

    /**
     * Each frame: consume buffered inputs, broadcast them, check for game end.
     * No simulation runs on the server — clients do that.
     */
    @Override
    void update() {
        int frame = gameContext.getFrameNum();
        InputBufferSystem buf = gameContext.getInputBufferSystem();

        // Let bots generate inputs for this frame
        botSystem.update(gameContext);

        // Consume all inputs for this frame
        Map<Long, InputRequestDto> inputs = buf.consume(frame);
        buf.discardOlderThan(frame);

        // Broadcast the confirmed input set to all players and spectators
        ConfirmedFrameDto confirmed = new ConfirmedFrameDto(frame, inputs);
        sessionObject.sendFrameInfo(sessionObject.getLeftUserId(), confirmed);
        sessionObject.sendFrameInfo(sessionObject.getRightUserId(), confirmed);
        sessionObject.broadcastFrameInfo(confirmed);

        // Game end is signalled by clients or by ping timeout
        if (gameContext.getGameTimer().isEnd()) {
            gameContext.getResultChecker().setEnd();
        }
        if (gameContext.getResultChecker().checkResult()) {
            handleGameEnd();
        }
    }
}
