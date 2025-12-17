package com.example.Payment_Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Emailsendtonot {
    private String emailId;
    private Long UserId;
    private Long orderId;
    private Double totalprice;
}
