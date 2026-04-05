package com.wordonline.server.data.controller;

import com.wordonline.server.data.dto.GameConfigDto;
import com.wordonline.server.data.dto.GameDataVersionDto;
import com.wordonline.server.data.service.GameDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data")
@RequiredArgsConstructor
public class GameDataController {

    private final GameDataService gameDataService;

    @GetMapping("/version")
    public ResponseEntity<GameDataVersionDto> getVersion() {
        return ResponseEntity.ok(new GameDataVersionDto(gameDataService.getVersion()));
    }

    @GetMapping("/config")
    public ResponseEntity<GameConfigDto> getConfig() {
        return ResponseEntity.ok(gameDataService.getConfig());
    }
}
