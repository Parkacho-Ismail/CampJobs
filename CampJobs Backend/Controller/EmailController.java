package com.example.campsjobs.Controller;

import com.example.campsjobs.Service.EmailInterface;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/email")
@RequiredArgsConstructor
public class EmailController {

    private final EmailInterface emailService;
    @PostMapping("/send")
    public ResponseEntity<String> sendTestEmail(@RequestParam String to) {
        emailService.sendEmail(to, "Test Email", "This is a test email from Spring Boot using Gmail SMTP.");
        return ResponseEntity.ok("Email sent successfully!");
    }

    @PostMapping("/status/send")
    public ResponseEntity<String> sendStatusEmail(
            @RequestParam String to,
            @RequestBody Map<String, String> emailDetails) {
        String subject = emailDetails.get("subject");
        String body = emailDetails.get("body");
        emailService.sendEmail(to, subject, body);
        return ResponseEntity.ok("Email sent successfully!");
    }
}
