package com.wordonline.server.auth.service;

import com.wordonline.server.auth.dto.KakaoTokenResponseDto;
import com.wordonline.server.auth.dto.KakaoUserInfoResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
public class KakaoAuthService {

    private final String clientId;
    private final String KAUTH_TOKEN_URL_HOST ="https://kauth.kakao.com";
    private final String KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    private final String redirectUri;

    private final RestClient restTokenClient = RestClient.builder().baseUrl(KAUTH_TOKEN_URL_HOST).build();
    private final RestClient restUserClient = RestClient.builder().baseUrl(KAUTH_USER_URL_HOST).build();

    @Autowired
    public KakaoAuthService(@Value("${kakao.client-id}") String clientId, @Value("${kakao.redirect-uri}") String redirectUri) {
        this.clientId = clientId;
        this.redirectUri = redirectUri;
    }

    public HttpHeaders getLoginRedirectHeader() {
        URI redirectUrl = UriComponentsBuilder.fromUriString(KAUTH_TOKEN_URL_HOST + "/oauth/authorize")
                .queryParam("response_type", "code")
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .build()
                .encode()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(redirectUrl);
        return headers;
    }

    public String getAccessTokenFromKakao(String code) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", clientId);
        formData.add("code", code);
        formData.add("redirect_uri", redirectUri);

        KakaoTokenResponseDto responseDto = restTokenClient.post()
                .uri("/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(formData)
                .retrieve()
                .body(KakaoTokenResponseDto.class);


        log.info(" [Kakao Service] Access Token ------> {}", responseDto.getAccessToken());
        log.info(" [Kakao Service] Refresh Token ------> {}", responseDto.getRefreshToken());
        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
        log.info(" [Kakao Service] Id Token ------> {}", responseDto.getIdToken());
        log.info(" [Kakao Service] Scope ------> {}", responseDto.getScope());

        return responseDto.getAccessToken();
    }

    public KakaoUserInfoResponseDto getUserInfo(String accessToken) {
        KakaoUserInfoResponseDto userInfo = restUserClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .retrieve()
                .body(KakaoUserInfoResponseDto.class);

        log.info("[ Kakao Service ] Auth ID ---> {} ", userInfo.getId());
        log.info("[ Kakao Service ] NickName ---> {} ", userInfo.getKakaoAccount().getProfile().getNickName());
        log.info("[ Kakao Service ] ProfileImageUrl ---> {} ", userInfo.getKakaoAccount().getProfile().getProfileImageUrl());

        return userInfo;
    }
}