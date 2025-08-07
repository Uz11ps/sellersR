package org.example;

import org.example.service.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class WilberisAnalyticsApplication implements CommandLineRunner {

    @Autowired
    private TelegramBotService telegramBotService;

    public static void main(String[] args) {
        System.out.println("🚀 Запуск Wilberis Analytics Application...");
        SpringApplication.run(WilberisAnalyticsApplication.class, args);
        System.out.println("✅ Приложение успешно запущено!");
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(telegramBotService);
            System.out.println("🤖 Telegram бот успешно зарегистрирован и запущен!");
            System.out.println("📱 Бот готов к приему сообщений: " + telegramBotService.getBotUsername());
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка регистрации Telegram бота: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 