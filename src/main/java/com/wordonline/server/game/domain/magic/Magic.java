package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Magic {

    // id, name and element are the magics row this bean was matched to. DatabaseMagicParser fills
    // them in at startup; name equals the Spring bean name, which equals magics.name.
    public long id;
    public String name;
    public ElementType element = ElementType.NONE;

    public final CardType magicType;

    public abstract void run(GameContext gameContext, Master master, Vector3 position);

    public void run(GameContext gameContext, Master master, Vector3 castOrigin, Vector3 position) {
        run(gameContext, master, position);
    }

}
