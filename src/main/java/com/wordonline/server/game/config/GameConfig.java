package com.wordonline.server.game.config;

import com.wordonline.server.game.domain.object.CircleObstacle;
import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class GameConfig {
    public static final int X_BOUND = 15;
    public static final int Y_BOUND = 10;

    public static final int X_MID = 9;
    public static final int Y_MID = 5;

    public static final int WIDTH = 18;
    public static final int HEIGHT = 10;
    public static final float GRAVITY_ACCEL = 4f;
    public static final float AERIAL_STANDARD_HEIGHT = 2f;
    public static final float AERIAL_MOB_INIT_HEIGHT = 3f;
    public static final float DROP_MAGIC_INITIAL_HEIGHT = 10f;
    public static final float FALL_THRESHOLD_VELOCITY = 4f;
    public static final float FALL_THRESHOLD = 0.01f;

    public static final Vector3 LEFT_PLAYER_POSITION = new Vector3(1, 5, 0);
    public static final Vector3 RIGHT_PLAYER_POSITION = new Vector3(17, 5, 0);
    public static final Dictionary<Master, Vector3> PLAYER_POSITION = new Hashtable<>() {{
        put(Master.LeftPlayer, LEFT_PLAYER_POSITION);
        put(Master.RightPlayer, RIGHT_PLAYER_POSITION);
    }};

    public static final List<CircleObstacle> OBSTACLES = Collections.singletonList(
            new CircleObstacle(new Vector2(WIDTH/2,HEIGHT/2), 1.5f)
    );

    public static final float DEFAULT_FLOATING_VELOCITY = 1;
}