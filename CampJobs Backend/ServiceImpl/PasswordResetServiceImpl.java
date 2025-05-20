package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.Entity.PasswordResetToken;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.PasswordResetTokenRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.PasswordResetServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetServiceInterface {
    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String generateResetToken(String email) {
        Optional<Users> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new RuntimeException("Email not registered!");
        }

        // Generate token
        String token = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // Expires in 1 hour

        // Check if a token already exists for the email
        Optional<PasswordResetToken> existingToken = tokenRepository.findByEmail(email);

        PasswordResetToken resetToken;
        if (existingToken.isPresent()) {
            // Update the existing token
            resetToken = existingToken.get();
            resetToken.setToken(token);
            resetToken.setExpiryDate(expiryDate);
        } else {
            // Create a new token
            resetToken = new PasswordResetToken();
            resetToken.setEmail(email);
            resetToken.setToken(token);
            resetToken.setExpiryDate(expiryDate);
        }

        // Save the token
        tokenRepository.save(resetToken);

        // Send reset link via email
        sendResetEmail(email, token);

        return "Reset link has been sent to your email.";
    }

    @Override
    public String resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid or expired token!"));

        if (resetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired!");
        }

        Users user = userRepository.findByEmail(resetToken.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.delete(resetToken);

        return "Password reset successful.";
    }

    @Override
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByToken(token);

        if (resetToken.isPresent() && !resetToken.get().getExpiryDate().isBefore(LocalDateTime.now())) {
            return true; // Token is valid
        } else {
            return false; // Token is invalid or expired
        }
    }

    private void sendResetEmail(String email, String token) {
        String resetLink = "token= " + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Request");
        message.setText("Copy the " + resetLink);
        mailSender.send(message);
    }
}
