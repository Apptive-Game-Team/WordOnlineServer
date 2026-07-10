package com.wordonline.server.game.dto;

import java.util.ArrayList;
import java.util.List;

import com.wordonline.server.game.domain.object.GameObject;
import com.wordonline.server.game.domain.object.Vector3;
import com.wordonline.server.game.dto.frame.GaugeDto;
import com.wordonline.server.game.util.GaugeExtractor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@Setter
@AllArgsConstructor
// This class is used to send updated object information to the client
public class UpdatedObjectDto {
    private final int id;
    private Status status;
    private List<Effect> effects;
    private Master master;
    private Vector3 position;
    private List<GaugeDto> gauges = new ArrayList<>();

    public void updateGauges(GameObject gameObject) {
        GaugeExtractor.mergeGaugeDto(gameObject, gauges);
    }

    public UpdatedObjectDto(GameObject gameObject) {
        this.id = gameObject.getId();

        updateGauges(gameObject);

        this.status = gameObject.getStatus();
        this.effects = List.copyOf(gameObject.getEffects());
        this.master = gameObject.getMaster();
        this.position = gameObject.getPosition();
    }
}
