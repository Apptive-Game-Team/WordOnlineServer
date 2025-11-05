package com.wordonline.server.config;

import com.wordonline.server.service.LocalizationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class LocalizationConfigTest {

    private MessageSource createMessageSource() {
        ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setUseCodeAsDefaultMessage(true);
        return messageSource;
    }

    private LocaleResolver createLocaleResolver() {
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.KOREAN);
        return localeResolver;
    }

    @Test
    @DisplayName("한국어 메시지 소스 테스트")
    void koreanMessageSource() {
        // given
        MessageSource messageSource = createMessageSource();
        Locale koreanLocale = Locale.KOREAN;

        // when
        String message = messageSource.getMessage("error.invalid.request", null, koreanLocale);

        // then
        assertThat(message).isEqualTo("요청이 잘못됐습니다.");
    }

    @Test
    @DisplayName("영어 메시지 소스 테스트")
    void englishMessageSource() {
        // given
        MessageSource messageSource = createMessageSource();
        Locale englishLocale = Locale.ENGLISH;

        // when
        String message = messageSource.getMessage("error.invalid.request", null, englishLocale);

        // then
        assertThat(message).isEqualTo("Invalid request.");
    }

    @Test
    @DisplayName("파라미터가 있는 메시지 테스트")
    void messageWithParameters() {
        // given
        MessageSource messageSource = createMessageSource();
        Locale englishLocale = Locale.ENGLISH;
        long userId = 123L;

        // when
        String message = messageSource.getMessage("error.user.not.found", new Object[]{userId}, englishLocale);

        // then
        assertThat(message).isEqualTo("User not found: 123");
    }

    @Test
    @DisplayName("LocalizationService로 한국어 메시지 가져오기 테스트")
    void localizationServiceKorean() {
        // given
        MessageSource messageSource = createMessageSource();
        LocaleResolver localeResolver = createLocaleResolver();
        LocalizationService localizationService = new LocalizationService(messageSource, localeResolver);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "ko");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        String message = localizationService.getMessage("error.unauthorized");

        // then
        assertThat(message).isEqualTo("인증되지 않았습니다.");

        // cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("LocalizationService로 영어 메시지 가져오기 테스트")
    void localizationServiceEnglish() {
        // given
        MessageSource messageSource = createMessageSource();
        LocaleResolver localeResolver = createLocaleResolver();
        LocalizationService localizationService = new LocalizationService(messageSource, localeResolver);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        String message = localizationService.getMessage("error.unauthorized");

        // then
        assertThat(message).isEqualTo("Unauthorized.");

        // cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("LocalizationService로 파라미터가 있는 메시지 가져오기 테스트")
    void localizationServiceWithParameters() {
        // given
        MessageSource messageSource = createMessageSource();
        LocaleResolver localeResolver = createLocaleResolver();
        LocalizationService localizationService = new LocalizationService(messageSource, localeResolver);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept-Language", "en");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        long userId = 456L;

        // when
        String message = localizationService.getMessage("error.user.not.found", new Object[]{userId});

        // then
        assertThat(message).isEqualTo("User not found: 456");

        // cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("기본 로케일 테스트 (헤더 없을 때)")
    void defaultLocaleTest() {
        // given
        MessageSource messageSource = createMessageSource();
        LocaleResolver localeResolver = createLocaleResolver();
        LocalizationService localizationService = new LocalizationService(messageSource, localeResolver);
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        // when
        String message = localizationService.getMessage("error.invalid.request");

        // then - default is Korean
        assertThat(message).isEqualTo("요청이 잘못됐습니다.");

        // cleanup
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    @DisplayName("존재하지 않는 메시지 키에 대한 기본 동작 테스트")
    void nonExistentKeyTest() {
        // given
        MessageSource messageSource = createMessageSource();
        Locale englishLocale = Locale.ENGLISH;

        // when
        String message = messageSource.getMessage("error.nonexistent.key", null, englishLocale);

        // then - returns the key itself as default
        assertThat(message).isEqualTo("error.nonexistent.key");
    }
}
