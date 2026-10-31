package com.example.ecommerce.service;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;

@Service
public class AuthService {
    public static final int MIN_USERNAME_LENGTH = 3;
    public static final int MAX_USERNAME_LENGTH = 50;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 72;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,50}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{10,15}$");

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository) { this.userRepository = userRepository; }

    public User register(Map<String, String> data) {
        if (data == null) throw badRequest("Registration details are required");
        String fullName = value(data, "fullName");
        String email = value(data, "email").toLowerCase(Locale.ROOT);
        String phone = value(data, "phone");
        String username = value(data, "username");
        String password = rawValue(data, "password");
        String confirmPassword = rawValue(data, "confirmPassword");

        if (fullName.isBlank()) throw badRequest("Full name is required");
        if (fullName.length() > 100) throw badRequest("Full name must not exceed 100 characters");
        if (username.isBlank()) throw badRequest("Username is required");
        validateUsername(username);
        if (email.isBlank() && phone.isBlank()) throw badRequest("Email or phone number is required");
        if (!email.isBlank()) {
            if (email.length() > 100) throw badRequest("Email must not exceed 100 characters");
            if (!EMAIL_PATTERN.matcher(email).matches()) throw badRequest("Please enter a valid email address");
            if (userRepository.existsByEmail(email)) throw conflict("Email already exists");
        }
        if (!phone.isBlank()) {
            if (!PHONE_PATTERN.matcher(phone).matches()) throw badRequest("Phone number must contain 10 to 15 digits");
            if (userRepository.existsByPhone(phone)) throw conflict("Phone number already exists");
        }
        if (password.isBlank()) throw badRequest("Password is required");
        validatePassword(password);
        if (data.containsKey("confirmPassword") && !password.equals(confirmPassword)) {
            throw badRequest("Passwords do not match");
        }
        if (userRepository.existsByUsername(username)) throw conflict("Username already exists");

        User user = new User();
        user.setFullName(fullName);
        // Some existing Oracle schemas declare EMAIL NOT NULL. Phone-only
        // accounts receive a non-routable placeholder and can sign in by phone.
        user.setEmail(email.isBlank() ? "phone." + phone + "@local.invalid" : email);
        user.setPhone(phone.isBlank() ? null : phone);
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    public User authenticate(String login, String password) {
        if (login == null || login.isBlank()) throw badRequest("Username/email is required");
        if (password == null || password.isBlank()) throw badRequest("Password is required");
        String identifier = login.trim();
        if (identifier.length() > 100) throw badRequest("Username/email must not exceed 100 characters");
        if (identifier.contains("@") && !EMAIL_PATTERN.matcher(identifier).matches()) throw badRequest("Invalid email format");
        if (!identifier.contains("@") && !identifier.matches("[0-9]{10,15}")) validateUsername(identifier);
        if (password.length() < MIN_PASSWORD_LENGTH) throw badRequest("Password must contain at least 8 characters");
        if (password.length() > MAX_PASSWORD_LENGTH) throw badRequest("Password must not exceed 72 characters");

        User user;
        if (identifier.contains("@")) {
            user = userRepository.findByEmail(identifier.toLowerCase(Locale.ROOT)).orElseThrow(this::invalidCredentials);
        } else if (identifier.matches("[0-9]{10,15}")) {
            user = userRepository.findByPhone(identifier).orElseThrow(this::invalidCredentials);
        } else {
            user = userRepository.findByUsername(identifier).orElseThrow(this::invalidCredentials);
        }
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) throw invalidCredentials();
        return user;
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private void validateUsername(String username) {
        if (username == null || username.isBlank()) throw badRequest("Username is required");
        if (username.length() < MIN_USERNAME_LENGTH) throw badRequest("Username must contain at least 3 characters");
        if (username.length() > MAX_USERNAME_LENGTH) throw badRequest("Username must not exceed 50 characters");
        if (!USERNAME_PATTERN.matcher(username).matches()) throw badRequest("Username may contain only letters, numbers, dot, underscore, and hyphen");
    }

    private void validatePassword(String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) throw badRequest("Password must contain at least 8 characters");
        if (password.length() > MAX_PASSWORD_LENGTH) throw badRequest("Password must not exceed 72 characters");
        if (!password.matches(".*[A-Z].*")) throw badRequest("Password must contain at least one uppercase letter");
        if (!password.matches(".*[a-z].*")) throw badRequest("Password must contain at least one lowercase letter");
        if (!password.matches(".*[0-9].*")) throw badRequest("Password must contain at least one number");
        if (!password.matches(".*[^A-Za-z0-9].*")) throw badRequest("Password must contain at least one special character");
    }

    private String value(Map<String, String> data, String key) { return rawValue(data, key).trim(); }
    private String rawValue(Map<String, String> data, String key) { String result = data.get(key); return result == null ? "" : result; }
    private ResponseStatusException invalidCredentials() { return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username/email/phone or password"); }
    private ResponseStatusException badRequest(String message) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, message); }
    private ResponseStatusException conflict(String message) { return new ResponseStatusException(HttpStatus.CONFLICT, message); }
}
