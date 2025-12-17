package com.example.Payment_Service.Model;



import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payments")
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;
    private Long UserId;
    private Long orderId;
    private Double totalprice;
    private String paymentMode;
    private String paymentStatus;
    private LocalDateTime paymentDate;
    private String razorpayOrderId;
    private String emailId;


}
