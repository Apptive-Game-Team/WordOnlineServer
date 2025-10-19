package com.wordonline.server.game.domain.object.prefab;

import com.wordonline.server.game.domain.object.GameObject;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class PrefabInitializer {

    public final PrefabType prefabType;

    public abstract void initialize(GameObject gameObject);
}