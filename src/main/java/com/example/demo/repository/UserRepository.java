package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhone(String phone);
    
    List<User> findByRole(UserRole role);
    
    @Query("SELECT u FROM User u WHERE u.firstname LIKE %:keyword% OR u.lastname LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> searchUsers(@Param("keyword") String keyword);
    
    @Query("SELECT u FROM User u WHERE u.role = :role ORDER BY u.createdAt DESC")
    List<User> findRecentUsersByRole(@Param("role") UserRole role);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.addresses WHERE u.email = :email")
    Optional<User> findByEmailWithAddresses(@Param("email") String email);
}
