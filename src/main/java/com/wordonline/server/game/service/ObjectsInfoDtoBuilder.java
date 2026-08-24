package com.wordonline.server.game.service;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.*;
import com.wordonline.server.game.dto.frame.CreatedObjectDto;
import com.wordonline.server.game.dto.frame.ObjectsInfoDto;
import com.wordonline.server.game.dto.frame.projectile.PositionProjectileTarget;
import com.wordonline.server.game.dto.frame.projectile.ProjectileDto;
import com.wordonline.server.game.dto.frame.projectile.ProjectileTarget;
import com.wordonline.server.game.dto.frame.projectile.ReferenceProjectileTarget;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ObjectsInfoDtoBuilder {

    private List<CreatedObjectDto> createdObjectDtos = new ArrayList<>();
    private List<UpdatedObjectDto> updatedObjectDtos = new ArrayList<>();
    // index over updatedObjectDtos: ids cannot repeat there, so a lookup here and the
    // first list match are the same entry. Reset together with the list every frame.
    private Map<Integer, UpdatedObjectDto> updatedObjectDtosById = new HashMap<>();
    private List<ProjectileDto> projectileDtos = new ArrayList<>();
    private final GameContext gameContext;

    public ObjectsInfoDtoBuilder(GameContext gameContext) {
        this.gameContext = gameContext;
    }

    public ObjectsInfoDto getObjectsInfoDto() {
        ObjectsInfoDto result = new ObjectsInfoDto(createdObjectDtos, updatedObjectDtos, projectileDtos);
        if (!createdObjectDtos.isEmpty() || !updatedObjectDtos.isEmpty())
            log.trace("ObjectsInfoDto: {}", result);
        createdObjectDtos = new ArrayList<>();
        updatedObjectDtos = new ArrayList<>();
        updatedObjectDtosById = new HashMap<>();
        projectileDtos = new ArrayList<>();
        return result;
    }

    public void createProjection(GameObject start, GameObject end, String type, float duration) {
        createProjection(
                new ReferenceProjectileTarget(start.getId()),
                new ReferenceProjectileTarget(end.getId()),
                type,
                duration
        );
    }

    public void createProjection(Vector3 start, Vector3 end, String type, float duration) {
        createProjection(
                new PositionProjectileTarget(start),
                new PositionProjectileTarget(end),
                type,
                duration
        );
    }

    public void createProjection(ProjectileTarget start, ProjectileTarget destination, String type, float duration) {
        projectileDtos.add(new ProjectileDto(type, start, destination, duration));
    }

    public void createGameObject(GameObject gameObject) {
        gameContext.addGameObject(gameObject);
        gameObject.start();
        CreatedObjectDto createdObjectDto = new CreatedObjectDto(
                gameObject.getId(),
                gameObject.getType(),
                gameObject.getPosition(),
                gameObject.getMaster(),
                copyOrEmpty(gameObject.getGizmos())
        );
        createdObjectDtos.add(createdObjectDto);
        log.trace("CreatedObjectDto: {}", createdObjectDto);
    }

    public void updateGameObject(GameObject gameObject) {
        UpdatedObjectDto updatedObjectDto = updatedObjectDtosById.get(gameObject.getId());
        if (updatedObjectDto != null) { // if the object is already in the update list, update it
            updatedObjectDto.setPosition(gameObject.getPosition());
            updatedObjectDto.setStatus(gameObject.getStatus());
            updatedObjectDto.setEffects(copyOrEmpty(gameObject.getEffects()));
            updatedObjectDto.setMaster(gameObject.getMaster());
            updatedObjectDto.updateGauges(gameObject);
        } else { // if the object is not in the update list, add it
            updatedObjectDto = new UpdatedObjectDto(gameObject);
            updatedObjectDto.setPosition(gameObject.getPosition());
            updatedObjectDtos.add(updatedObjectDto);
            updatedObjectDtosById.put(gameObject.getId(), updatedObjectDto);
        }
        log.trace("UpdatedObjectDto: {}", updatedObjectDto);
    }

    // ArrayList.toArray() allocates even for an empty source, and these lists are
    // usually empty, so hand back the shared empty list instead.
    private static <T> List<T> copyOrEmpty(List<T> source) {
        return source.isEmpty() ? List.of() : List.copyOf(source);
    }
}
