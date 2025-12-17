package com.example.Payment_Service.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RazorpayOrderResponse {
    private String razorpayOrderId;
    private Integer amount;
    private String currency;
    private String receipt;
    private Long orderId;
    private String requestId;
}