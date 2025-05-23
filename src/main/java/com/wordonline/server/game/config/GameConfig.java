package com.wordonline.server.game.config;

import com.wordonline.server.game.domain.object.Vector2;
import com.wordonline.server.game.dto.Master;

import java.util.Dictionary;
import java.util.Hashtable;

public class GameConfig {
    public static final int X_BOUND = 15;
    public static final int Y_BOUND = 10;

    public static final int X_MID = 9;
    public static final int Y_MID = 5;

    public static final int WIDTH = 18;
    public static final int HEIGHT = 10;

    public static final Vector2 LEFT_PLAYER_POSITION = new Vector2(1, 5);
    public static final Vector2 RIGHT_PLAYER_POSITION = new Vector2(18, 5);
    public static final Dictionary<Master, Vector2> PLAYER_POSITION = new Hashtable<>() {{
        put(Master.LeftPlayer, LEFT_PLAYER_POSITION);
        put(Master.RightPlayer, RIGHT_PLAYER_POSITION);
    }};
}
