package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.Position;
import com.wordonline.server.game.domain.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

// This class is used to store the game object data
// TODO - method for updating object and ObjectsInfoDto ex) updatePosition
@Getter
public class GameObject implements Component {
    private final int id;
    private static int idCounter = 0;
    private final Master master;

    private PrefabType type;
    private Status status;

    private Effect effect;
    @Getter
    private Position position;

    private final GameLoop gameLoop;
    private final List<Component> components = new ArrayList<Component>();

    public GameObject(Master master, PrefabType type, Position position, GameLoop gameLoop) {
        this.id = idCounter++;
        this.master = master;
        this.type = type;
        this.position = position;
        this.gameLoop = gameLoop;
        this.status = Status.Idle;
        gameLoop.getObjectsInfoDtoBuilder().createGameObject(this);
    }

    public void setPosition(Position position) {
        this.position = position;
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
    }

    public void setStatus(Status status) {
        this.status = status;
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
    }

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
