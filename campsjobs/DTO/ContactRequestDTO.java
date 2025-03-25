package com.example.campsjobs.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestDTO {
    private String senderName;
    private String senderEmail;
    private String messageContent;
}

