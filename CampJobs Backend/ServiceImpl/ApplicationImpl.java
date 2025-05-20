package com.example.campsjobs.ServiceImpl;

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
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ApplicationImpl implements ApplicationInterface {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final JobSeekerRepository jobSeekerRepository;
    private final EmailInterface emailService;


    private static final String UPLOAD_DIR = "uploads/";

    public ApplicationImpl(ApplicationRepository applicationRepository,
                           JobRepository jobRepository, UserRepository userRepository, JobSeekerRepository jobSeekerRepository, EmailInterface emailService) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.emailService = emailService;
    }

    @Override
    public ResponseEntity<Long> saveApplication(ApplicationDTO applicationDTO) {
        return null;
    }

    @Override
    public List<Application> getAllApplications() {
        return null;
    }

    @Override
    public Application getApplicationById(Long id) {
        return null;
    }

    @Override
    public Application applyForJob(Long jobId, Long seekerId,
                                   MultipartFile resumeImg, MultipartFile letterImg,
                                   MultipartFile certImg, MultipartFile idImg,
                                   LocalDateTime appliedAt) {

        // Validate job existence
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found for ID: " + jobId));

        // Validate job seeker existence
        Users seeker = userRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("Job seeker not found for ID: " + seekerId));

        String seekerEmail = seeker.getEmail(); // Fetch email directly

        // Check if job application already exists
        if (applicationRepository.existsByJob_JobIdAndJobSeeker_UserId(jobId, seekerId)) {
            throw new RuntimeException("You have already applied for this job.");
        }

        // Create new job application
        Application application = buildApplication(job, seeker, resumeImg, letterImg, certImg, idImg, appliedAt);

        // Save application & send email
        applicationRepository.save(application);
        sendApplicationConfirmationEmail(seekerEmail, job);

        return application;
    }

    private Application buildApplication(Job job, Users seeker,
                                         MultipartFile resumeImg, MultipartFile letterImg,
                                         MultipartFile certImg, MultipartFile idImg,
                                         LocalDateTime appliedAt) {
        Application application = new Application();
        application.setJob(job);
        application.setJobSeeker(seeker);
        application.setAppliedAt(appliedAt);

        try {
            application.setResumeImg(saveFile(resumeImg));
            application.setLetterImg(saveFile(letterImg));
            application.setCertImg(saveFile(certImg));
            application.setIdImg(saveFile(idImg));
        } catch (IOException e) {
            throw new RuntimeException("File upload failed", e);
        }

        return application;
    }

    private void sendApplicationConfirmationEmail(String to, Job job) {
        System.out.println("📧 Sending email to: " + to); 
        String subject = "Job Application Submitted Successfully";
        String body = String.format(
                "Dear Job Seeker,\n\n" +
                        "You have successfully applied for the job: '%s'.\n\n" +
                        "We will notify you once there is an update on your application status.\n\n" +
                        "Thank you for using CampJobs.\n\nBest Regards,\nCampJobs Team",
                job.getJobTitle()
        );

        emailService.sendEmail(to, subject, body);
    }

    @Override
    public List<ApplicationDTO> getApplicationsBySeeker(Long seekerId) {
        return null;
    }

    //Each jobseeker to see their applications
    @Override
    public List<Application> getApplicationsByJobSeeker(String email) {
        List<Application> applications = applicationRepository.findByJobSeekerEmail(email);
        log.info("Applications found for {}: {}", email, applications.size());
        return applications;
    }

    @Override
    public List<Application> getApplicationsByEmployer(String email) {
        Users employerUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Employer not found"));

        List<Job> employerJobs = jobRepository.findByEmployer_User(employerUser);

        if (employerJobs.isEmpty()) {
            throw new RuntimeException("No jobs found for this employer");
        }

        List<Application> applications = applicationRepository.findByJobIn(employerJobs);

        applications.forEach(application -> {
            application.getJob().setJobTitle(application.getJob().getJobTitle());
            application.getJobSeeker().setFullName(application.getJobSeeker().getFullName());
        });

        log.info("Applications found for employer {}: {}", email, applications.size());
        return applications;
    }

    @Override
    public List<Application> getApplicationsByEmployer(Long employerId) {
        List<Job> jobs = jobRepository.findByEmployer_EmpId(employerId); // Get jobs posted by employer
        return jobs.stream()
                .flatMap(job -> applicationRepository.findByJob_JobId(job.getJobId()).stream())
                .collect(Collectors.toList());
    }

    @Override
    public void updateApplicationStatus(Long appId, String newStatus, String emailBody) {
        Application application = applicationRepository.findById(appId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        try {
            // Convert the String to ApplicationStatus enum
            Application.ApplicationStatus status = Application.ApplicationStatus.valueOf(newStatus.toUpperCase());
            application.setAppStatus(status);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid application status: " + newStatus);
        }

        applicationRepository.save(application);

        // Send email with custom message from frontend
        sendApplicationStatusUpdateEmail(application, emailBody);
    }

    private void sendApplicationStatusUpdateEmail(Application application, String emailBody) {
        String email = application.getJobSeeker().getEmail();
        String subject = "Application Status Update - " + application.getJob().getJobTitle();

        emailService.sendEmail(email, subject, emailBody); //Use email body from frontend
    }

    //seeker applications
    @Override
    public List<ApplicationDTO> getApplicationsBySeekerId(Long seekerId) {
        List<Application> applications = applicationRepository.findByJobSeekerUserId(seekerId);

        return applications.stream().map(application -> new ApplicationDTO(
                application.getAppId(),
                application.getJob().getJobTitle(),
                application.getJob().getEmployer().getEmpName(),
                application.getJobSeeker().getFullName(),
                application.getResumeImg(),
                application.getLetterImg(),
                application.getCertImg(),
                application.getIdImg(),
                application.getAppliedAt(),
                application.getAppStatus().name()
        )).collect(Collectors.toList());
    }

    @Override
    public void deleteApplication(Long appId) {
        if (!applicationRepository.existsByAppId(appId)) {
            throw new RuntimeException("Application not found with ID: " + appId);
        }
        applicationRepository.deleteByAppId(appId);
    }

    @Override
    public String getJobSeekerEmailByAppId(Long appId) {
        return applicationRepository.findJobSeekerEmailByAppId(appId);
    }

    private String uploadFile(String directory, MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) return null;

        Path uploadPath = Paths.get(directory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filePath = directory + file.getOriginalFilename();
        Files.copy(file.getInputStream(), Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);
        return filePath;
    }

    private String saveFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        String filePath = UPLOAD_DIR + file.getOriginalFilename();
        Files.write(Paths.get(filePath), file.getBytes());
        return filePath;
    }
}
