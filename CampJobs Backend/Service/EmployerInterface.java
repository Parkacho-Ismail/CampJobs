package com.example.campsjobs.Service;

import com.example.campsjobs.DTO.EmployerDTO;
import com.example.campsjobs.DTO.JobSeekerDTO;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.JobSeeker;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

public interface EmployerInterface {

    Employer saveEmployer(EmployerDTO employerDTO);
    List<Employer> getAllEmployers();
    Employer getEmployerById(Long id);

    Long getEmployerIdByUserId(Long userId);
    Optional<Employer> getEmployerDetails(Long empId);
    Employer updateEmployer(Long empId, EmployerDTO employerDTO);

    Optional<String> getEmployerNameByJobId(Long jobId);

}
