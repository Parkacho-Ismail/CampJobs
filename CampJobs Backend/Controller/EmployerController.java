package com.example.campsjobs.Controller;

import com.example.campsjobs.DTO.EmployerDTO;
import com.example.campsjobs.Entity.Application;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Repository.ApplicationRepository;
import com.example.campsjobs.Repository.EmployerRepository;
import com.example.campsjobs.Repository.JobRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.ApplicationInterface;
import com.example.campsjobs.ServiceImpl.EmployerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/employer")
@CrossOrigin(origins = "*")
public class EmployerController {

    private final EmployerImpl employerService;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private  final ApplicationRepository applicationRepository;
    private final EmployerRepository employerRepository;
    private final ApplicationInterface applicationInterface;

    @Autowired
    public EmployerController(EmployerImpl employerService, JobRepository jobRepository, UserRepository userRepository, ApplicationRepository applicationRepository, EmployerRepository employerRepository, ApplicationInterface applicationInterface) {
        this.employerService = employerService;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.employerRepository = employerRepository;
        this.applicationInterface = applicationInterface;
    }

    // ✅ Save Employer Method with Image Upload
    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public ResponseEntity<?> saveEmployer(
            @RequestPart("employer") EmployerDTO employerDTO,
            @RequestPart(value = "empLogo", required = false) MultipartFile empLogo) {
        try {
            // Save image file if provided
            if (empLogo != null && !empLogo.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + empLogo.getOriginalFilename();
                Path filePath = Paths.get("uploads/" + fileName);
                Files.createDirectories(filePath.getParent()); // Ensure folder exists
                Files.write(filePath, empLogo.getBytes());

                employerDTO.setEmpLogo(fileName); // Save filename in DTO
            } else {
                employerDTO.setEmpLogo("default-logo.png"); // Default logo
            }

            // Save Employer
            Employer savedEmployer = employerService.saveEmployer(employerDTO);
            if (savedEmployer == null) {
                return ResponseEntity.status(500).body("Error: Employer was not saved (returned null)");
            }

            return ResponseEntity.ok(savedEmployer);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }


    @GetMapping("/applications")
    public List<Application> getApplicationsForEmployer(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String email = userDetails.getUsername();

        Optional<Employer> employer = employerRepository.findByUser_Email(email);
        if (employer.isEmpty()) {
            throw new RuntimeException("Employer not found");
        }

        return applicationInterface.getApplicationsByEmployer(employer.get().getEmpId());
    }

//    @GetMapping("/job/{jobId}/applications")
//    public ResponseEntity<?> getApplicationsForJob(
//            @AuthenticationPrincipal UserDetails userDetails,
//            @PathVariable Long jobId) {
//
//        // Get logged-in employer's email
//        String employerEmail = userDetails.getUsername();
//        Users employerUser = userRepository.findByEmail(employerEmail)
//                .orElseThrow(() -> new RuntimeException("Employer not found"));
//
//        // Fetch job posted by the employer (using Users instead of Employer)
//        Job job = jobRepository.findByIdAndEmployer(jobId, employerUser)
//                .orElseThrow(() -> new RuntimeException("Job not found or does not belong to you"));
//
//        // Retrieve applications for the job
//        List<Application> applications = applicationRepository.findByJob(job);
//
//        return ResponseEntity.ok(applications);
//    }





    // ✅ Get All Employers
    @GetMapping("/all-employers")
    public ResponseEntity<List<Employer>> getAllEmployers() {
        List<Employer> employers = employerService.getAllEmployers();
        return ResponseEntity.ok(employers);
    }

    // ✅ Get a Single Employer by ID
    @GetMapping("/{id}")
    public ResponseEntity<Employer> getEmployer(@PathVariable Long id) {
        Employer employer = employerService.getEmployerById(id);
        if (employer != null) {
            return ResponseEntity.ok(employer);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Serve Uploaded Employer Logos
    @GetMapping("/images/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            Path filePath = Paths.get("uploads/").resolve(filename).normalize();
            UrlResource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 1. Get employer ID using userId
    @GetMapping("/getEmpId/{userId}")
    public ResponseEntity<Long> getEmployerId(@PathVariable Long userId) {
        Long empId = employerService.getEmployerIdByUserId(userId);
        return ResponseEntity.ok(empId);
    }

    // 2. Get employer details by empId
    @GetMapping("/details/{empId}")
    public ResponseEntity<Employer> getEmployerDetails(@PathVariable Long empId) {
        Optional<Employer> employer = employerService.getEmployerDetails(empId);
        return employer.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 3. Update employer details
    @PutMapping("/update/{empId}")
    public ResponseEntity<Employer> updateEmployer(@PathVariable Long empId, @RequestBody EmployerDTO employerDTO) {
        Employer updatedEmployer = employerService.updateEmployer(empId, employerDTO);
        return ResponseEntity.ok(updatedEmployer);
    }

    //to get employer name using jobId
    @GetMapping("/employer-name")
    public Optional<String> getEmployerName(@RequestParam Long jobId) {
        return employerService.getEmployerNameByJobId(jobId);
    }
}
