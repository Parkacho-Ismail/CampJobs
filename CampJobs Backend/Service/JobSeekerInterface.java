package com.example.campsjobs.Service;

import com.example.campsjobs.DTO.JobSeekerDTO;
import com.example.campsjobs.Entity.JobSeeker;
import org.apache.catalina.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service

public interface JobSeekerInterface {

    JobSeeker saveJobSeeker(JobSeekerDTO jobSeekerDTO);

    List<JobSeeker> getAllJobSeekers();
    
    JobSeeker getJobSeekerById(Long id);

    Optional<JobSeeker> getSeekerByUserId(Long userId);

    Long getSeekerIdByUserId(Long userId);

    void updateJobSeekerDetails(Long seekerId, JobSeekerDTO jobSeekerDTO);
     JobSeekerDTO getJobSeekerDetails(Long userId) ;

}
