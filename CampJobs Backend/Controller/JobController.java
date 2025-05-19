package com.example.campsjobs.Controller;

import com.example.campsjobs.Configs.JwtUtil;
import com.example.campsjobs.DTO.JobDTO;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.Job;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.EmployerRepository;
import com.example.campsjobs.Repository.JobRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.JobInterface;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SignatureException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/job")
@CrossOrigin(origins = "*")
@Slf4j
public class JobController {

    private final JobInterface jobService;
    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public JobController(JobInterface jobService, JobRepository jobRepository, EmployerRepository employerRepository, JwtUtil jwtUtil, UserRepository userRepository) {
        this.jobService = jobService;
        this.jobRepository = jobRepository;
        this.employerRepository = employerRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

//    @PostMapping("/save")
//    public ResponseEntity<?> saveJob(@RequestBody JobDTO jobDTO) {
//        try {
//            ResponseEntity<Long> saveJob = jobService.saveJob(jobDTO);
//            if (saveJob == null) {
//                return ResponseEntity.status(500).body("Error: Job was not saved (returned null)");
//            }
//            return ResponseEntity.ok(saveJob);
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body("Error saving Job: " + e.getMessage());
//        }
//    }

    @PostMapping("/post-job")
    public ResponseEntity<Map<String, String>> postJob(@RequestBody JobDTO jobDTO,
                                                       @RequestHeader("Authorization") String token) {
        try {
            // Extract email from JWT token
            String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));

            // Find employer by user's email
            Employer employer = employerRepository.findByUser_Email(email)
                    .orElseThrow(() -> new RuntimeException("Employer not found for email: " + email));

            // Create a new job and associate it with employer
            Job newJob = new Job();
            newJob.setJobTitle(jobDTO.getJobTitle());
            newJob.setJobType(jobDTO.getJobType());
            newJob.setJobLocation(jobDTO.getJobLocation());
            newJob.setJobSalary(jobDTO.getJobSalary());
            newJob.setExpiryDate(jobDTO.getExpiryDate());
            newJob.setJobDesc(jobDTO.getJobDesc().trim());
            newJob.setJobReqs(jobDTO.getJobReqs());

            // Ensure jobStatus is always "NotApproved"
            newJob.setJobStatus("NotApproved");

            // Ensure postedAt is set correctly
            newJob.setPostedAt(LocalDateTime.now());

            newJob.setEmployer(employer);

            jobRepository.save(newJob);
            log.info("Received JobDTO: {}", jobDTO); // Debugging output
            // Log and return JSON response
            Map<String, String> response = Collections.singletonMap("message", "Job posted successfully");
            System.out.println("Response: " + response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while posting the job"));
        }
    }





//    @PostMapping("/post-job")
//    public ResponseEntity<String> postJob(@RequestBody JobDTO jobDTO,
//                                          @RequestHeader("Authorization") String token) {
//        // Extract email from JWT token
//        String email = jwtUtil.extractUsername(token.replace("Bearer ", ""));
//
//        // Find employer by user's email
//        Employer employer = employerRepository.findByUser_Email(email)
//                .orElseThrow(() -> new RuntimeException("Employer not found for email: " + email));
//
//        // Create a new job and associate it with employer
//        Job newJob = new Job();
//                newJob.setJobTitle(jobDTO.getJobTitle());
//                newJob.setJobType(jobDTO.getJobType());
//                newJob.setJobLocation(jobDTO.getJobLocation());
//                newJob.setJobSalary(jobDTO.getJobSalary());
//                newJob.setExpiryDate(jobDTO.getExpiryDate());
//                newJob.setJobDesc(jobDTO.getJobDesc());
//                newJob.setJobReqs(jobDTO.getJobReqs());
//                newJob.setJobStatus(jobDTO.getJobStatus());
//                newJob.setPostedAt(jobDTO.getPostedAt());
//                newJob.setEmployer(employer); // Set the Employer// Set Employer object
//
//        jobRepository.save(newJob);
//        return ResponseEntity.ok("Job posted successfully");
//    }


