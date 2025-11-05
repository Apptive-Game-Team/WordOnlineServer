package com.wordonline.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class LocalizationService {

    private final MessageSource messageSource;
    private final LocaleResolver localeResolver;

    public String getMessage(String code) {
        return getMessage(code, null);
    }

    public String getMessage(String code, Object[] args) {
        Locale locale = getCurrentLocale();
        return messageSource.getMessage(code, args, locale);
    }

    private Locale getCurrentLocale() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null && attributes.getRequest() != null) {
            return localeResolver.resolveLocale(attributes.getRequest());
        }
        return Locale.KOREAN;
    }
}
