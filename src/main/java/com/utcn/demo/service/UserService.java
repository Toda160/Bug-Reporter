package com.utcn.demo.service;

import com.utcn.demo.entity.User;
import com.utcn.demo.repository.UserRepository;
import com.utcn.demo.service.EmailService;
import com.utcn.demo.service.SmsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService     emailService;
    private final SmsService       smsService;


    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       SmsService smsService) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService    = emailService;
        this.smsService      = smsService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String username, String email, String password, String role) {
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password)); // Criptăm parola
        newUser.setRole(role);
        newUser.setScore(0.0); // Initialize score to 0.0 for new users

        return userRepository.save(newUser);
    }
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

/*
    public Optional<User> getNonBannedUserByUsername(String username) {
        return userRepository.findByUsernameAndIsBannedFalse(username);
    }

   // public Optional<User> getUserById(Long id) {
    //    return userRepository.findByIdAndIsBannedFalse(id);
    }
  */

    @Transactional
    public boolean deleteUser(Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<User> updateUser(Long id, String username, String email, String role, String password) {
        Optional<User> userOptional = userRepository.findById(id);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (username != null) user.setUsername(username);
            if (email != null) user.setEmail(email);
            if (role != null) user.setRole(role);

            // Dacă se trimite o parolă nouă, o criptăm și o actualizăm
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password)); // Criptăm parola
            }

            return Optional.of(userRepository.save(user));
        } else {
            return Optional.empty();
        }
    }

    // Method to update user score
    @Transactional
    public void updateUserScore(Long userId, double scoreChange) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Handle null score: If current score is null, treat it as 0.0
            double currentScore = (user.getScore() == null) ? 0.0 : user.getScore();
            user.setScore(currentScore + scoreChange);
            userRepository.save(user);
        }
        // If user is not found, we might want to log a warning,
        // but for now, silently fail if user ID is invalid.
    }

    //------------------bonus

    @Transactional
    public void banUser(Long userId, String reason) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));

        if (u.getBanned() == false) {
            u.setBanned(true);
            userRepository.save(u);

            // send notifications
            emailService.send(
                    u.getEmail(),
                    "You have been banned",
                    "You were banned for the following reason:\n" + reason
            );
            smsService.send(
                    u.getPhone(),
                    "Your account has been banned. Reason: " + reason
            );
        }
    }

    /**
     * Un‐ban a previously banned user, and notify them by email & SMS.
     */
    @Transactional
    public void unbanUser(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("No user with id " + userId));

        if (u.getBanned() == true) {
            u.setBanned(false);
            userRepository.save(u);

            emailService.send(
                    u.getEmail(),
                    "Your account has been un-banned",
                    "A moderator has un-banned your account. You may now log in again."
            );
            smsService.send(
                    u.getPhone(),
                    "Your ban has been lifted. Welcome back!"
            );
        }
    }

    public Optional<Object> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}