package com.example.campsjobs.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobDTO {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Long jobId;
    private String jobTitle;

    private String jobType; // Full-time, Part-time, Internship

    private String jobLocation;

    private Double jobSalary;


    private LocalDateTime expiryDate;

    private String jobDesc;
    private String jobReqs;
    private String jobStatus; //PENDING, APPROVED, REJECTED

//    private LocalDateTime postedAt = LocalDateTime.now();
    private LocalDateTime postedAt = LocalDateTime.now(); // Default to now

    public JobDTO(Long jobId, String jobTitle, String jobType, String jobLocation, Double jobSalary, String jobDesc, String jobReqs, String jobStatus, LocalDateTime postedAt, LocalDateTime expiryDate) {
        this.jobId = jobId;
        this.jobTitle = jobTitle;
        this.jobType = jobType;
        this.jobLocation = jobLocation;
        this.jobSalary = jobSalary;
        this.jobDesc = jobDesc;
        this.jobReqs = jobReqs;
        this.jobStatus = jobStatus;
        this.postedAt = postedAt;
        this.expiryDate = expiryDate;
    }


    public String getFormattedPostedAt() {
        return postedAt != null ? postedAt.format(FORMATTER) : null;
    }

    public String getFormattedExpiryDate() {
        return expiryDate != null ? expiryDate.format(FORMATTER) : null;
    }
//    private Long EmpId;
//    private Employer employer;
}
