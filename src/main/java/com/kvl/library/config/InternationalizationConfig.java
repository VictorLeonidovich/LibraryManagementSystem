package com.kvl.library.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.Locale;

/**
 * Конфигурационный класс для активации и настройки интернационализации (i18n).
 * <p>
 * Обеспечивает поддержку мультиязычности в веб-интерфейсе приложения.
 * Хранит выбранную локаль пользователя в куках браузера и перехватывает
 * параметры смены языка из URL-запросов.
 */
@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    /**
     * Имя параметра в URL для принудительной смены языка (например, ?lang=en).
     */
    public static final String LANG_PARAMETER = "lang";

    /**
     * Имя куки-файла для сохранения выбранной локали в браузере пользователя.
     */
    public static final String LANG_COOKIE_NAME = "lang";

    @Bean
    public LocaleResolver localeResolver() {
        final CookieLocaleResolver resolver = new CookieLocaleResolver(LANG_COOKIE_NAME);

        // Устанавливаем язык по умолчанию через BCP 47
        resolver.setDefaultLocale(Locale.forLanguageTag("ru"));

        // Кука будет храниться 365 дней (в секундах)
        resolver.setCookieMaxAge(java.time.Duration.ofDays(365));

        return resolver;
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName(LANG_PARAMETER);

        return interceptor;
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}