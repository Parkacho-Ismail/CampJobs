package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.DTO.JobSeekerDTO;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import com.example.campsjobs.Repository.JobSeekerRepository;
import com.example.campsjobs.Repository.UserRepository;
import com.example.campsjobs.Service.JobSeekerInterface;
import lombok.RequiredArgsConstructor;
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
public class JobSeekerImpl implements JobSeekerInterface {
    private final JobSeekerRepository jobSeekerRepository;
    private final UserRepository userRepository;
    private final String UPLOAD_DIR = "uploads/";

    public JobSeekerImpl(JobSeekerRepository jobSeekerRepository, UserRepository userRepository) {
        this.jobSeekerRepository = jobSeekerRepository;
        this.userRepository = userRepository;
    }

    @Override
    public JobSeeker saveJobSeeker(JobSeekerDTO jobSeekerDTO) {
        Users user = userRepository.findById(jobSeekerDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        JobSeeker jobSeeker = new JobSeeker();
        jobSeeker.setFirstName(jobSeekerDTO.getFirstName());
        jobSeeker.setLastName(jobSeekerDTO.getLastName());
        jobSeeker.setSeekerGender(jobSeekerDTO.getSeekerGender());
        jobSeeker.setSeekerNat(jobSeekerDTO.getSeekerNat());
        jobSeeker.setSeekerTel(jobSeekerDTO.getSeekerTel());
        jobSeeker.setSeekerDob(jobSeekerDTO.getSeekerDob());
        jobSeeker.setUser(user);

        // Save image if available
        if (jobSeekerDTO.getSeekerImg() != null && !jobSeekerDTO.getSeekerImg().isEmpty()) {
            try {
                String file = jobSeekerDTO.getSeekerImg();
                Path sourcePath = Paths.get(file);  // Convert the file path string to a Path object
                String fileName = UUID.randomUUID() + "_" + sourcePath.getFileName().toString();  // Get the file name from the path
                String filePath = "uploads/" + fileName;

                // Create directories if they don't exist
                Path path = Paths.get(filePath);
                Files.createDirectories(path.getParent());

                // Copy the file from the source to the new location
                Files.copy(sourcePath, path);

                // Save the file path in the database
                jobSeeker.setSeekerImg(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return jobSeekerRepository.save(jobSeeker);
    }

    @Override
    public List<JobSeeker> getAllJobSeekers() {
        return jobSeekerRepository.findAll();
    }

    @Override
    public JobSeeker getJobSeekerById(Long id) {
        return jobSeekerRepository.findById(id).orElse(null);
    }

    @Override
    public Optional<JobSeeker> getSeekerByUserId(Long userId) {
        return jobSeekerRepository.findByUserUserId(userId);
    }

    @Override
    public Long getSeekerIdByUserId(Long userId) {
        return jobSeekerRepository.findByUser_UserId(userId)
                .map(JobSeeker::getSeekerId)
                .orElse(null); // Returns null if not found
    }

    @Override
    public void updateJobSeekerDetails(Long seekerId, JobSeekerDTO jobSeekerDTO) {
        JobSeeker jobSeeker = jobSeekerRepository.findById(seekerId)
                .orElseThrow(() -> new RuntimeException("Job seeker not found"));

        jobSeeker.setFirstName(jobSeekerDTO.getFirstName());
        jobSeeker.setLastName(jobSeekerDTO.getLastName());
        jobSeeker.setSeekerGender(jobSeekerDTO.getSeekerGender());
        jobSeeker.setSeekerNat(jobSeekerDTO.getSeekerNat());
        jobSeeker.setSeekerTel(jobSeekerDTO.getSeekerTel());

        jobSeekerRepository.save(jobSeeker);
    }

    @Override
    public JobSeekerDTO getJobSeekerDetails(Long userId) {
        Optional<JobSeeker> optionalJobSeeker = jobSeekerRepository.findByUser_UserId(userId);

        // Check if jobSeeker exists, else throw exception
        JobSeeker jobSeeker = optionalJobSeeker.orElseThrow(() -> new RuntimeException("Job seeker not found"));

        // Map to DTO
        JobSeekerDTO jobSeekerDTO = new JobSeekerDTO();
        jobSeekerDTO.setSeekerId(jobSeeker.getSeekerId());
        jobSeekerDTO.setFirstName(jobSeeker.getFirstName());
        jobSeekerDTO.setLastName(jobSeeker.getLastName());
        jobSeekerDTO.setSeekerGender(jobSeeker.getSeekerGender());
        jobSeekerDTO.setSeekerDob(jobSeeker.getSeekerDob());
        jobSeekerDTO.setSeekerNat(jobSeeker.getSeekerNat());
        jobSeekerDTO.setSeekerTel(jobSeeker.getSeekerTel());

        return jobSeekerDTO;
    }
}
