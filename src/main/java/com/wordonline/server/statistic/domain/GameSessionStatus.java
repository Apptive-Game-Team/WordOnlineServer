package com.wordonline.server.statistic.domain;

// Lifecycle state of a statistic_game_sessions row. IN_PROGRESS is written at session
// start; the other states resolve it at session end. A row that stays IN_PROGRESS long
// after started_at means the hosting process died without cleaning up.
public enum GameSessionStatus {
    IN_PROGRESS, COMPLETED, DRAW, ABANDONED
}
