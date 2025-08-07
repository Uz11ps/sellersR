package org.example.service;

import org.example.config.TelegramBotConfig;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TelegramBotService extends TelegramLongPollingBot {
    
    private final TelegramBotConfig telegramBotConfig;
    
    @Autowired
    private UserRepository userRepository;
    
    public TelegramBotService(TelegramBotConfig telegramBotConfig) {
        super(telegramBotConfig.getToken());
        this.telegramBotConfig = telegramBotConfig;
    }
    
    @Override
    public String getBotUsername() {
        return telegramBotConfig.getUsername();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            String text = message.getText();
            
            System.out.println("📩 Получено сообщение от Telegram: " + text);
            
            // Обрабатываем команды
            if (text.startsWith("/start")) {
                sendWelcomeMessage(chatId);
            } else if (text.startsWith("/help")) {
                sendHelpMessage(chatId);
            } else if (text.startsWith("/verify")) {
                processVerificationCode(chatId, text);
            } else {
                // Пытаемся обработать как код верификации
                processVerificationCode(chatId, text);
            }
        }
    }
    
    /**
     * Отправляет приветственное сообщение
     */
    private void sendWelcomeMessage(String chatId) {
        String message = "👋 Добро пожаловать в бот Sellers Wildberries!\n\n" +
                "Этот бот поможет вам верифицировать ваш аккаунт и получать уведомления о важных событиях.\n\n" +
                "Для верификации аккаунта отправьте код, который вы получили при регистрации, или используйте команду /verify КОД_ВЕРИФИКАЦИИ.\n\n" +
                "Для получения помощи используйте команду /help.";
        
        sendMessage(chatId, message);
    }
    
    /**
     * Отправляет справочное сообщение
     */
    private void sendHelpMessage(String chatId) {
        String message = "🔍 Доступные команды:\n\n" +
                "/start - Начать работу с ботом\n" +
                "/help - Показать это сообщение\n" +
                "/verify КОД - Верифицировать аккаунт с помощью кода\n\n" +
                "Вы также можете просто отправить код верификации без команды.";
        
        sendMessage(chatId, message);
    }
    
    /**
     * Обрабатывает код верификации
     */
    private void processVerificationCode(String chatId, String text) {
        // Извлекаем код верификации из текста
        String verificationCode = text;
        
        // Если это команда /verify, извлекаем код из команды
        if (text.startsWith("/verify")) {
            String[] parts = text.split(" ", 2);
            if (parts.length < 2) {
                sendMessage(chatId, "❌ Пожалуйста, укажите код верификации после команды /verify");
                return;
            }
            verificationCode = parts[1].trim();
        }
        
        // Проверяем, что код имеет правильный формат (6 цифр)
        if (!verificationCode.matches("\\d{6}")) {
            sendMessage(chatId, "❌ Неверный формат кода верификации. Код должен состоять из 6 цифр.");
            return;
        }
        
        // Ищем пользователя с таким кодом верификации
        Optional<User> userOptional = userRepository.findByVerificationCode(verificationCode);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            // Проверяем, не истек ли срок действия кода
            if (user.getVerificationExpiresAt() != null && 
                user.getVerificationExpiresAt().isBefore(LocalDateTime.now())) {
                sendMessage(chatId, "❌ Код верификации истек. Пожалуйста, запросите новый код в приложении.");
                return;
            }
            
            // Верифицируем пользователя
            user.setIsVerified(true);
            user.setVerificationCode(null);
            user.setVerificationExpiresAt(null);
            user.setTelegramChatId(chatId);
            user.setUpdatedAt(LocalDateTime.now());
            userRepository.save(user);
            
            // Отправляем подтверждение
            sendMessage(chatId, "✅ Аккаунт успешно верифицирован! Теперь вы будете получать уведомления в этом чате.");
            
            // Отправляем приветственное сообщение с информацией о пользователе
            String welcomeMessage = "👤 Информация о вашем аккаунте:\n\n" +
                    "Email: " + user.getEmail() + "\n" +
                    "Имя: " + (user.getFirstName() != null ? user.getFirstName() : "Не указано") + "\n" +
                    "Фамилия: " + (user.getLastName() != null ? user.getLastName() : "Не указана") + "\n\n" +
                    "Теперь вы можете использовать все функции приложения Sellers Wildberries!";
            
            sendMessage(chatId, welcomeMessage);
        } else {
            sendMessage(chatId, "❌ Неверный код верификации. Пожалуйста, проверьте код и попробуйте снова.");
        }
    }
    
    /**
     * Отправляет сообщение пользователю
     */
    public void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        
        try {
            execute(message);
            System.out.println("✅ Сообщение отправлено в Telegram: " + chatId);
        } catch (TelegramApiException e) {
            System.err.println("❌ Ошибка отправки сообщения в Telegram: " + e.getMessage());
        }
    }
} 