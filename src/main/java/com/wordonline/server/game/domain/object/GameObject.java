package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.config.GameConfig;
import com.wordonline.server.game.domain.debug.GizmoCategory;
import com.wordonline.server.game.domain.debug.Gizmo;
import com.wordonline.server.game.domain.magic.ElementType;
import com.wordonline.server.game.domain.object.component.Component;
import com.wordonline.server.game.domain.object.component.physic.CircleCollider;
import com.wordonline.server.game.domain.object.component.physic.Collider;
import com.wordonline.server.game.domain.object.prefab.PrefabProvider;
import com.wordonline.server.game.domain.object.prefab.PrefabType;
import com.wordonline.server.game.dto.Effect;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.Status;
import com.wordonline.server.game.service.GameContext;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

// This class is used to store the game object data
@Getter
public class GameObject {

    private static final AtomicInteger idCounter = new AtomicInteger(0);

    private final int id;
    private Master master;

    private final PrefabType type;
    private Status status;

    public boolean isActive() {
        return !isDestroyed() && isInitialized();
    }

    public boolean isDestroyed() {
        return status == Status.Destroyed;
    }

    public boolean isInitialized() {
        return status != Status.Initializing;
    }

    private final List<Effect> effects = new ArrayList<>();
    private final Element element = new Element();
    private Vector3 position;

    private final GameContext gameContext;
    private final List<Collider> colliders = new ArrayList<Collider>();
    private final List<Component> components = new ArrayList<Component>();
    private final List<Component> componentsToAdd = new ArrayList<Component>();
    private final List<Component> componentsToRemove = new ArrayList<Component>();
    private final List<Gizmo> gizmos = new ArrayList<Gizmo>();

    public GameObject(GameObject parent, Master master, PrefabType prefabType) {
        this(master, prefabType, parent.getPosition(), parent.gameContext);
    }

    public GameObject(GameObject parent, PrefabType prefabType) {
        this(parent, parent.getMaster(), prefabType);
    }

    public GameObject(Master master, PrefabType prefabType, Vector3 position, GameContext gameContext) {
        this.status = Status.Initializing;
        this.id = idCounter.getAndIncrement();
        this.master = master;
        this.type = prefabType;
        this.position = position;
        this.gameContext = gameContext;
        gameContext.createGameObject(this);
    }

    public Optional<CircleCollider> getFirstCircleCollider(boolean isTrigger) {
        return colliders.stream()
                .filter(CircleCollider.class::isInstance)
                .filter(collider -> collider.isTrigger() == isTrigger)
                .map(CircleCollider.class::cast)
                .findFirst();
    }

    public Optional<CircleCollider> getFirstCircleCollider() {
        return colliders.stream()
                .filter(CircleCollider.class::isInstance)
                .map(CircleCollider.class::cast)
                .findFirst();
    }

    public void addCollider(Collider collider) {
        colliders.add(collider);
        if (collider instanceof CircleCollider circleCollider) {
            gizmos.add(Gizmo.circle(Vector3.ZERO, circleCollider.getRadius(), GizmoCategory.Collider));
        }
    }

    public <T> boolean hasComponent(Class<T> clazz) {
        return components.stream()
                .anyMatch(clazz::isInstance);
    }

    public <T> T getComponent(Class<T> clazz) {
        return components.stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .findFirst()
                .orElse(null);
    }

    public <T> Optional<T> getComponentOptional(Class<T> clazz) {
        return Optional.ofNullable(getComponent(clazz));
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
        applyUpdate();
        onDestroy();
    }

    public void setPosition(Vector3 position) {
        this.position = position;
        if (Math.abs(position.getX() - GameConfig.X_MID) > GameConfig.X_BOUND || Math.abs(position.getY() - GameConfig.Y_MID) > GameConfig.Y_BOUND) {
            destroy();
            return;
        }
        applyUpdate();
    }

    public void setStatus(Status status) {
        if (this.status == Status.Destroyed) return;
        this.status = status;
        applyUpdate();
    }

    public void setMaster(Master master) {
        if (this.master == master) return;
        this.master = master;
        applyUpdate();
    }

    public void addEffect(Effect effect) {
        if (effect == Effect.None || effects.contains(effect)) {
            return;
        }
        effects.add(effect);
        applyUpdate();
    }

    public void removeEffect(Effect effect) {
        if (effects.remove(effect)) {
            applyUpdate();
        }
    }

    public void removeEffects(Predicate<Effect> predicate) {
        if (effects.removeIf(predicate)) {
            applyUpdate();
        }
    }

    public void clearEffects() {
        if (effects.isEmpty()) {
            return;
        }
        effects.clear();
        applyUpdate();
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
        flushComponents();
        for (Component component : components)
            component.start();
        setStatus(Status.Idle);
    }

    public void update() {
        for (Component component : components)
            component.update();
    }

    public void onDestroy() {
        for (Component component : components)
            component.onDestroy();
    }

    public void drawCircle(Vector3 relativePosition, float radius, GizmoCategory category) {
        gizmos.add(Gizmo.circle(relativePosition, radius, category));
    }

    public void drawBox(Vector3 relativePosition, Vector3 boxSize, GizmoCategory category) {
        gizmos.add(Gizmo.box(relativePosition, boxSize, category));
    }

    public void applyUpdate() {
        gameContext.updateGameObject(this);
    }

    public void flushComponents() {
        if (!this.getComponentsToAdd().isEmpty()) {
            List<Component> toAdd = new ArrayList<>(this.getComponentsToAdd());
            this.getComponentsToAdd().clear();
            this.getComponents().addAll(toAdd);
            for (Component c : toAdd) {
                c.start();
            }
        }

        if (!this.getComponentsToRemove().isEmpty()) {
            List<Component> toRem = new ArrayList<>(this.getComponentsToRemove());
            this.getComponentsToRemove().clear();
            for (Component c : toRem) {
                c.onDestroy();
            }
            this.getComponents().removeAll(toRem);
        }
    }
}
