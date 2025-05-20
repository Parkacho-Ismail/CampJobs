package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.DTO.RegisterDTO;
import com.example.campsjobs.DTO.UniversalResponse;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.*;
import com.example.campsjobs.Service.EmailInterface;
import com.example.campsjobs.Service.UserServiceInterface;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserServiceInterface {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmployerRepository employerRepository;
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final EmailInterface emailService; //Use the correct interface

    @Override
    public ResponseEntity<UniversalResponse> register(RegisterDTO registerDTO) {
        Optional<Users> optionalUser = userRepository.findByEmail(registerDTO.getEmail());

        if (optionalUser.isPresent()) {
            return ResponseEntity.ok(
                    UniversalResponse.builder()
                            .status("01")
                            .message("User already exists... please login")
                            .data(null)
                            .build()
            );
        }

        // Hash password before storing it
        String hashedPassword = passwordEncoder.encode(registerDTO.getPassword());

        Users newUser = Users.builder()
                .fullName(registerDTO.getFullName())
                .email(registerDTO.getEmail())
                .role(registerDTO.getRole())
                .password(hashedPassword)
                .createdAt(LocalDateTime.now())
                .build();

        Users savedUser = userRepository.save(newUser);

        // Send Registration Email
        try {
            String subject = "Welcome to CampJobs!";
            String body = "Hello " + savedUser.getFullName() + ",\n\n" +
                    "Thank you for registering with CampJobs. We are excited to have you on board!\n\n" +
                    "Best regards,\nCampJobs Team";
            emailService.sendEmail(savedUser.getEmail(), subject, body);
            log.info("Registration email sent successfully to {}", savedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send registration email to {}: {}", savedUser.getEmail(), e.getMessage());
        }

        UniversalResponse response = UniversalResponse.builder()
                .status("00")
                .message("Registered Successfully... please login")
                .data(Map.of("userId", savedUser.getUserId()))
                .build();

        return ResponseEntity.ok().body(response);
    }

    @Override
    public ResponseEntity<UniversalResponse> getAllUsers() {
        List<Users> users = userRepository.findAll();
        UniversalResponse response = UniversalResponse.builder()
                .status("00")
                .message("List of users found")
                .data(users)
                .build();
        return ResponseEntity.ok().body(response);
    }

    @Override
    public Users getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean authenticateUser(String email, String password) {
        Optional<Users> userOpt = userRepository.findByEmail(email);

        if (userOpt.isPresent()) {
            String storedHash = userOpt.get().getPassword(); // Hashed password from DB
            boolean matches = passwordEncoder.matches(password, storedHash); // Compare hashed password

            log.info("Authentication Result: {}", matches);

            return matches;
        }

        return false;
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        //  Delete applications related to the job seeker
        Optional<JobSeeker> jobSeekerOpt = jobSeekerRepository.findByUser_UserId(id);
        jobSeekerOpt.ifPresent(jobSeeker -> {
            applicationRepository.deleteByJobSeeker_UserId(id);
            jobSeekerRepository.delete(jobSeeker);
        });

        // Delete employer and related jobs if the user is an employer
        Optional<Employer> employerOpt = employerRepository.findByUser_UserId(id);
        if (employerOpt.isPresent()) {
            Employer employer = employerOpt.get();

            employer.getJobs().forEach(job -> {
                // Delete job categories related to jobs
//                jobCategoryRepository.deleteByJob_JobId(job.getJobId());

                // Delete job applications related to jobs
                applicationRepository.deleteByJob_JobId(job.getJobId());
            });

            // Delete jobs
            jobRepository.deleteAll(employer.getJobs());

            // Delete employer
            employerRepository.delete(employer);
        }

        // Finally, delete the user
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User not found with ID: " + id);
        }
    }

    @Override
    public Optional<String> getUserEmailById(Long userId) {
        return userRepository.findByUserId(userId).map(Users::getEmail);
    }

    @Override
    public Optional<String> getUserFullNameById(Long userId) {
        return userRepository.findByUserId(userId).map(Users::getFullName);
    }


}
