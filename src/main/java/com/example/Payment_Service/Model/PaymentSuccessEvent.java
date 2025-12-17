package com.example.Payment_Service.Model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentSuccessEvent {
    private Long orderId;
    private double totalprice;
    private String status; // SUCCESS or FAILED
    private LocalDateTime paymentTime;

}

