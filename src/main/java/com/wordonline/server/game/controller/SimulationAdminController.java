package com.wordonline.server.game.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wordonline.server.game.dto.simulation.SimulationBatchRequestDto;
import com.wordonline.server.game.dto.simulation.SimulationBatchResponseDto;
import com.wordonline.server.game.service.SimulationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'WORDONLINE_ADMIN')")
@RequestMapping("/api/admin/simulations")
public class SimulationAdminController {

    private final SimulationService simulationService;

    @PostMapping("/matchup")
    public ResponseEntity<SimulationBatchResponseDto> runMatchupBatch(
            @RequestBody SimulationBatchRequestDto requestDto
    ) {
        log.info(
                "runMatchupBatch leftUserId={}, rightUserId={}, matchCount={}, parameterProfileId={}",
                requestDto.leftUserId(),
                requestDto.rightUserId(),
                requestDto.matchCount(),
                requestDto.parameterProfileId()
        );
        return ResponseEntity.ok(simulationService.runFixedMatchupBatch(requestDto));
    }
}
