package com.wordonline.server.game.dto;

import java.util.List;

import com.wordonline.server.game.domain.magic.CardType;

/**
 * {@code magics} 한 행과, 그 마법이 가리키는 game object 에서 읽은 prefab 이름.
 *
 * <p>{@code castKind}, {@code gameObjectName}, {@code prefabName} 은 셋 다 null 일 수 있다.
 * 데이터가 아직 들어가지 않은 마법은 지금처럼 bean 으로 찾는다.
 */
public record MagicInfoDto(
        Long id,
        String name,
        List<CardType> cards,
        String castKind,
        String gameObjectName,
        String prefabName
) {

}
