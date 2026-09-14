package com.wordonline.server.game.domain.magic;

import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public abstract class Magic {

    public long id;

    /**
     * {@code magics.name}. 등록할 때 {@code id} 와 함께 채운다. 계열이 데이터로 옮겨간 뒤로는
     * 여러 마법이 같은 클래스를 쓰기 때문에, 어느 마법인지는 클래스 이름이 아니라 이 값으로 안다.
     */
    public String name;

    public final CardType magicType;

    public abstract void run(GameContext gameContext, Master master, Vector3 position);

    public void run(GameContext gameContext, Master master, Vector3 castOrigin, Vector3 position) {
        run(gameContext, master, position);
    }

}
