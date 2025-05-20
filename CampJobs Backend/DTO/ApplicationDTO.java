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
    private Long appId;         
    private String jobTitle;    
    private String employerName; 
    private String seekerName;  
    private String resumeImg;
    private String letterImg;
    private String certImg;
    private String idImg;
    private LocalDateTime appliedAt;
    private String appStatus;
}
