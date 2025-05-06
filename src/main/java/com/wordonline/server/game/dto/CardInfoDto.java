package com.wordonline.server.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class CardInfoDto {
    private final List<String> added;
}

