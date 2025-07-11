package com.wordonline.server.matching.dto;

import com.wordonline.server.auth.domain.KakaoUser;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MatchedInfoDto{
    private String message;
    private KakaoUser leftUser;
    private KakaoUser rightUser;
    private String sessionId;
}
