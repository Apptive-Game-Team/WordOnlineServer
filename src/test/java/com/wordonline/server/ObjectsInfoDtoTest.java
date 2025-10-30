package com.wordonline.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordonline.server.game.dto.frame.projectile.PositionProjectileTarget;
import com.wordonline.server.game.dto.frame.projectile.ProjectileDto;
import com.wordonline.server.game.dto.frame.projectile.ReferenceProjectileTarget;

public class ObjectsInfoDtoTest {

    @Test
    void print_projectile_dto_json() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        // 예시 객체
        ProjectileDto projectileDto = new ProjectileDto(
          "WaterShot",
          new ReferenceProjectileTarget(1),
                new PositionProjectileTarget(1, 2, 3),
                0.3f
        );

        // 객체 → JSON 문자열 변환
        String jsonString = mapper.writeValueAsString(projectileDto);

        System.out.println(jsonString);
    }
}
