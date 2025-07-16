package com.wordonline.server.matching.dto;

import com.wordonline.server.auth.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedInfoDto{
    private String message;
    private UserResponseDto leftUser;
    private UserResponseDto rightUser;
    private String sessionId;
}
