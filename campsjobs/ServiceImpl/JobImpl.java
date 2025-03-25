package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.DTO.JobDTO;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.Job;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.ApplicationRepository;
import com.example.campsjobs.Repository.EmployerRepository;
import com.example.campsjobs.Repository.JobRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.EmailInterface;
import com.example.campsjobs.Service.JobInterface;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class JobImpl implements JobInterface {

    private final JobRepository jobRepository;
    private final EmployerRepository employerRepository; // ✅ Mark as final
    private UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final EmailInterface emailService;

    public JobImpl(JobRepository jobRepository, EmployerRepository employerRepository, UserRepository userRepository, ApplicationRepository applicationRepository, EmailInterface emailService) {
        this.jobRepository = jobRepository;
        this.employerRepository = employerRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.emailService = emailService;
    }

    @Override
    public ResponseEntity<Long> saveJob(JobDTO jobDTO) {
//        Employer employer = employerRepository.findById(jobDTO.getEmpId())
//                .orElseThrow(() -> new RuntimeException("Employer not found"));
//
//        Job job = new Job();
//        job.setJobTitle(jobDTO.getJobTitle());
//        job.setJobType(jobDTO.getJobType());
//        job.setJobLocation(jobDTO.getJobLocation());
//        job.setJobSalary(jobDTO.getJobSalary());
//        job.setJobDesc(jobDTO.getJobDesc());
//        job.setJobReqs(jobDTO.getJobReqs());
//        job.setExpiryDate(jobDTO.getExpiryDate());
//        job.setPostedAt(jobDTO.getPostedAt());
//        job.setJobStatus(jobDTO.getJobStatus());
//        job.setEmployer(employer); // ✅ Correctly set employer
//
//        Job savedJob = jobRepository.save(job);
//        return ResponseEntity.ok(savedJob.getJobId());
        return  null;//to be deleted
    }

    @Override
    public List<Job> getAllJobs() {
        return jobRepository.findAll();
    }

    @Override
//    public Job getJobById(Long id) {
//        return jobRepository.findById(id).orElse(null);
    //    }
    public Job getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .orElse(null); // Return null if job is not found
    }


    @Override
    public ResponseEntity<?> saveJobForEmployer(JobDTO jobDTO, Long userId) {
        Optional<Users> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty() || !userOptional.get().getRole().equalsIgnoreCase("EMPLOYER")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You must be an employer to post jobs");
        }

        Optional<Employer> employerOptional = employerRepository.findById(userId);
        if (employerOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Employer not found");
        }

        Employer employer = employerOptional.get();

        Job job = new Job();
        job.setJobTitle(jobDTO.getJobTitle());
        job.setJobType(jobDTO.getJobType());
        job.setJobLocation(jobDTO.getJobLocation());
        job.setJobSalary(jobDTO.getJobSalary());
        job.setJobDesc(jobDTO.getJobDesc());
        job.setJobReqs(jobDTO.getJobReqs());
        job.setExpiryDate(jobDTO.getExpiryDate());
        job.setPostedAt(jobDTO.getPostedAt());
        job.setJobStatus(jobDTO.getJobStatus());
        job.setEmployer(employer);

        jobRepository.save(job);
        return ResponseEntity.ok("Job posted successfully");
    }

    @Override
    public List<Job> getAllJobsWithEmployers() {
        return jobRepository.findAllWithEmployers();
    }

    @Override
    @Transactional
    public void deleteJobByEmployer(Long jobId, Long empId) {
        int deletedCount = jobRepository.deleteByJobIdAndEmployer_EmpId(jobId, empId);

        if (deletedCount == 0) {
            throw new RuntimeException("Job not found or unauthorized to delete.");
        }
    }

    @Override
    @Transactional
    public void deleteJobById(Long jobId) {
        applicationRepository.deleteByJobId(jobId); // First delete related applications
        jobRepository.deleteById(jobId); // Then delete the job
    }

    @Override
    public void updateJobStatus(Long jobId, String jobStatus) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        job.setJobStatus(jobStatus);
        jobRepository.save(job);

        // Fetch employer email from the associated User entity
        String employerEmail = job.getEmployer().getUser().getEmail();
        if (employerEmail != null && !employerEmail.isEmpty()) {
            sendJobStatusEmail(employerEmail, job);
        }
    }
//associated with the method above it
    private void sendJobStatusEmail(String to, Job job) {
        String subject = "Job Status Update: " + job.getJobTitle();
        String body = "Dear Employer,\n\n" +
                "Your job posting '" + job.getJobTitle() + "' has been updated to: " + job.getJobStatus() + ".\n\n" +
                "Thank you for using CampJobs.\n\nBest Regards,\nCampJobs Team";

        emailService.sendEmail(to, subject, body);
    }

//    @Transactional
//    @Override
//    public void updateJobStatus(Long jobId, String jobStatus) {
//        Job job = jobRepository.findById(jobId).orElseThrow(() -> new RuntimeException("Job not found"));
//        job.setJobStatus(jobStatus);
//        jobRepository.save(job);
//    }

    @Override
    public int getApplicationCount(Long jobId) {
        return jobRepository.countApplicationsByJobId(jobId);
    }

    @Override
    public Optional<JobDTO> getJobDetails(Long jobId) {
        return jobRepository.findById(jobId).map(job -> new JobDTO(
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
        ));
    }

    @Override
    public Job updateJob(Long jobId, JobDTO jobDTO) {
        return jobRepository.findById(jobId).map(job -> {
            job.setJobTitle(jobDTO.getJobTitle());
            job.setJobType(jobDTO.getJobType());
            job.setJobLocation(jobDTO.getJobLocation());
            job.setJobSalary(jobDTO.getJobSalary());
            job.setJobDesc(jobDTO.getJobDesc());
            job.setJobReqs(jobDTO.getJobReqs());
//            job.setJobStatus(jobDTO.getJobStatus());
            job.setExpiryDate(jobDTO.getExpiryDate());
            return jobRepository.save(job);
        }).orElseThrow(() -> new RuntimeException("Job not found with ID: " + jobId));
    }


    @Override
    @Transactional
    public void updateExpiredJobs() {
        jobRepository.markExpiredJobs();
        System.out.println("Expired jobs updated successfully.");
    }

    // Run this task every midnight to mark expired jobs
    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduleExpiredJobsUpdate() {
        updateExpiredJobs();
    }
}


