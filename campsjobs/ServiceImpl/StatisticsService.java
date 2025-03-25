package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.Entity.Application;
import com.example.campsjobs.Repository.ApplicationRepository;
import com.example.campsjobs.Repository.JobRepository;
import com.example.campsjobs.Repository.UserRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class StatisticsService {

    private final ApplicationRepository jobApplicationRepo;
    private final UserRepository userRepo;
    private final JobRepository jobRepo;
    private final ApplicationRepository applicationRepository;

    public StatisticsService(ApplicationRepository jobApplicationRepo, UserRepository userRepo, JobRepository jobRepo, ApplicationRepository applicationRepository) {
        this.jobApplicationRepo = jobApplicationRepo;
        this.userRepo = userRepo;
        this.jobRepo = jobRepo;
        this.applicationRepository = applicationRepository;
    }

    public Map<String, Object> getStatistics(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        return Map.of(
                "newUsers", userRepo.countByCreatedAtBetween(startDateTime, endDateTime),
                "applications", jobApplicationRepo.countByAppliedAtBetween(startDateTime, endDateTime),
                "jobsPosted", jobRepo.countByPostedAtBetween(startDateTime, endDateTime)
        );
    }


    public long getNewUsersLastWeek() {
        LocalDate lastWeek = LocalDate.now().minusDays(7);
        return userRepo.countByCreatedAtAfter(lastWeek);
    }

    public long getTotalUsers() {
        return userRepo.count();
    }

    public long getApplicationsLastWeek() {
        LocalDate lastWeek = LocalDate.now().minusDays(7);
        return jobApplicationRepo.countByAppliedAtAfter(lastWeek);
    }


        public Map<String, Long> getApplicationStatusBreakdown() {
            Map<String, Long> statusCounts = new HashMap<>();
            statusCounts.put("Pending", jobApplicationRepo.countByAppStatus(Application.ApplicationStatus.PENDING));
            statusCounts.put("Shortlisted", jobApplicationRepo.countByAppStatus(Application.ApplicationStatus.SHORTLISTED));
            statusCounts.put("Accepted", jobApplicationRepo.countByAppStatus(Application.ApplicationStatus.ACCEPTED));
            statusCounts.put("Rejected", jobApplicationRepo.countByAppStatus(Application.ApplicationStatus.REJECTED));
            return statusCounts;
        }

    public long getJobsPostedLastWeek() {
        LocalDateTime lastWeek = LocalDate.now().minusDays(7).atStartOfDay();
        return jobRepo.countByPostedAtAfter(lastWeek);
    }

}
