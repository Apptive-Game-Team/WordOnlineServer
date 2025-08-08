package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.domain.Parameters;

public interface PrefabInitializer {
    // This method is used to create a prefab object
    void initialize(GameObject gameObject, Parameters parameters);

}
