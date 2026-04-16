package com.example.Payment_Service.Repositery;

import com.example.Payment_Service.Model.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface PayementRepositery extends JpaRepository<PaymentEntity,Long> {
    Optional<PaymentEntity> findTopByPaymentStatus(String paymentStatus);
    Optional<PaymentEntity> findByRazorpayOrderId(String razorpayOrderId);


}
