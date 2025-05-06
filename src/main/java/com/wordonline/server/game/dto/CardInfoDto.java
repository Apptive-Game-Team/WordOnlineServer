package com.wordonline.server.game.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
// This class is used to send card information to the client
public class CardInfoDto {
    private final List<String> added;

    public CardInfoDto() {added = new ArrayList<>();}
    public CardInfoDto(List<String> added) {this.added = added;}

    public void addCard(String card) {
        added.add(card);
    }
}

