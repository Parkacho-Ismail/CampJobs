package com.example.campsjobs.DTO;

import com.example.campsjobs.Entity.Users;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSeekerDTO {
    private Long seekerId;
    private Long userId;
    private String firstName;
    private String lastName;
    private String seekerGender;
    private String seekerNat;
    private String seekerTel;
    private LocalDate seekerDob;
//    private MultipartFile seekerImg;
    @JsonIgnore
    private String seekerImg;
    private Users user;

//    public MultipartFile getSeekerImg() {
//        return seekerImg;
//    }
//
//    public void setSeekerImg(MultipartFile seekerImg) {
//        this.seekerImg = seekerImg;
//    }
}

