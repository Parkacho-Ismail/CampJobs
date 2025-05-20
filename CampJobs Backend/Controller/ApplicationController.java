package com.example.campsjobs.Controller;

import com.example.campsjobs.Configs.JwtUtil;
import com.example.campsjobs.DTO.ApplicationDTO;
import com.example.campsjobs.Entity.Application;
import com.example.campsjobs.Entity.Job;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.ApplicationRepository;
import com.example.campsjobs.Repository.JobRepository;
import com.example.campsjobs.Repository.JobSeekerRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.ApplicationInterface;
import com.example.campsjobs.Service.EmailInterface;
import com.example.campsjobs.ServiceImpl.ApplicationImpl;
import org.apache.catalina.User;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/application")
@CrossOrigin(origins = "*")
public class ApplicationController {
    private static final String UPLOAD_DIR = "uploads";
    private final ApplicationImpl applicationService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private ApplicationRepository applicationRepository;
    private JobSeekerRepository jobSeekerRepository;
    private final ApplicationInterface applicationInterface;
    private final EmailInterface emailService;

    public ApplicationController(ApplicationImpl applicationService, JwtUtil jwtUtil, UserRepository userRepository, JobRepository jobRepository, ApplicationRepository applicationRepository, JobSeekerRepository jobSeekerRepository, ApplicationInterface applicationInterface, EmailInterface emailService) {
        this.applicationService = applicationService;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.applicationInterface = applicationInterface;
        this.emailService = emailService;
    }

    @GetMapping("/check/{jobId}/{userId}")
    public ResponseEntity<Map<String, Boolean>> checkIfApplied(@PathVariable Long jobId, @PathVariable Long userId) {
        boolean applied = applicationRepository.existsByJobAndJobSeeker(jobRepository.findById(jobId).orElse(null),
                userRepository.findById(userId).orElse(null));
        return ResponseEntity.ok(Collections.singletonMap("applied", applied));
    }

    @PostMapping(value = "/apply/{jobId}/{userId}", consumes = {"multipart/form-data"})
    public ResponseEntity<Map<String, String>> applyForJob(
            @PathVariable Long jobId,
            @PathVariable Long userId,
            @RequestParam("resumeImg") MultipartFile resumeImg,
            @RequestParam("letterImg") MultipartFile letterImg,
            @RequestParam("certImg") MultipartFile certImg,
            @RequestParam("idImg") MultipartFile idImg) {

        // Fetch seeker_id from jobseeker table using userId
        Long seekerId = jobSeekerRepository.findSeekerIdByUserId(userId);
        if (seekerId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Invalid user ID."));
        }

        try {
            // Ensure `applicationService.applyForJob()` returns the saved application
            Application application = applicationService.applyForJob(jobId, seekerId, resumeImg, letterImg, certImg, idImg, LocalDateTime.now());

            if (application == null || application.getJob() == null) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Application saved but job data is missing."));
            }

            // Get the job object from the application
            Job job = application.getJob();

            // Fetch the user's email
            Optional<Users> user = userRepository.findById(userId);
            if (user.isPresent()) {
                String email = user.get().getEmail();

                // Ensure email function is called with job title
                System.out.println("📧 Sending email to: " + email);
                emailService.sendEmail(email, "Job Application Submitted",
                        String.format("Dear Job Seeker,\n\n" +
                                        "You have successfully applied for the job: '%s'.\n\n" +
                                        "We will notify you once there is an update on your application status.\n\n" +
                                        "Thank you for using CampJobs.\n\nBest Regards,\nCampJobs Team",
                                job.getJobTitle())); 
                System.out.println("Email sent successfully!");
            } else {
                System.err.println("User not found for email sending.");
            }

            return ResponseEntity.ok(Collections.singletonMap("message", "Application submitted successfully!"));
        } catch (Exception e) {
            e.printStackTrace(); // Print the full error in logs
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Failed to apply for job: " + e.getMessage()));
        }
    }

//for downloading images to front end
@GetMapping("/download/{filename:.+}")
public ResponseEntity<byte[]> downloadFile(@PathVariable String filename) {
    try {
        // Properly decode the filename to handle spaces and special characters
        filename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

        // Ensure filename is sanitized (Prevent directory traversal)
        Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();

        // Check if file exists and is readable
        if (!Files.exists(filePath) || !Files.isReadable(filePath)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("File not found: " + filename).getBytes());
        }

        byte[] fileBytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(fileBytes);

    } catch (IOException e) {
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error loading file: " + filename).getBytes());
    }
}
    @GetMapping("/employer-applications")
    public ResponseEntity<List<Application>> getEmployerApplications(@AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername(); // Extract email from authenticated user
        List<Application> applications = applicationInterface.getApplicationsByEmployer(userEmail);
        return ResponseEntity.ok(applications);
    }


    @PostMapping("/updateStatus/{appId}")
    public ResponseEntity<Map<String, String>> updateApplicationStatus(
            @PathVariable Long appId,
            @RequestBody Map<String, String> statusUpdate) {

        String newStatus = statusUpdate.get("status");
        String emailBody = statusUpdate.get("emailBody"); // Get email body from frontend

        try {
            applicationInterface.updateApplicationStatus(appId, newStatus, emailBody);
            return ResponseEntity.ok(Collections.singletonMap("message", "Application status updated and email sent!"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "Failed to update status: " + e.getMessage()));
        }
    }

    @GetMapping("/my-applications")
    public ResponseEntity<List<Application>> getJobSeekerApplications(@AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername(); // Get the logged-in user's email
        List<Application> applications = applicationService.getApplicationsByJobSeeker(email);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/all-applications")
    public ResponseEntity<List<Application>> getAllApplications() {
        List<Application> applications = applicationService.getAllApplications();
        return ResponseEntity.ok(applications);
    }

    //seeker applications
    @GetMapping("/seeker/{seekerId}")
    public ResponseEntity<List<ApplicationDTO>> getApplicationsBySeeker(@PathVariable Long seekerId) {
        List<ApplicationDTO> applications = applicationInterface.getApplicationsBySeekerId(seekerId);
        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Application> getApplication(@PathVariable Long id) {
        Application applicationDTO = applicationService.getApplicationById(id);
        if (applicationDTO != null) {
            return ResponseEntity.ok(applicationDTO);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/seeker/delete/{appId}")
    public ResponseEntity<String> deleteApplication(@PathVariable Long appId) {
        applicationService.deleteApplication(appId);
        return ResponseEntity.ok("Application deleted successfully");
    }

    @GetMapping("/job-seeker-email/{appId}")
    public ResponseEntity<String> getJobSeekerEmail(@PathVariable Long appId) {
        String email = applicationService.getJobSeekerEmailByAppId(appId);
        if (email != null) {
            return ResponseEntity.ok(email);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Job seeker email not found");
        }
    }
}
