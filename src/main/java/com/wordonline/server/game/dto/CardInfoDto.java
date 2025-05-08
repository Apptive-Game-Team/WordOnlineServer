package com.wordonline.server.game.dto;

import com.wordonline.server.game.domain.magic.CardType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
// This class is used to send card information to the client
public class CardInfoDto {
    private final List<CardType> added;

    public CardInfoDto() {added = new ArrayList<>();}
    public CardInfoDto(List<CardType> added) {this.added = added;}

    public void addCard(CardType card) {
        added.add(card);
    }
}

