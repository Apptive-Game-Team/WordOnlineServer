package com.wordonline.server.game.util;

import java.util.ArrayList;
import java.util.List;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.component.GaugeComponent;
import com.wordonline.server.game.dto.frame.GaugeCategory;
import com.wordonline.server.game.dto.frame.GaugeDto;

public final class GaugeExtractor {

    public static List<GaugeDto> extractGaugeDto(GameObject gameObject) {
        List<GaugeDto> gauges = new ArrayList<>();
        mergeGaugeDto(gameObject, gauges);
        return gauges;
    }

    public static void mergeGaugeDto(GameObject gameObject, List<GaugeDto> gauges) {
        for (GaugeComponent gaugeComponent : gameObject.getComponents(GaugeComponent.class)) {
            GaugeDto gaugeDto = gaugeComponent.getGauge();
            if (gaugeDto.maxValue() < 0) {
                continue;
            }

            removeGaugeByCategory(gauges, gaugeDto.category());
            gauges.add(gaugeDto);
        }
    }

    private static void removeGaugeByCategory(List<GaugeDto> gauges, GaugeCategory category) {
        for (int i = 0; i < gauges.size(); i++) {
            if (gauges.get(i).category().equals(category)) {
                gauges.remove(i);
                return;
            }
        }
    }
}
