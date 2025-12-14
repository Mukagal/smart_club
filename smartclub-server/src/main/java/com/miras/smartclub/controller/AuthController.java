package com.miras.smartclub.controller;

import com.miras.smartclub.model.User;
import com.miras.smartclub.model.dto.LoginRequest;
import com.miras.smartclub.model.dto.RegisterRequest;
import com.miras.smartclub.service.UserService;
import com.miras.smartclub.util.PhoneUtils;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            request.setPhone(PhoneUtils.normalize(request.getPhone()));
            User savedUser = userService.register(request);
            savedUser.setPassword(null);
            return ResponseEntity.status(201).body(savedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpSession session) {
        try {
            request.setPhone(PhoneUtils.normalize(request.getPhone()));
            User loggedIn = userService.login(request);

            session.setAttribute("userId", loggedIn.getId());
            session.setAttribute("role", loggedIn.getRole());

            loggedIn.setPassword(null);
            return ResponseEntity.ok(loggedIn);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(java.util.Map.of("message", "Logout successful"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("error", "Not logged in"));
        }
        User user = userService.findById(userId);
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    /**
     * Обновление профиля текущего пользователя.
     * Тело: { firstName, lastName, phone, password (optional) }
     */
    @PostMapping("/update")
    public ResponseEntity<?> updateProfile(@RequestBody java.util.Map<String, String> body, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(java.util.Map.of("error", "Not authenticated"));

        try {
            String firstName = body.get("firstName");
            String lastName = body.get("lastName");
            String phone = body.get("phone");
            String password = body.get("password");

            User updated = userService.updateProfile(userId, firstName, lastName, phone, password);
            updated.setPassword(null);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
