package org.example.repository;

import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationCode(String verificationCode);
    
    Optional<User> findByTelegramChatId(Long telegramChatId);
    
    boolean existsByEmail(String email);
} 