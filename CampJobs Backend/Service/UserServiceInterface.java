package com.example.campsjobs.Service;

import com.example.campsjobs.DTO.RegisterDTO;
import com.example.campsjobs.DTO.UniversalResponse;
import com.example.campsjobs.Entity.Users;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface UserServiceInterface {

    ResponseEntity<UniversalResponse> register(RegisterDTO registerDTO);

    ResponseEntity<UniversalResponse> getAllUsers();

    Users getUserById(Long id);

    Users getUserByEmail(String email);

    boolean authenticateUser(String email, String password);

    void deleteUser(Long id);

    Optional<String> getUserEmailById(Long userId);

    Optional<String> getUserFullNameById(Long userId); // New method

}
