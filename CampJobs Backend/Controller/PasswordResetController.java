package com.example.campsjobs.Controller;

import com.example.campsjobs.DTO.ResetPasswordRequest;
import com.example.campsjobs.DTO.ForgotPasswordRequest;
import com.example.campsjobs.Service.PasswordResetServiceInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/camps-jobs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PasswordResetController {
    private final PasswordResetServiceInterface passwordResetService;

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String response = passwordResetService.generateResetToken(request.getEmail());
            return ResponseEntity.ok(Map.of("message", response));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 6 characters long."));
        }

        try {
            String response = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of("message", response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Error resetting password: " + e.getMessage()));
        }
    }

    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");

        if (token == null || token.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token is required."));
        }

        try {
            boolean isValid = passwordResetService.validateToken(token);
            if (isValid) {
                return ResponseEntity.ok(Map.of("message", "Token is valid.")); // Return JSON
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid or expired token.")); // Return JSON
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error validating token: " + e.getMessage())); // Return JSON
        }
    }
}
