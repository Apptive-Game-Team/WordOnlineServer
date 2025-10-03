package com.wordonline.server.matching.dto;

import com.wordonline.server.auth.dto.UserDetailResponseDto;
import com.wordonline.server.auth.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedInfoDto{
    private String message;
    private UserDetailResponseDto leftUser;
    private UserDetailResponseDto rightUser;
    private String sessionId;
}
