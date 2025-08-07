package org.example.repository;

import org.example.entity.Seller;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    
    List<Seller> findByUser(User user);
    
    List<Seller> findByUserAndIsActiveTrue(User user);
    
    Optional<Seller> findByUserAndId(User user, Long sellerId);
    
    Optional<Seller> findByInn(String inn);
    
    @Query("SELECT s FROM Seller s WHERE s.user = :user AND s.wbApiKey IS NOT NULL AND s.wbApiKey != ''")
    List<Seller> findByUserWithApiKey(@Param("user") User user);
    
    @Query("SELECT COUNT(s) FROM Seller s WHERE s.user = :user AND s.isActive = true")
    long countActiveByUser(@Param("user") User user);
    
    boolean existsByUserAndInn(User user, String inn);
} 