    //To get logged in employer's jobs
    @GetMapping("/my-jobs")
    public List<Job> getEmployerJobs(@RequestParam String email) {
        // Find the user by email
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Find the employer linked to the user
        Employer employer = employerRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Employer not found for user: " + email));

        // Fetch jobs posted by this employer
        return jobRepository.findByEmployerUserUserId(user.getUserId());
    }

//    @PostMapping("/post-job")
//    public ResponseEntity<?> postJob(@RequestBody JobDTO jobDTO, @RequestHeader("Authorization") String token) {
//        try {
//            // Extract user email and role from token
//            String userEmail = jwtUtil.extractUsername(token);
//            String role = jwtUtil.extractRole(token);
//
//            log.info("Extracted User Email: {}", userEmail);
//            log.info("Extracted User Role: {}", role);
//
//            // Ensure only employers can post jobs
//            if (!"EMPLOYER".equalsIgnoreCase(role)) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Only employers can post jobs");
//            }
//
//            // Fetch employer by email
//            Users employer = userRepository.findByEmail(userEmail)
//                    .orElseThrow(() -> new RuntimeException("Employer not found"));
//
//            log.info("Employer found: {} (ID: {})", employer.getEmail(), employer.getUserId());
//
//            // Save the job with employer's ID
//            return jobService.saveJobForEmployer(jobDTO, employer.getUserId());
//
//        } catch (ExpiredJwtException e) {
//            log.error("JWT Token expired: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token has expired, please log in again.");
//        } catch (SecurityException | MalformedJwtException e) {
//            log.error("Invalid JWT Token: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token.");
//        } catch (Exception e) {
//            log.error("Error posting job: {}", e.getMessage());
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token or user not found.");
//        }
//    }







//    @PostMapping("/post-job")
//    public ResponseEntity<?> postJob(@RequestBody JobDTO jobDTO, @RequestHeader("Authorization") String token) {
//        String userId = jwtUtil.extractUsername(token);
//
//        try {
//            Long userIdLong = Long.valueOf(userId);
//            return jobService.saveJobForEmployer(jobDTO, userIdLong);
//        } catch (NumberFormatException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format");
//        }
//    }



//    @PostMapping("/post-job")
//    public ResponseEntity<?> postJob(@RequestBody JobDTO jobDTO, @RequestHeader("Authorization") String token) {
//        // Extract userId from JWT token
//        String userId = jwtUtil.extractUsername(token);
//
//        // Use userRepository to find the user (Employer)
////        Optional<Users> user = userRepository.findById(Long.valueOf(userId));
////        if (user.isEmpty()) {
////            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
////        }
//        Optional<Users> user;
//        try {
//            Long userIdLong = Long.valueOf(userId);  // Convert the String to Long
//            user = userRepository.findById(userIdLong);
//            if (user.isEmpty()) {
//                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found");
//            }
//            // Proceed with user logic...
//        } catch (NumberFormatException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid user ID format");
//        }
//
//
//        // Check if the user is an employer
//        if (user.get().getRole().equalsIgnoreCase("EMPLOYER")) {
//            Long employerId = user.get().getUserId();  // Get the user ID
//
//            // Assuming you have an EmployerRepository to fetch the employer
//            Optional<Employer> employerOptional = employerRepository.findById(employerId);
//            if (employerOptional.isPresent()) {
//                Employer employer = employerOptional.get();
//
//                Job newJob = new Job();
//                newJob.setJobTitle(jobDTO.getJobTitle());
//                newJob.setJobType(jobDTO.getJobType());
//                newJob.setJobLocation(jobDTO.getJobLocation());
//                newJob.setJobSalary(jobDTO.getJobSalary());
//                newJob.setExpiryDate(jobDTO.getExpiryDate());
//                newJob.setJobDesc(jobDTO.getJobDesc());
//                newJob.setJobReqs(jobDTO.getJobReqs());
//                newJob.setJobStatus(jobDTO.getJobStatus());
//                newJob.setPostedAt(jobDTO.getPostedAt());
//                newJob.setEmployer(employer); // Set the Employer
//
//                jobRepository.save(newJob);
//                return ResponseEntity.ok().body("Job posted successfully");
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employer not found");
//            }
//        }
//
//        // If the user is not an employer
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an employer to post jobs");
//    }

