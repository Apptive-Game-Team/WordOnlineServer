package com.wordonline.server.game.domain.pvebot;

import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.magic.Magic;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.Master;

public class PveEnemyAction {

    public boolean useMagic(SessionObject sessionObject, Magic magic, Vector3 target, Master botSide) {
        return sessionObject.getGameContext()
                .getMagicInputHandler()
                .handleBotMagicInput(sessionObject.getGameContext(), botSide, magic, target)
                .valid();
    }
}
