package com.example.campsjobs.ServiceImpl;

import com.example.campsjobs.Service.EmailInterface;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailInterface {
    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // Supports HTML emails

            mailSender.send(message);
            log.info("Email sent successfully to {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
        }
    }

    @Override
    public void sendContactEmail(String senderName, String senderEmail, String messageContent) {
        log.info("Preparing email with: Name = {}, Email = {}, Message = {}",
                senderName, senderEmail, messageContent);

        String to = "campjobsinfo@gmail.com";
        String subject = "New Contact Form Submission from " + senderName;
        String body = "<h3>Contact Form Submission</h3>"
                + "<p><strong>Sender Name:</strong> " + senderName + "</p>"
                + "<p><strong>Sender Email:</strong> " + senderEmail + "</p>"
                + "<p><strong>Message:</strong></p>"
                + "<p>" + messageContent + "</p>";

        sendEmail(to, subject, body);
    }



}
