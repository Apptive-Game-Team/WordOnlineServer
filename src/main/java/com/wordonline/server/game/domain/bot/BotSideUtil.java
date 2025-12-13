package com.wordonline.server.game.domain.bot;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.PlayerData;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

/**
 * Utility class for bot side-related operations.
 * Reduces ternary operator usage and centralizes bot side logic.
 */
public final class BotSideUtil {

    private BotSideUtil() {
        // Utility class - prevent instantiation
    }

    /**
     * Get player position based on bot side
     */
    public static Vector3 getPlayerPosition(Master botSide) {
        return botSide == Master.LeftPlayer 
                ? GameConfig.LEFT_PLAYER_POSITION 
                : GameConfig.RIGHT_PLAYER_POSITION;
    }

    /**
     * Get enemy side based on bot side
     */
    public static Master getEnemySide(Master botSide) {
        return botSide == Master.LeftPlayer 
                ? Master.RightPlayer 
                : Master.LeftPlayer;
    }

    /**
     * Get player data from game session data based on bot side
     */
    public static PlayerData getPlayerData(GameSessionData data, Master botSide) {
        return botSide == Master.LeftPlayer 
                ? data.leftPlayerData 
                : data.rightPlayerData;
    }

    /**
     * Get user ID from session object based on bot side
     */
    public static long getUserId(SessionObject sessionObject, Master botSide) {
        return botSide == Master.LeftPlayer 
                ? sessionObject.getLeftUserId() 
                : sessionObject.getRightUserId();
    }
}
