package com.wordonline.server.auth.domain;

import com.wordonline.server.auth.dto.KakaoUserInfoResponseDto;

public record KakaoUser(long id, String email, String nickname, String imageUrl) {
    public static KakaoUser from(KakaoUserInfoResponseDto kakaoUserInfoResponseDto) {
        return new KakaoUser(
                kakaoUserInfoResponseDto.id,
                kakaoUserInfoResponseDto.kakaoAccount.email,
                kakaoUserInfoResponseDto.kakaoAccount.profile.nickName,
                kakaoUserInfoResponseDto.kakaoAccount.profile.profileImageUrl
        );
    }
}
