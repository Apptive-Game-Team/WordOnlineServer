package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.dto.*;
import com.wordonline.server.game.dto.frame.CreatedObjectDto;
import com.wordonline.server.game.dto.frame.ObjectsInfoDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Slf4j
public class ObjectsInfoDtoBuilder {

    private List<CreatedObjectDto> createdObjectDtos = new ArrayList<>();
    private List<UpdatedObjectDto> updatedObjectDtos = new ArrayList<>();
    private final GameLoop gameLoop;

    public ObjectsInfoDtoBuilder(GameLoop gameLoop) {
        this.gameLoop = gameLoop;
    }

    public ObjectsInfoDto getObjectsInfoDto () {
        ObjectsInfoDto result = new ObjectsInfoDto(createdObjectDtos, updatedObjectDtos);
        if (!createdObjectDtos.isEmpty() || !updatedObjectDtos.isEmpty())
            log.trace("ObjectsInfoDto: {}", result);
        createdObjectDtos = new ArrayList<>();
        updatedObjectDtos = new ArrayList<>();
        return result;
    }

    public void createGameObject(GameObject gameObject) {
        gameLoop.gameSessionData.gameObjectsToAdd.add(gameObject);
        CreatedObjectDto createdObjectDto = new CreatedObjectDto(gameObject.getId(), gameObject.getType(), gameObject.getPosition(), gameObject.getMaster());
        createdObjectDtos.add(createdObjectDto);
        log.trace("CreatedObjectDto: {}", createdObjectDto);
        gameObject.start();
    }

    public void updateGameObject(GameObject gameObject) {
        UpdatedObjectDto updatedObjectDto = updatedObjectDtos.stream()
                .filter(dto -> dto.getId() == gameObject.getId())
                .findFirst()
                .orElse(null);
        if (updatedObjectDto != null) { // if the object is already in the update list, update it
            updatedObjectDto.setPosition(gameObject.getPosition());
            updatedObjectDto.setStatus(gameObject.getStatus());
            updatedObjectDto.setEffect(gameObject.getEffect());
        } else { // if the object is not in the update list, add it
            updatedObjectDto = new UpdatedObjectDto(gameObject);
            updatedObjectDto.setPosition(gameObject.getPosition());
            updatedObjectDtos.add(updatedObjectDto);
        }
        log.trace("UpdatedObjectDto: {}", updatedObjectDto);
    }
}

