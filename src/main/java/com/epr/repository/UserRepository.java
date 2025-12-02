// src/main/java/com/epr/repository/UserRepository.java
package com.epr.repository;

import com.epr.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT u FROM User u WHERE u.deleteStatus = 2 AND u.displayStatus = '1' ORDER BY u.fullName ASC")
    List<User> findAllActiveUsers();

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleteStatus = 2")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END " +
            "FROM User u WHERE u.email = :email AND u.deleteStatus = 2 AND (:id IS NULL OR u.id != :id)")
    boolean existsByEmailIgnoreCaseAndNotId(@Param("email") String email, @Param("id") Long id);

    @Query("SELECT u FROM User u WHERE (u.fullName LIKE %:keyword% OR u.email LIKE %:keyword%) AND u.deleteStatus = 2")
    List<User> searchByFullNameOrEmail(@Param("keyword") String keyword);

    @Query("SELECT u FROM User u WHERE u.id = :id AND u.deleteStatus = 2")
    Optional<User> findActiveUserById(@Param("id") Long id);

    Optional<User> findByUuid(String uuid);

    // Optional: Find by email (useful for login)
    Optional<User> findByEmailIgnoreCaseAndDeleteStatus(String email, int deleteStatus);
}