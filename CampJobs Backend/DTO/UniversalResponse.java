package com.example.campsjobs.DTO;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversalResponse {
    private String status;
    private String message;
    private Object data;
}
