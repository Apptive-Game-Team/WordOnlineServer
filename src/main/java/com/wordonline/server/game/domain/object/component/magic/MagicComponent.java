package com.wordonline.server.game.domain.object.component.magic;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Master;

public abstract class MagicComponent extends Component {
    @Override
    public void start() {

    }

    @Override
    public void update() {

    }

    @Override
    public void onDestroy() {

    }

    public abstract void use(Master master);

    public MagicComponent(GameObject gameObject) {
        super(gameObject);
    }
}
