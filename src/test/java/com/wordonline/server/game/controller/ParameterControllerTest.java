package com.wordonline.server.game.controller;

import com.wordonline.server.auth.config.JwtProvider;
import com.wordonline.server.auth.config.WebSecurityConfig;
import com.wordonline.server.game.domain.magic.parser.DatabaseMagicParser;
import com.wordonline.server.game.service.ParameterService;
import com.wordonline.server.service.LocalizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ParameterController.class)
@Import(WebSecurityConfig.class)
class ParameterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ParameterService parameterService;

    @MockBean
    private DatabaseMagicParser databaseMagicParser;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private LocalizationService localizationService;

    @Test
    @DisplayName("파라미터 캐시 무효화 테스트")
    void invalidateParameterCache() throws Exception {
        // when & then
        mockMvc.perform(post("/api/admin/invalidate"))
                .andExpect(status().isOk());

        verify(parameterService).invalidateCache();
        verify(databaseMagicParser).invalidateCache();
    }
}
