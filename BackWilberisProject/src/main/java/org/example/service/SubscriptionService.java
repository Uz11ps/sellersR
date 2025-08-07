package org.example.service;

import org.example.entity.Subscription;
import org.example.entity.User;
import org.example.repository.SubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionService {
    
    @Autowired
    private SubscriptionRepository subscriptionRepository;
    
    /**
     * Проверяет, есть ли у пользователя активная подписка
     */
    public boolean hasActiveSubscription(User user) {
        // Получаем активные подписки пользователя
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserAndStatus(
            user, Subscription.SubscriptionStatus.ACTIVE);
        
        // Проверяем, есть ли активные подписки, которые не истекли
        LocalDateTime now = LocalDateTime.now();
        return activeSubscriptions.stream()
            .anyMatch(subscription -> subscription.getEndDate().isAfter(now));
    }
    
    /**
     * Получает активную подписку пользователя
     */
    public Subscription getActiveSubscription(User user) {
        // Получаем активные подписки пользователя
        List<Subscription> activeSubscriptions = subscriptionRepository.findByUserAndStatus(
            user, Subscription.SubscriptionStatus.ACTIVE);
        
        LocalDateTime now = LocalDateTime.now();
        
        // Находим первую не истекшую активную подписку
        Optional<Subscription> activeSubscription = activeSubscriptions.stream()
            .filter(subscription -> subscription.getEndDate().isAfter(now))
            .findFirst();
        
        // Если нашли активную подписку, возвращаем ее
        if (activeSubscription.isPresent()) {
            return activeSubscription.get();
        }
        
        // Если активных подписок нет, проверяем, есть ли истекшие активные подписки
        // и деактивируем их
        activeSubscriptions.stream()
            .filter(subscription -> subscription.getEndDate().isBefore(now))
            .forEach(subscription -> {
                subscription.setStatus(Subscription.SubscriptionStatus.EXPIRED);
                subscriptionRepository.save(subscription);
            });
        
        return null;
    }
    
    /**
     * Получает все подписки пользователя
     */
    public List<Subscription> getUserSubscriptions(User user) {
        return subscriptionRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Создает новую подписку для пользователя
     */
    public Subscription createSubscription(User user, String planType, Double price) {
        // Проверяем, есть ли у пользователя активная подписка
        if (hasActiveSubscription(user)) {
            throw new RuntimeException("У пользователя уже есть активная подписка");
        }
        
        // Создаем новую подписку
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        
        // Устанавливаем тип плана
        try {
            subscription.setPlanType(Subscription.PlanType.valueOf(planType));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Неверный тип плана: " + planType);
        }
        
        // Устанавливаем цену
        subscription.setPrice(price);
        
        // Устанавливаем даты
        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        
        // Устанавливаем дату окончания в зависимости от типа плана
        int durationDays = switch (subscription.getPlanType()) {
            case PLAN_30_DAYS -> 30;
            case PLAN_60_DAYS -> 60;
            case PLAN_90_DAYS -> 90;
            case PLAN_FREE -> 7;
        };
        
        subscription.setEndDate(now.plusDays(durationDays));
        
        // Устанавливаем статус
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        
        // Сохраняем подписку
        return subscriptionRepository.save(subscription);
    }
    
    /**
     * Создает бесплатную пробную подписку для пользователя
     */
    public Subscription createTrialSubscription(User user) {
        // Проверяем, есть ли у пользователя активная подписка
        if (hasActiveSubscription(user)) {
            throw new RuntimeException("У пользователя уже есть активная подписка");
        }
        
        // Создаем новую пробную подписку
        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setPlanType(Subscription.PlanType.PLAN_FREE);
        subscription.setStatus(Subscription.SubscriptionStatus.ACTIVE);
        subscription.setPrice(0.0);
        
        // Устанавливаем даты
        LocalDateTime now = LocalDateTime.now();
        subscription.setStartDate(now);
        subscription.setEndDate(now.plusDays(7)); // 7 дней пробного периода
        
        // Сохраняем подписку
        return subscriptionRepository.save(subscription);
    }
    
    /**
     * Отменяет подписку пользователя
     */
    public void cancelSubscription(User user) {
        // Получаем активную подписку пользователя
        Subscription subscription = getActiveSubscription(user);
        
        if (subscription == null) {
            throw new RuntimeException("У пользователя нет активной подписки");
        }
        
        // Отменяем подписку
        subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
        subscriptionRepository.save(subscription);
    }
} 