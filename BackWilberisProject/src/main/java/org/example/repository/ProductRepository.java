package org.example.repository;

import org.example.entity.Product;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByUserAndIsActiveTrue(User user);
    
    List<Product> findByUser(User user);
    
    Optional<Product> findByUserAndNmId(User user, Long nmId);
    
    List<Product> findByUserAndCategory(User user, String category);
    
    List<Product> findByUserAndColor(User user, String color);
    
    List<Product> findByUserOrderByCreatedAtDesc(User user);
    
    // Дополнительные методы для контроллера
    long countByUser(User user);
} 