package com.wordonline.server.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameConfigDto {
    private String version;
    private List<MagicRecipeDto> magicRecipes;
    private Map<String, Map<String, Double>> parameters;
    private List<ElementalChartEntryDto> elementalChart;
    private List<CardDefinitionDto> cards;
}
