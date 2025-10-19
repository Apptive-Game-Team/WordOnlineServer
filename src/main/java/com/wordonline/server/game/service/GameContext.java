package com.wordonline.server.game.service;

import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.wordonline.server.game.domain.GameSessionData;
import com.wordonline.server.game.domain.Parameters;
import com.wordonline.server.game.domain.SessionObject;
import com.wordonline.server.game.domain.bot.BotAgent;
import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.Master;
import com.wordonline.server.game.dto.frame.ObjectsInfoDto;
import com.wordonline.server.game.util.Physics;
import com.wordonline.server.game.util.SimplePhysics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Service
@Scope("prototype")
@RequiredArgsConstructor
public class GameContext {

    private SessionObject sessionObject;
    private GameSessionData gameSessionData;
    private ResultChecker resultChecker;
    private int frameNum = 0;
    private final Parameters parameters;
    private Physics physics;
    private final MagicInputHandler magicInputHandler;
    private ObjectsInfoDtoBuilder objectsInfoDtoBuilder;
    private float deltaTime = 1f / GameLoop.FPS;

    public void init(SessionObject sessionObject) {
        this.sessionObject = sessionObject;
        this.gameSessionData = new GameSessionData(sessionObject.getLeftUserCardDeck(), sessionObject.getRightUserCardDeck(), parameters);;
        this.resultChecker = new ResultChecker(sessionObject);
        this.objectsInfoDtoBuilder = new ObjectsInfoDtoBuilder(this);
        physics = new SimplePhysics(gameSessionData.gameObjects);
    }

    public void setLoser(Master master) {
        resultChecker.setLoser(master);
    }

    public List<GameObject> getGameObjects() {
        return gameSessionData.gameObjects;
    }

    public void updateGameObject(GameObject gameObject) {
        objectsInfoDtoBuilder.updateGameObject(gameObject);
    }

    public void createGameObject(GameObject gameObject) {
        objectsInfoDtoBuilder.createGameObject(gameObject);
    }

    public void addGameObject(GameObject gameObject) {
        gameSessionData.addGameObject(gameObject);
    }

    public List<GameObject> overlapCircleAll(GameObject object, float distance) {
        return physics.overlapCircleAll(object, distance);
    }

    public ObjectsInfoDto getObjectsInfoDto() {
        return objectsInfoDtoBuilder.getObjectsInfoDto();
    }

    public void incrementFrameNum() {
        this.frameNum++;
    }
}
