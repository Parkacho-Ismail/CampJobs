package com.example.campsjobs.Controller;

import com.example.campsjobs.Configs.JwtUtil;
import com.example.campsjobs.DTO.LoginDTO;
import com.example.campsjobs.DTO.RegisterDTO;
import com.example.campsjobs.DTO.UniversalResponse;
import com.example.campsjobs.Entity.Application;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.ApplicationRepository;
import com.example.campsjobs.Repository.JobSeekerRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.UserServiceInterface;
import com.example.campsjobs.ServiceImpl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/api/v1/camps-jobs")
@RestController
@Slf4j
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {
    private final UserServiceInterface userServiceInterface;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserServiceImpl userService;
    private final ApplicationRepository applicationRepository;
    private final JobSeekerRepository jobSeekerRepository;

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
        Optional<Users> optionalUser = userRepository.findByEmail(loginDTO.getEmail());

        // Check if the user exists
        if (optionalUser.isEmpty()) {
            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
                    .message("Login failed..user not found").build());
        }

        Users user = optionalUser.get();

        // Check if password matches
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
                    .message("Login failed..Wrong Credentials").build());
        }

        // Check if the role matches
        if (!user.getRole().equalsIgnoreCase(loginDTO.getRole())) {
            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
                    .message("Login failed..Incorrect Role").build());
        }

        // Generate JWT token if authentication is successful
//        String token = jwtUtil.generateToken(loginDTO.getEmail());
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), Math.toIntExact(user.getUserId()));


        // Build the response
        return ResponseEntity.ok().body(UniversalResponse.builder().status("00")
                .message("Login Successful")
                .data(Map.of(
                        "token", token,
                        "role", user.getRole(),
                        "email", user.getEmail(),
                        "userId", user.getUserId()  // Include userId inside the data object
                ))
                .build());
    }




//    @PostMapping("/login")
//    public ResponseEntity<?> loginUser(@RequestBody LoginDTO loginDTO) {
//        Optional<Users> optionalUser = userRepository.findByEmail(loginDTO.getEmail());
//
//        if (optionalUser.isEmpty()) {
//            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
//                    .message("Login failed..user not found").build());
//        }
//
//        Users user = optionalUser.get();
//
//        // Check if password matches
//        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
//            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
//                    .message("Login failed..Wrong Credentials").build());
//        }
//
//        // Check if the role matches
//        if (!user.getRole().equalsIgnoreCase(loginDTO.getRole())) {
//            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
//                    .message("Login failed..Incorrect Role").build());
//        }
//
//        // Generate token if authentication is successful
//        String token = jwtUtil.generateToken(loginDTO.getEmail());
//
//        return ResponseEntity.ok().body(UniversalResponse.builder().status("00")
//                .message("Login Successful")
//                .data(Map.of("token", token, "role", user.getRole(), "email", user.getEmail()))
//                .build());
//    }


//    @PostMapping("/login")
//
//        public ResponseEntity<?> loginUser (@RequestBody LoginDTO loginDTO){
//
//        Optional<Users> optionalUser = userRepository.findByEmail(loginDTO.getEmail());
//        if (optionalUser.isEmpty()){
//            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
//                    .message("Login failed..user not found").build());
//        }
//        if (!passwordEncoder.matches(loginDTO.getPassword(),optionalUser.get().getPassword())){
//            return ResponseEntity.ok().body(UniversalResponse.builder().status("01")
//                    .message("Login failed..Wrong Credentials").build());
//        }
//        String token = jwtUtil.generateToken(loginDTO.getEmail());
//        return ResponseEntity.ok().body(UniversalResponse.builder().status("00")
//                .message("Login Succesfull").data(token).build());
//        }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDTO registerDTO) {
        log.info("Register request from client: {}", registerDTO.toString());

        // Call the service and get the response
        ResponseEntity<UniversalResponse> response = userService.register(registerDTO);

        // Extract response body
        UniversalResponse responseBody = response.getBody();

        // ✅ Ensure JSON Response
        if (responseBody != null && responseBody.getData() instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) responseBody.getData();
            if (dataMap.containsKey("userId")) {
                return ResponseEntity.ok().body(Map.of("userId", dataMap.get("userId")));
            }
        }

        // ✅ Return JSON instead of plain text
        log.info("Incoming registration request: {}", registerDTO);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "User registration failed."));
    }




    @GetMapping("/get-all-users")
        public ResponseEntity<?> getAllUsers () {
            return userServiceInterface.getAllUsers();
        }
    @CrossOrigin(origins = "http://127.0.0.1:5500")
    @DeleteMapping("/delete-user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable("id") Long id) {
        try {
            // Call the service method to delete the user
            userServiceInterface.deleteUser(id);
            return ResponseEntity.ok().body("User deleted successfully");
        } catch (Exception e) {
            // Handle any errors or exceptions that occur during deletion
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error deleting user");
        }
    }

    @GetMapping("/email")
    public Optional<String> getUserEmail(@RequestParam Long userId) {
        return userService.getUserEmailById(userId);
    }


    @GetMapping("/fullname") // New endpoint
    public Optional<String> getUserFullName(@RequestParam Long userId) {
        return userService.getUserFullNameById(userId);
    }

}

