package ru.nsu.carwash_server.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleConfiguration {
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver slr = new SessionLocaleResolver();
        slr.setDefaultLocale(new Locale("ru", "RU"));
        return slr;
    }
}
