package com.utcn.demo.controller;

import com.utcn.demo.entity.User;
import com.utcn.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService     userService;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserService userService,
                          PasswordEncoder passwordEncoder) {
        this.userService     = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * LOGIN:
     *  - Return 403 if user is banned
     *  - Return 401 if bad credentials
     *  - Otherwise return basic user info (or a JWT, etc.)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        // 1) Look up user
        Optional<User> userOpt = userService.getUserByUsername(username);
        if (userOpt.isEmpty()) {
            // no such user
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        User user = userOpt.get();
        System.out.println(user);

        // 2) BANNED check
        if (user.getBanned() == true) {
            System.out.println(1);
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Your account has been banned"));
        }

        // 3) Password check
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println(2);
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        // 4) Success => return whatever you want (here: id, username, email)
        return ResponseEntity.ok(Map.of(
                "id",       user.getId().toString(),
                "username", user.getUsername(),
                "email",    user.getEmail(),
                "role",     user.getRole()
        ));
    }

    /**
     * REGISTER:
     *  - Creates a new user with default ROLE_USER
     *  - Returns 201 CREATED plus basic user info
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> payload) {
        String username = payload.get("username");
        String email = payload.get("email");
        String password = payload.get("password");
        String role = payload.get("role");
        String phone = payload.get("phone");

        // Validate role
        if (role == null || (!role.equals("USER") && !role.equals("MODERATOR"))) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Invalid role. Must be either 'USER' or 'MODERATOR'"));
        }

        // Check if username or email already exists
        if (userService.getUserByUsername(username).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Username already taken"));
        }

        if (userService.getUserByEmail(email).isPresent()) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", "Email already registered"));
        }

        // Create new user with the selected role
        User newUser = userService.createUser(
                username,
                email,
                password,
                role
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "id", newUser.getId().toString(),
                        "username", newUser.getUsername(),
                        "email", newUser.getEmail(),
                        "role", newUser.getRole()
                ));
    }
}
