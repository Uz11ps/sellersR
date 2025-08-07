package org.example.repository;

import org.example.entity.Subscription;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Находит подписки пользователя с указанным статусом
     */
    List<Subscription> findByUserAndStatus(User user, Subscription.SubscriptionStatus status);
    
    /**
     * Находит все подписки пользователя
     */
    List<Subscription> findByUser(User user);
    
    /**
     * Находит все подписки пользователя, отсортированные по дате создания (сначала новые)
     */
    List<Subscription> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Находит подписки, срок действия которых истекает в указанный период
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate BETWEEN :now AND :endDate")
    List<Subscription> findExpiringSubscriptions(@Param("now") LocalDateTime now, @Param("endDate") LocalDateTime endDate);
    
    /**
     * Находит истекшие подписки, которые все еще помечены как активные
     */
    @Query("SELECT s FROM Subscription s WHERE s.status = 'ACTIVE' AND s.endDate < :now")
    List<Subscription> findExpiredActiveSubscriptions(@Param("now") LocalDateTime now);
} 