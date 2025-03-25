package com.example.campsjobs.Controller;

import com.example.campsjobs.DTO.ContactRequestDTO;
import com.example.campsjobs.ServiceImpl.EmailServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@Slf4j
@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")  // Allow frontend requests
public class ContactController {

    @Autowired
    private EmailServiceImpl emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendContactEmail(@RequestBody ContactRequestDTO contactRequestDTO) {
        log.info("Received Contact Request: Name = {}, Email = {}, Message = {}",
                contactRequestDTO.getSenderName(),
                contactRequestDTO.getSenderEmail(),
                contactRequestDTO.getMessageContent());

        emailService.sendContactEmail(contactRequestDTO.getSenderName(),
                contactRequestDTO.getSenderEmail(),
                contactRequestDTO.getMessageContent());
        return ResponseEntity.ok("Email sent successfully!");
    }


}

class ContactRequest {
    private String name;
    private String email;
    private String message;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

