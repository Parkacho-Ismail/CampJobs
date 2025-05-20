package com.example.campsjobs.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmployerDTO {
    private Long userId;
    private String empName;

    private String empContact;

    private String empAddress;

    private String empIndustry;

    private String empWebsite;
    private String empDesc;
    private String empLogo;

}
