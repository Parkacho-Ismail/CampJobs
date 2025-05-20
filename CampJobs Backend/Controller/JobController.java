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
            log.info("Received JobDTO: {}", jobDTO); 
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

    // Get application count for a job
    @GetMapping("/applications/count/{jobId}")
    public ResponseEntity<Integer> getApplicationCount(@PathVariable Long jobId) {
        int count = jobService.getApplicationCount(jobId);
        return ResponseEntity.ok(count);
    }

    // Get job details
    @GetMapping("/details/{jobId}")
    public ResponseEntity<JobDTO> getJobDetails(@PathVariable Long jobId) {
        Optional<JobDTO> job = jobService.getJobDetails(jobId);
        return job.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Update job details
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

    //Endpoint to delete job using jobId
    @DeleteMapping("/delete/{jobId}")
    public ResponseEntity<String> deleteJob(@PathVariable Long jobId) {
        jobService.deleteJobById(jobId);
        return ResponseEntity.ok("Job deleted successfully.");
    }

}
