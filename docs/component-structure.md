# Word Online Component Structure

This document describes the component-based architecture used in the Word Online game server.

## Overview

The game logic is structured using a Component pattern, similar to Unity's ECS or component-based systems. Each `GameObject` can have multiple `Component` instances that define its behavior.

## Core Classes

### 1. GameObject
The base class for all entities in the game.
- Located at: `src/main/java/com/wordonline/server/game/domain/object/GameObject.java`
- Manages a list of `Component`s.
- Has a `Master` (LeftPlayer, RightPlayer, or None) to identify ownership.
- Has a `Position` (Vector3) and `Status`.

### 2. Component
The abstract base class for all components.
- Located at: `src/main/java/com/wordonline/server/game/domain/object/component/Component.java`
- Lifecycle methods:
  - `start()`: Called when the component is initialized.
  - `update()`: Called every frame (via `ComponentUpdateSystem`).
  - `onDestroy()`: Called when the component or its GameObject is destroyed.

### 3. PrefabInitializer
Used to initialize a `GameObject` with a specific set of components and properties based on its `PrefabType`.
- Located at: `src/main/java/com/wordonline/server/game/domain/object/prefab/PrefabInitializer.java`

## Common Components

- **RigidBody**: Handles physics properties like mass and velocity.
- **Collider** (CircleCollider, etc.): Handles collision detection.
- **Mob**: Base component for entities that can take damage.
- **SelfAttacker**: A component that deals damage to its own owner (often used for totems or timed entities).

## Physics System

The `Physics` interface and `SimplePhysics` implementation provide spatial queries like:
- `overlapSphereAll`: Finds all GameObjects within a certain radius.
- `raycast`: Finds the first GameObject in a specific direction.
- `overlapBoxAll` (To be implemented): Finds all GameObjects within a rectangular area.
