package com.wordonline.server.exception;

import com.wordonline.server.service.LocalizationService;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final LocalizationService localizationService = mock(LocalizationService.class);
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(localizationService);

    @Test
    void unhandledExceptionRespondsWithInternalServerError() {
        when(localizationService.getMessage("error.internal")).thenReturn("서버 내부 오류가 발생했습니다.");

        ResponseEntity<String> response = handler.handleException(new NullPointerException("boom"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("서버 내부 오류가 발생했습니다.");
    }

    @Test
    void internalErrorMessageIsDefinedInEveryBundle() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");

        for (Locale locale : new Locale[]{Locale.KOREAN, Locale.ENGLISH}) {
            assertThat(messageSource.getMessage("error.internal", null, locale)).isNotBlank();
        }
    }
}
