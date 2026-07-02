package com.wordonline.server.game.dto.input;

import com.wordonline.server.game.domain.magic.CardType;
import com.wordonline.server.game.domain.object.Vector3;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MagicUseRequestDto {
    private String type;
    private List<CardType> cards;
    private int id;
    private Vector3 position;
}
