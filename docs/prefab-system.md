# Prefab System Reference

This document explains the Prefab System used to instantiate configured GameObjects in the Word Online game server.

---

## Architecture Overview

Instead of hardcoding component wiring inside magic spawning methods, the server uses templates called **Prefabs**. Creating a GameObject requires specifying its `PrefabType`. The engine then resolves the components that belong to that type using a lookup cache.

```
[new GameObject(..., PrefabType.FireShot)]
               |
               v
[PrefabProvider.get(PrefabType.FireShot)]
               |
               v (resolves Spring Bean)
[FireShotPrefabInitializer.initialize(gameObject)]
               |
               v
Adds CircleCollider, RigidBody, Shot component, elements...
```

---

## Key Classes

### 1. `PrefabType` (Enum)
Located at [PrefabType.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabType.java).
- Defines all prefab categories in the game (e.g. `FireShot`, `RockGolem`, `RainCloud`, `Player`).
- Each enum constant maps to a Spring bean identifier name (e.g., `FireShot("fire_shot_prefab")`).

### 2. `PrefabInitializer` (Abstract Class)
Located at [PrefabInitializer.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabInitializer.java).
- Defines the initialization hook: `public abstract void initialize(GameObject gameObject);`.
- Implementations are registered as Spring beans annotated with `@Component("prefab_bean_name")`.
- Inside `initialize(...)`, the developer adds colliders, behaviors, and elements using helpers like:
  - `gameObject.addCollider(...)`
  - `gameObject.addComponent(...)` or direct component instantiation.
  - `gameObject.setElement(...)`

### 3. `PrefabProvider` (Utility Class)
Located at [PrefabProvider.java](file:///Users/jeong-yunseong/development/word-online/dev/game-server/src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabProvider.java).
- Holds a static reference to the Spring `ApplicationContext`.
- Provides the static method:
  `public static PrefabInitializer get(PrefabType prefabType)`
- Dynamically resolves and returns the registered `PrefabInitializer` singleton bean associated with the requested `PrefabType`.

---

## Component Hook Safety

> [!WARNING]
> Inside the `PrefabInitializer.initialize(GameObject gameObject)` method, components are added directly to the GameObject's list using `gameObject.getComponents().add(...)` or `gameObject.addComponent(...)`.
> Because initialization occurs during the GameObject construction phase (prior to its first frame update), it is safe to add components directly. However, for modifications made **during runtime ticks**, you **must** use the `gameObject.addComponent(...)` buffer helpers to avoid concurrent modification exceptions.
