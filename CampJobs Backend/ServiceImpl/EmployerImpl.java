package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.DTO.EmployerDTO;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.EmployerRepository;
import com.example.campsjobs.Repository.JobRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.EmployerInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
//@NoArgsConstructor(force = true)
public class EmployerImpl implements EmployerInterface {
    private final EmployerRepository employerRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final String UPLOAD_DIR = "uploads/";
    public EmployerImpl(EmployerRepository employerRepository, UserRepository userRepository, JobRepository jobRepository) {
        this.employerRepository = employerRepository;
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
    }


    @Override
    public Employer saveEmployer(EmployerDTO employerDTO) {
        Users user = userRepository.findById(employerDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Employer not found"));
        Employer employer = new Employer();
        employer.setEmpName(employerDTO.getEmpName());
        employer.setEmpContact(employerDTO.getEmpContact());
        employer.setEmpAddress(employerDTO.getEmpAddress());
        employer.setEmpIndustry(employerDTO.getEmpIndustry());
        employer.setEmpWebsite(employerDTO.getEmpWebsite());
        employer.setEmpDesc(employerDTO.getEmpDesc());
        employer.setUser(user);

        // Save image if available
        if (employerDTO.getEmpLogo() != null && !employerDTO.getEmpLogo().isEmpty()) {
            try {
                String file = employerDTO.getEmpLogo();
                Path sourcePath = Paths.get(file);  // Convert the file path string to a Path object
                String fileName = UUID.randomUUID() + "_" + sourcePath.getFileName().toString();  // Get the file name from the path
                String filePath = "uploads/" + fileName;

                // Create directories if they don't exist
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());

                // Copy the file from the source to the new location
                Files.copy(sourcePath, path);

                // Save the file path in the database
                employer.setEmpLogo(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return employerRepository.save(employer);

//        Employer savedEmployer = employerRepository.save(employer);
//        return ResponseEntity.ok(savedEmployer.getEmpId()); // Return employer ID
    }

    @Override
    public List<Employer> getAllEmployers() {
        return employerRepository.findAll();
    }
    @Override
    public Employer getEmployerById(Long id) {
        return employerRepository.findById(id).orElse(null);
    }

    //for getting and updating employer details
    @Override
    public Long getEmployerIdByUserId(Long userId) {
        return employerRepository.findByUserUserId(userId)
                .map(Employer::getEmpId)
                .orElseThrow(() -> new RuntimeException("Employer not found for User ID: " + userId));
    }

    @Override
    public Optional<Employer> getEmployerDetails(Long empId) {
        return employerRepository.findById(empId);
    }

    @Override
    public Employer updateEmployer(Long empId, EmployerDTO employerDTO) {
        Employer employer = employerRepository.findById(empId)
                .orElseThrow(() -> new RuntimeException("Employer not found with ID: " + empId));

        employer.setEmpName(employerDTO.getEmpName());
        employer.setEmpContact(employerDTO.getEmpContact());
        employer.setEmpAddress(employerDTO.getEmpAddress());
        employer.setEmpIndustry(employerDTO.getEmpIndustry());
        employer.setEmpWebsite(employerDTO.getEmpWebsite());
        employer.setEmpDesc(employerDTO.getEmpDesc());
        employer.setEmpLogo(employerDTO.getEmpLogo());

        return employerRepository.save(employer);
    }

    @Override
    public Optional<String> getEmployerNameByJobId(Long jobId) {
        return jobRepository.findByJobId(jobId)
                .map(job -> job.getEmployer().getEmpName());
    }
}
