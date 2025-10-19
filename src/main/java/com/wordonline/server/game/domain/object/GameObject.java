package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.physic.Collider;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import com.wordonline.server.game.service.GameLoop;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

// This class is used to store the game object data
@Getter
public class GameObject {
    private final int id;
    private static int idCounter = 0;
    private final Master master;

    private final PrefabType type;
    private Status status;

    public boolean isDestroyed() {
        return status == Status.Destroyed;
    }

    private Effect effect;
    private Element element = new Element();
    private Vector3 position;

    private final GameContext gameContext;
    private final List<Collider> colliders = new ArrayList<Collider>();
    private final List<Component> components = new ArrayList<Component>();
    private List<Component> componentsToAdd = new ArrayList<Component>();
    private List<Component> componentsToRemove = new ArrayList<Component>();

    public GameObject(GameObject parent, Master master, PrefabType prefabType) {
        this(master, prefabType, parent.getPosition(), parent.gameContext);
    }

    public GameObject(GameObject parent, PrefabType prefabType) {
        this(parent, parent.getMaster(), prefabType);
    }

    public GameObject(Master master, PrefabType prefabType, Vector3 position, GameContext gameContext) {
        this.id = idCounter++;
        this.master = master;
        this.type = prefabType;
        this.position = position;
        this.gameContext = gameContext;
        this.status = Status.Idle;
        gameContext.createGameObject(this);
    }

    public <T> T getComponent(Class<T> clazz) {
        return components.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    public <T> List<T> getComponents(Class<T> clazz) {
        return components.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }

    public void addComponent(Component component)
    {
        componentsToAdd.add(component);
    }

    public void removeComponent(Component component)
    {
        componentsToRemove.add(component);
    }

    public void destroy() {
        setStatus(Status.Destroyed);
        gameContext.updateGameObject(this);
        onDestroy();
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        if (Math.abs(position.getX() - GameConfig.X_MID) > GameConfig.X_BOUND || Math.abs(position.getY() - GameConfig.Y_MID) > GameConfig.Y_BOUND) {
            destroy();
            return;
        }
        gameContext.updateGameObject(this);
    }

    public void setStatus(Status status) {
        if (this.status == Status.Destroyed) return;
        this.status = status;
        gameContext.updateGameObject(this);
    }

    public void setEffect(Effect effect) {
        this.effect = effect;
        gameContext.updateGameObject(this);
    }

    public void setElement(ElementType element) {
        this.element.addNative(element);
    }
    public void setElement(Set<ElementType> elementSet) {
        elementSet.forEach(this::setElement);
    }

    public void addTempElement(ElementType element, Object source) {
        this.element.addBonus(element, source);
    }
    public void removeTempElement(ElementType element, Object source) {
        this.element.removeBonus(element, source);
    }

    public void start() {
        PrefabProvider.get(type).initialize(this);
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
