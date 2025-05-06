package com.wordonline.server.game.domain;

import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class GameObject implements Component {
    private final int id;
    private final Master master;

    private Type type;
    private Status status;
    private Effect effect;
    private Position position;

    private final List<Component> components = new ArrayList<Component>();

    @Override
    public void start() {
        for (Component component : components)
            component.start();
    }

    @Override
    public void update() {
        for (Component component : components)
            component.update();
    }

    @Override
    public void onDestroy() {
        for (Component component : components)
            component.onDestroy();
    }
}
