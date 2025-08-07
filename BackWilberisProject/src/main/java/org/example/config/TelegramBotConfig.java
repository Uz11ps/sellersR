package org.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class TelegramBotConfig {
    
    @Value("${telegram.bot.token}")
    private String token;
    
    @Value("${telegram.bot.username}")
    private String username;
    
    public String getToken() {
        return token;
    }
    
    public String getUsername() {
        return username;
    }
} 