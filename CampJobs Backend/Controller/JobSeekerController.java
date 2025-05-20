package com.example.campsjobs.Controller;

import com.example.campsjobs.Service.JobSeekerInterface;
import com.example.campsjobs.ServiceImpl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import com.example.campsjobs.DTO.JobSeekerDTO;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.ServiceImpl.JobSeekerImpl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/job-seeker")
@CrossOrigin(origins = "*")
public class JobSeekerController {
    private final JobSeekerImpl jobSeekerService;
    private final JobSeekerInterface jobSeekerInterface;

    @Autowired
    public JobSeekerController(JobSeekerImpl jobSeekerService, UserServiceImpl userService, JobSeekerInterface jobSeekerInterface) {
        this.jobSeekerService = jobSeekerService;
        this.jobSeekerInterface = jobSeekerInterface;
    }


    // Corrected Job Seeker Save Method
    @PostMapping(value = "/save", consumes = {"multipart/form-data"})
    public ResponseEntity<?> saveJobSeeker(
            @RequestPart("jobSeeker") JobSeekerDTO jobSeekerDTO,
            @RequestPart(value = "seekerImg", required = false) MultipartFile seekerImg) {
        try {
            // Save image file if provided
            if (seekerImg != null && !seekerImg.isEmpty()) {
                String fileName = UUID.randomUUID().toString() + "_" + seekerImg.getOriginalFilename();
                Path filePath = Paths.get("uploads/" + fileName);
                Files.createDirectories(filePath.getParent()); // Ensure folder exists
                Files.write(filePath, seekerImg.getBytes());

                jobSeekerDTO.setSeekerImg(fileName); // Save filename in DTO
            } else {
                jobSeekerDTO.setSeekerImg("default-profile.png"); // Default image
            }

            // Save Job Seeker
            JobSeeker savedJobSeeker = jobSeekerService.saveJobSeeker(jobSeekerDTO);
            if (savedJobSeeker == null) {
                return ResponseEntity.status(500).body("Error: JobSeeker was not saved (returned null)");
            }

            return ResponseEntity.ok(savedJobSeeker);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("File upload failed: " + e.getMessage());
        }
    }

    // Get All Job Seekers
    @GetMapping("/all-job-seekers")
    public ResponseEntity<List<JobSeeker>> getAllJobSeekers() {
        List<JobSeeker> jobSeekers = jobSeekerService.getAllJobSeekers();
        return ResponseEntity.ok(jobSeekers);
    }

    @GetMapping("/user/{userId}")
    public Long getSeekerId(@PathVariable Long userId) {
        System.out.println("Fetching job seeker with User ID: " + userId);
        return jobSeekerService.getSeekerIdByUserId(userId);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<JobSeeker> getJobSeekerByUserId(@PathVariable Long userId) {
        return jobSeekerInterface.getSeekerByUserId(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @GetMapping("/seeker/details/{userId}")
    public ResponseEntity<JobSeekerDTO> getJobSeekerDetails(@PathVariable Long userId) {
        JobSeekerDTO jobSeekerDTO = jobSeekerService.getJobSeekerDetails(userId);
        return ResponseEntity.ok(jobSeekerDTO);
    }
    @PutMapping("/update/{seekerId}")
    public ResponseEntity<String> updateJobSeekerDetails(
            @PathVariable Long seekerId,
            @RequestBody JobSeekerDTO jobSeekerDTO) {
        jobSeekerService.updateJobSeekerDetails(seekerId, jobSeekerDTO);
        return ResponseEntity.ok("Job seeker details updated successfully");
    }

    // Serve Uploaded Images
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
}
