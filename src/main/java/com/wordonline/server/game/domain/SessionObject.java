package com.wordonline.server.game.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SessionObject {
    String sessionId;
    String leftUserId;
    String rightUserId;
}
