package com.example.campsjobs.Service;

import com.example.campsjobs.DTO.ApplicationDTO;
import com.example.campsjobs.Entity.Application;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface ApplicationInterface {
    ResponseEntity<Long> saveApplication(ApplicationDTO applicationDTO);
    List<Application> getAllApplications();
    Application getApplicationById(Long id);



    Application applyForJob(Long jobId, Long seekerId,
                               MultipartFile resumeImg, MultipartFile letterImg,
                               MultipartFile certImg, MultipartFile idImg,
                               LocalDateTime appliedAt);


//    List<Application> getApplicationsBySeeker(Long seekerId);
    List<ApplicationDTO> getApplicationsBySeeker(Long seekerId);

    List<Application> getApplicationsByJobSeeker(String email);

    List<Application> getApplicationsByEmployer(String email);

    List<Application> getApplicationsByEmployer(Long employerId);
//     void updateApplicationStatus(Long appId, String newStatus);
void updateApplicationStatus(Long appId, String newStatus, String emailBody);


    List<ApplicationDTO> getApplicationsBySeekerId(Long seekerId);//seeker applications

    void deleteApplication(Long appId);

    String getJobSeekerEmailByAppId(Long appId);






}
