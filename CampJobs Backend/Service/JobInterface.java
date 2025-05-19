package com.example.campsjobs.Service;

import java.util.Optional;
import com.example.campsjobs.DTO.EmployerDTO;
import com.example.campsjobs.DTO.JobDTO;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.Job;
import jakarta.transaction.Transactional;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface JobInterface {
    ResponseEntity<Long> saveJob(JobDTO jobDTO);
    List<Job> getAllJobs();
    Job getJobById(Long id);
    ResponseEntity<?> saveJobForEmployer(JobDTO jobDTO, Long userId);//added

    List<Job> getAllJobsWithEmployers();

    void deleteJobByEmployer(Long jobId, Long empId);

    void deleteJobById(Long jobId);




    @Transactional
    void updateJobStatus(Long jobId, String jobStatus);

    int getApplicationCount(Long jobId);
    Optional<JobDTO> getJobDetails(Long jobId);
    Job updateJob(Long jobId, JobDTO jobDTO);

        void updateExpiredJobs();


}
