package com.wordonline.server.game.domain.object;

import com.wordonline.server.game.dto.CreatedObjectDto;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.service.GameLoop;

public interface PrefabInitializer {
    // This method is used to create a prefab object
    void initialize(GameObject gameObject);

}
