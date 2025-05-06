package com.wordonline.server.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
public class CardInfoDto {
    private final List<String> added;

    public CardInfoDto() {added = new ArrayList<>();}
    public CardInfoDto(List<String> added) {this.added = added;}

    public void addCard(String card) {
        added.add(card);
    }
}

