package org.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // CORS настройки перенесены в SecurityConfig для избежания конфликтов
    // Здесь только базовая веб-конфигурация если потребуется

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS настройки убраны - используются из SecurityConfig
        System.out.println("🌐 WebConfig initialized (CORS настройки в SecurityConfig)");
    }
} 