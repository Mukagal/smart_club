package com.miras.smartclub.service;

import com.miras.smartclub.enums.Role;
import com.miras.smartclub.model.User;
import com.miras.smartclub.model.dto.LoginRequest;
import com.miras.smartclub.model.dto.RegisterRequest;
import com.miras.smartclub.repository.UserRepository;
import com.miras.smartclub.util.PasswordValidator;
import com.miras.smartclub.util.PhoneUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        String normalizedPhone = PhoneUtils.normalize(request.getPhone());
        request.setPhone(normalizedPhone);

        Optional<User> existing = userRepository.findByPhone(normalizedPhone);
        if (existing.isPresent()) {
            throw new RuntimeException("User with this phone already exists");
        }

        if (!PasswordValidator.isValid(request.getPassword())) {
            throw new RuntimeException("Password must be at least 8 characters long and include at least one uppercase letter and one digit");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(normalizedPhone);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        return userRepository.save(user);
    }

    public User login(LoginRequest request) {
        String normalizedPhone = PhoneUtils.normalize(request.getPhone());
        Optional<User> found = userRepository.findByPhone(normalizedPhone);
        if (found.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User existingUser = found.get();
        if (!passwordEncoder.matches(request.getPassword(), existingUser.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        return existingUser;
    }

    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * Обновление профиля (firstName, lastName, phone, password optional).
     */
    public User updateProfile(String userId, String firstName, String lastName, String phone, String rawPassword) {
        User u = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        if (firstName != null) u.setFirstName(firstName);
        if (lastName != null) u.setLastName(lastName);
        if (phone != null && !phone.isBlank()) u.setPhone(PhoneUtils.normalize(phone));

        if (rawPassword != null && !rawPassword.isBlank()) {
            if (!PasswordValidator.isValid(rawPassword)) {
                throw new RuntimeException("Password does not meet policy");
            }
            u.setPassword(passwordEncoder.encode(rawPassword));
        }

        return userRepository.save(u);
    }
}
