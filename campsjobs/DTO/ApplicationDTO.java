package com.example.campsjobs.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDTO {
    private Long appId;         // Application ID
    private String jobTitle;    // Job title
    private String employerName; // Employer name
    private String seekerName;  // Job seeker name
    private String resumeImg;
    private String letterImg;
    private String certImg;
    private String idImg;
    private LocalDateTime appliedAt;
    private String appStatus;
}
