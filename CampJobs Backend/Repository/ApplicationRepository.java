package com.example.campsjobs.Repository;


import com.example.campsjobs.Entity.Application;
import com.example.campsjobs.Entity.Job;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    Optional<Application> findByappId(Long seekerId);
    // Method to find applications by JobSeeker entity
    List<Application> findByJobSeeker(Users jobSeeker);

    //ensure employer sees applications to a job
    List<Application> findByJob(Job job);

    //Each jobseeker is able to see their oen applications
    List<Application> findByJobSeekerEmail(String email);

    List<Application> findByJobIn(List<Job> jobs);

    List<Application> findByJob_JobId(Long jobId); // Fetch applications for a specific job

    void deleteByJob_JobId(Long jobId); // Custom delete method

    void deleteByJobSeeker_UserId(Long userId);


    List<Application> findByJobSeekerUserId(Long seekerId);//seeker applications

    @Modifying
    @Transactional
    @Query("DELETE FROM Application a WHERE a.job.jobId = :jobId")
    void deleteByJobId(@Param("jobId") Long jobId);


    long countByAppliedAtAfter(LocalDate date);

    long countByAppStatus(Application.ApplicationStatus appStatus); // Correct field name

    long countByAppliedAtBetween(LocalDateTime startDate, LocalDateTime  endDate); // Use appliedAt instead of createdAt

//methods to delete an application
void deleteByAppId(Long appId);

    boolean existsById(Long appId); // Check if the application exists

    boolean existsByAppId(Long appId);
    // Checks if the application exists and belongs to the job seeker
    boolean existsByJob_JobIdAndJobSeeker_UserId(Long jobId, Long seekerId);

    boolean existsByJobAndJobSeeker(Job job, Users jobSeeker);


@Query(value = "SELECT u.email FROM applications a " +
        "JOIN jobseeker js ON a.seeker_id = js.seeker_id " +
        "JOIN users u ON js.user_id = u.user_id " +
        "WHERE a.app_id = :appId", nativeQuery = true)
String findJobSeekerEmailByAppId(@Param("appId") Long appId);

}

