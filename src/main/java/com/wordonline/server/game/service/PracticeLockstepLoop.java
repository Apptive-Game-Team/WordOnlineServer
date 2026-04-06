package com.wordonline.server.game.service;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.input.InputRequestDto;
import com.wordonline.server.game.service.system.BotAgentSystem;
import com.wordonline.server.game.service.system.InputBufferSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lockstep loop for Practice sessions.
 *
 * The server generates simple RNG-based bot inputs every BOT_TICK_INTERVAL frames.
 * The bot picks a random magic recipe and a random position in its half.
 * The client validates card availability and mana — invalid inputs are silently dropped.
 * This removes any dependency on server-side game state for bot decisions.
 */
@Slf4j
@Scope("prototype")
@Service
public class PracticeLockstepLoop extends InputRelayLoop {

    private static final int BOT_TICK_INTERVAL = 8; // generate a bot input every 8 frames (~0.4 s)
    private static final AtomicInteger INPUT_ID_COUNTER = new AtomicInteger(0);

    private final DatabaseMagicParser magicParser;
    private List<List<CardType>> allRecipes;
    private long botUserId;

    public PracticeLockstepLoop(MmrService mmrService,
                                UserService userService,
                                GameContext gameContext,
                                Parameters parameters,
                                DatabaseMagicParser magicParser,
                                BotAgentSystem botSystem) {
        super(mmrService, userService, gameContext, parameters, magicParser, botSystem);
        this.magicParser = magicParser;
    }

    @Override
    public void init(SessionObject sessionObject, Runnable onTerminated) {
        gameContext.init(sessionObject, this);
        super.initializeLoop(sessionObject, onTerminated);

        this.rngSeed = ThreadLocalRandom.current().nextLong();

        // Cache all known recipes for random selection
        Collection<List<CardType>> recipes = magicParser.getAllMagicRecipes();
        allRecipes = new ArrayList<>(recipes);

        // Bot occupies whichever side has a negative user id
        botUserId = sessionObject.isRightBot()
                ? sessionObject.getRightUserId()
                : sessionObject.getLeftUserId();

        sendSessionStart(sessionObject, null, null);
        log.info("[Practice] Session started; botUserId={}", botUserId);
    }

    @Override
    void update() {
        int frame = gameContext.getFrameNum();

        // Inject a random bot input every BOT_TICK_INTERVAL frames
        if (frame % BOT_TICK_INTERVAL == 0 && !allRecipes.isEmpty()) {
            generateBotInput(frame);
        }

        // Delegate the rest (collect inputs + broadcast confirmedFrame) to parent
        super.update();
    }

    private void generateBotInput(int frame) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        List<CardType> recipe = allRecipes.get(rng.nextInt(allRecipes.size()));
        Vector3 pos = randomBotPosition(rng);

        InputRequestDto input = new InputRequestDto();
        input.setType("useMagic");
        input.setId(INPUT_ID_COUNTER.getAndIncrement());
        input.setCards(recipe);
        input.setPosition(pos);
        input.setFrameNum(frame);

        InputBufferSystem buf = gameContext.getInputBufferSystem();
        buf.receive(frame, botUserId, input);
        log.trace("[Practice] Bot input generated: frame={} recipe={} pos={}", frame, recipe, pos);
    }

    /**
     * Random position in the right-player half (x > 0).
     * Kept within world bounds; client physics handle boundary clamping.
     */
    private static Vector3 randomBotPosition(ThreadLocalRandom rng) {
        Master botSide = Master.RightPlayer;
        Vector3 center = GameConfig.RIGHT_PLAYER_POSITION;
        double range = 8.0;
        double u = rng.nextDouble();
        double r = Math.sqrt(u) * range;
        double theta = rng.nextDouble(Math.PI / 2.0, Math.PI * 3.0 / 2.0);
        float x = (float) (center.getX() + r * Math.cos(theta));
        float y = (float) (center.getY() + r * Math.sin(theta));
        return new Vector3(x, y, 0f);
    }
}
