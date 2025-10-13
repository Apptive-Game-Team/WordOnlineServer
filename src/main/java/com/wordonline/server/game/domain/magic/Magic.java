package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.service.GameLoop;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Magic {

    public final long id;
    public final CardType magicType;

    public abstract void run(GameLoop gameLoop);
}