    @GetMapping("/jobs-by-employer")
    public ResponseEntity<?> getJobsByEmployer(@RequestHeader("Authorization") String token) {
        Long employerId = Long.valueOf(jwtUtil.extractUsername(token));

        // Check if the logged-in user is an employer
        Optional<Users> employer = userRepository.findById(employerId);
        if (employer.isEmpty() || !employer.get().getRole().equalsIgnoreCase("EMPLOYER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an employer to view jobs");
        }

        // Get all jobs posted by the employer
        List<Job> jobs = jobRepository.findByEmployer_EmpId(employerId);

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/all-jobs")
//    public ResponseEntity<List<Job>> getAllJobs()
        public List<Job> getAllJobs() {
//        List<Job> jobs = jobService.getAllJobs();
//        return ResponseEntity.ok(jobs);
        return jobService.getAllJobsWithEmployers();
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<Job> getJob(@PathVariable Long id) {
//        Job jobDTO = jobService.getJobById(id);
//        if (jobDTO != null) {
//            return ResponseEntity.ok(jobDTO);
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }


    @GetMapping("/info/{jobId}")
    public ResponseEntity<?> getJobById(@PathVariable Long jobId) {
        Job job = jobService.getJobById(jobId);

        if (job == null) {
            return ResponseEntity.status(404).body("Job not found");
        }

        JobDTO jobDTO = new JobDTO(
                job.getJobId(),
                job.getJobTitle(),
                job.getJobType(),
                job.getJobLocation(),
                job.getJobSalary(),
                job.getJobDesc(),
                job.getJobReqs(),
                job.getJobStatus(),
                job.getPostedAt(),
                job.getExpiryDate()
        );

        return ResponseEntity.ok(Map.of(
                "jobId", jobDTO.getJobId(),
                "jobTitle", jobDTO.getJobTitle(),
                "jobType", jobDTO.getJobType(),
                "jobLocation", jobDTO.getJobLocation(),
                "jobSalary", jobDTO.getJobSalary(),
                "expiryDate", jobDTO.getFormattedExpiryDate(),
                "jobDesc", jobDTO.getJobDesc(),
                "jobReqs", jobDTO.getJobReqs(),
                "jobStatus", jobDTO.getJobStatus(),
                "postedAt", jobDTO.getFormattedPostedAt()
        ));
    }

    @PutMapping("/update-job-status/{jobId}")
    public ResponseEntity<String> updateJobStatus(@PathVariable Long jobId, @RequestBody Map<String, String> request) {
        String newStatus = request.get("jobStatus");
        jobService.updateJobStatus(jobId, newStatus);
        return ResponseEntity.ok("Job status updated successfully.");
    }

    // 1. Get application count for a job
    @GetMapping("/applications/count/{jobId}")
    public ResponseEntity<Integer> getApplicationCount(@PathVariable Long jobId) {
        int count = jobService.getApplicationCount(jobId);
        return ResponseEntity.ok(count);
    }

    // 2. Get job details
    @GetMapping("/details/{jobId}")
    public ResponseEntity<JobDTO> getJobDetails(@PathVariable Long jobId) {
        Optional<JobDTO> job = jobService.getJobDetails(jobId);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Update job details
    @PutMapping("/update/{jobId}")
    public ResponseEntity<Job> updateJob(@PathVariable Long jobId, @RequestBody JobDTO jobDTO) {
        Job updatedJob = jobService.updateJob(jobId, jobDTO);
        return ResponseEntity.ok(updatedJob);
    }

    // Endpoint to delete a job by employer
    @DeleteMapping("/delete/{jobId}/employer-by/{empId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId, @PathVariable Long empId) {
        jobService.deleteJobByEmployer(jobId, empId);
        return ResponseEntity.ok("Job deleted successfully.");
    }

    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJobById(jobId);
        return ResponseEntity.ok("Job deleted successfully.");
    }

}
