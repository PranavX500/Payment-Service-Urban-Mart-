package com.example.Payment_Service.DTO;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponesToOrderService {
    private Double totalPrice;

    private Long orderId;
    private String requestId;
    private String emailId;
    private Long UserId;





}
