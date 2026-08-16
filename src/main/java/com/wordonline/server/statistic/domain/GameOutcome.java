package com.wordonline.server.statistic.domain;

// How a recorded match ended. Stored in statistic_games.outcome; only WIN rows carry
// win/loss user ids. ABANDONED rows are written by the loop watchdog with whatever the
// builder had accumulated up to the stall, so the frame statistics survive for diagnosis.
public enum GameOutcome {
    WIN, DRAW, ABANDONED
}
