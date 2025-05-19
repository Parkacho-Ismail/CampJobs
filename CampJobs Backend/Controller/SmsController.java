package com.example.campsjobs.Controller;

import com.example.campsjobs.ServiceImpl.SmsSenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sms")
public class SmsController {

    @Autowired
    private SmsSenderService smsSenderService;

    @PostMapping("/send")
    public String sendSms(@RequestParam String phoneNumber, @RequestParam String message) {
        return smsSenderService.sendSms(phoneNumber, message);
    }
}

