package com.example.campsjobs.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    @NotBlank(message = "Role is required")
    private String role; // ADMIN, EMPLOYER, JOBSEEKER
    @Email(message = "Valid email required")
    private String email;
    @NotBlank(message = "Password required")
    private String password;
}
