package com.example.campsjobs.Controller;

import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.*;
import com.example.campsjobs.ServiceImpl.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
@CrossOrigin("*")
public class StatisticsController {

    @Autowired
    private EmployerRepository employerRepository;

    @Autowired
    private JobSeekerRepository jobSeekerRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StatisticsService statisticsService;

    private final SimpMessagingTemplate messagingTemplate;

    private final ApplicationRepository applicationRepository;

    public StatisticsController(EmployerRepository employerRepository, JobSeekerRepository jobSeekerRepository, ApplicationRepository applicationRepository, JobRepository jobRepository, UserRepository userRepository, StatisticsService statisticsService, SimpMessagingTemplate messagingTemplate) {
        this.employerRepository = employerRepository;
        this.jobSeekerRepository = jobSeekerRepository;
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.statisticsService = statisticsService;
        this.messagingTemplate = messagingTemplate;
    }

    // Get total count of employers
    @GetMapping("/employers/count")
    public ResponseEntity<Long> getTotalEmployers() {
        long count = employerRepository.count();
        return ResponseEntity.ok(count);
    }

    // Get total count of job seekers
    @GetMapping("/job-seekers/count")
    public ResponseEntity<Long> getTotalJobSeekers() {
        long count = jobSeekerRepository.count();
        return ResponseEntity.ok(count);
    }

    // Get total count of job applications
    @GetMapping("/applications/count")
    public ResponseEntity<Long> getTotalApplications() {
        long count = applicationRepository.count();
        return ResponseEntity.ok(count);
    }

    // Get total count of jobs
    @GetMapping("/jobs/count")
    public ResponseEntity<Long> getTotalJobs() {
        long count = jobRepository.count();
        return ResponseEntity.ok(count);
    }

    // Get top jobs with most applications
    @GetMapping("/jobs/top")
    public ResponseEntity<List<Map<String, Object>>> getTopJobs() {
        List<Object[]> results = jobRepository.findTopJobsByApplications();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> jobData = new HashMap<>();
            jobData.put("jobTitle", row[0]);
            jobData.put("companyName", row[1]);
            jobData.put("applicationCount", row[2]);
            response.add(jobData);
        }
        return ResponseEntity.ok(response);
    }


    // Get new users (registered within the last 7 days)
    @GetMapping("/users/new")
    public ResponseEntity<List<Users>> getNewUsers() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusDays(7);
        List<Users> newUsers = userRepository.findByCreatedAtAfter(oneWeekAgo);
        return ResponseEntity.ok(newUsers);
    }

    @GetMapping("/application-status")
    public Map<String, Long> getApplicationStatusStats() {
        return statisticsService.getApplicationStatusBreakdown();
    }

    @GetMapping
    public Map<String, Object> getStats(@RequestParam String startDate, @RequestParam String endDate) {
        LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(23, 59, 59);
        return statisticsService.getStatistics(startDateTime, endDateTime);
    }

    @PostMapping("/update")
    public void pushUpdate() {
        LocalDateTime startDateTime = LocalDate.now().minusDays(7).atStartOfDay();
        LocalDateTime endDateTime = LocalDate.now().atTime(23, 59, 59);

        Map<String, Object> stats = statisticsService.getStatistics(startDateTime, endDateTime);
        messagingTemplate.convertAndSend("/topic/stats", stats);
    }

}

