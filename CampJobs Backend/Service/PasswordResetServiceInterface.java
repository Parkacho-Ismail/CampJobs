package com.example.campsjobs.Service;

public interface PasswordResetServiceInterface {
    String generateResetToken(String email);
    String resetPassword(String token, String newPassword);
    boolean validateToken(String token); 

}

