package com.wordonline.server.auth.controller;

import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.config.WebSecurityConfig;
import com.wordonline.server.auth.domain.PrincipalDetails;
import com.wordonline.server.auth.domain.UserStatus;
import com.wordonline.server.auth.dto.UserResponseDto;
import com.wordonline.server.auth.service.UserService;
import com.wordonline.server.service.LocalizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(WebSecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private LocalizationService localizationService;

    private Authentication authentication;

    @BeforeEach
    void setUp() {
        long userId = 1L;
        PrincipalDetails principalDetails = new PrincipalDetails(userId);
        authentication = new UsernamePasswordAuthenticationToken(principalDetails, null, principalDetails.getAuthorities());
    }

    @Test
    @DisplayName("내 정보 조회 테스트")
    void getUser() throws Exception {
        // given
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        long memberId = principalDetails.memberId;
        UserResponseDto userResponseDto = new UserResponseDto(memberId, 1L);
        given(userService.getUser(memberId)).willReturn(userResponseDto);

        // when & then
        mockMvc.perform(get("/api/users/mine")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("회원 탈퇴 성공 테스트")
    void deleteUser_success() throws Exception {
        // given
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        long memberId = principalDetails.memberId;
        given(userService.deleteUser(memberId)).willReturn(true);

        // when & then
        mockMvc.perform(delete("/api/users/mine")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("successfully delete"));
    }

    @Test
    @DisplayName("회원 탈퇴 실패 테스트 - 사용자를 찾을 수 없음")
    void deleteUser_notFound() throws Exception {
        // given
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        long memberId = principalDetails.memberId;
        given(userService.deleteUser(memberId)).willReturn(false);

        // when & then
        mockMvc.perform(delete("/api/users/mine")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("내 상태 조회 테스트")
    void getMyStatus() throws Exception {
        // given
        PrincipalDetails principalDetails = (PrincipalDetails) authentication.getPrincipal();
        long memberId = principalDetails.memberId;
        given(userService.getStatus(memberId)).willReturn(UserStatus.Online);

        // when & then
        mockMvc.perform(get("/api/users/mine/status")
                        .with(authentication(authentication))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Online"));
    }
}
