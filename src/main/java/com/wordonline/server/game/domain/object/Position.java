package com.wordonline.server.game.domain.object;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// This class is used to represent the position of an object in the game
public class Position {
    private float x, y;
}
