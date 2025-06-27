package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

// This class is used to store the game object data
@Getter
public class GameObject {
    private final int id;
    private static int idCounter = 0;
    private final Master master;

    private PrefabType type;
    private Status status;

    private Effect effect;
    private Vector2 position;
    @Setter
    private float radius;

    private final GameLoop gameLoop;
    private final List<Component> components = new ArrayList<Component>();

    public GameObject(GameObject parent, PrefabType type) {
        this.id = idCounter++;
        this.master = parent.getMaster();
        this.type = type;
        this.position = parent.getPosition();
        this.gameLoop = parent.getGameLoop();
        this.status = Status.Idle;
        gameLoop.getObjectsInfoDtoBuilder().createGameObject(this);
    }

    public GameObject(Master master, PrefabType type, Vector2 position, GameLoop gameLoop) {
        this.id = idCounter++;
        this.master = master;
        this.type = type;
        this.position = position;
        this.gameLoop = gameLoop;
        this.status = Status.Idle;
        gameLoop.getObjectsInfoDtoBuilder().createGameObject(this);
    }

    public <T> T getComponent(Class<T> clazz) {
        for (Component component : components) {
            if (clazz.isInstance(component)) {
                return (T) component;
            }
        }
        return null;
    }

    public <T> List<T> getComponents(Class<T> clazz) {
        List<T> result = new ArrayList<>();
        for (Component component : components) {
            if (clazz.isInstance(component)) {
                result.add((T) component);
            }
        }
        return result;
    }

    public void destroy() {
        setStatus(Status.Destroyed);
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
        onDestroy();
    }

    public void setPosition(Vector2 position) {
        this.position = position;
        if (Math.abs(position.getX() - GameConfig.X_MID) > GameConfig.X_BOUND || Math.abs(position.getY() - GameConfig.Y_MID) > GameConfig.Y_BOUND) {
            destroy();
            return;
        }
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
    }

    public void setStatus(Status status) {
        if (this.status == Status.Destroyed) return;
        this.status = status;
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
        gameLoop.getObjectsInfoDtoBuilder().updateGameObject(this);
    }

    public void start() {
        type.initialize(this);
        for (Component component : components)
            component.start();
    }

    public void update() {
        for (Component component : components)
            component.update();
    }

    public void onDestroy() {
        for (Component component : components)
            component.onDestroy();
    }
}
