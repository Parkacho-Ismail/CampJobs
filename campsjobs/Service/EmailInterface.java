package com.example.campsjobs.Service;

public interface EmailInterface {
    void sendEmail(String to, String subject, String body);

    // New method to handle contact form emails
    void sendContactEmail(String senderName, String senderEmail, String messageContent);

}
