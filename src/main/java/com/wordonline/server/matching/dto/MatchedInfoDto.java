package com.wordonline.server.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedInfoDto{
    private String message, leftUserId, rightUserId, sessionId;
}
