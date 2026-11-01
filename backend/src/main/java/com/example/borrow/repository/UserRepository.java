package com.example.borrow.repository;

import com.example.borrow.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    List<User> findByRealNameContainingOrUsernameContaining(String realName, String username);
    List<User> findByRealName(String realName);
    List<User> findByRole(String role);
}
