package com.utcn.demo.repository;

import com.utcn.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findById(Long id);
    Optional<User> findByUsernameAndIsBannedFalse(String username);
    Optional<User> findByIdAndIsBannedFalse(Long id);


    Optional<Object> findByEmail(String email);
}

