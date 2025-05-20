package com.example.campsjobs.ServiceImpl;

        import org.springframework.beans.factory.annotation.Value;
        import org.springframework.http.*;
        import org.springframework.stereotype.Service;
        import org.springframework.web.client.RestTemplate;

        import java.net.URLEncoder;
        import java.nio.charset.StandardCharsets;

@Service
public class SmsSenderService {

    @Value("${africastalking.apiKey}")
    private String apiKey;

    @Value("${africastalking.username}")
    private String username;

    @Value("${africastalking.smsUrl}")
    private String smsUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public String sendSms(String phoneNumber, String message) {
        try {
            // Ensure phone number is correctly formatted
            phoneNumber = phoneNumber.trim();

            // Encode message to handle special characters
            String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);

            // Construct request body as URL-encoded string with 'default' sender ID
            String requestBody = "username=" + URLEncoder.encode(username, StandardCharsets.UTF_8) +
                    "&to=" + URLEncoder.encode(phoneNumber, StandardCharsets.UTF_8) +
                    "&message=" + encodedMessage +
                    "&from=" + URLEncoder.encode("default", StandardCharsets.UTF_8);  // Explicitly set 'default' sender ID

            // Set request headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("apiKey", apiKey);

            // Create request entity
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

            // Send POST request
            ResponseEntity<String> response = restTemplate.exchange(smsUrl, HttpMethod.POST, requestEntity, String.class);

            return response.getBody();
        } catch (Exception e) {
            return "Error sending SMS: " + e.getMessage();
        }
    }
}
