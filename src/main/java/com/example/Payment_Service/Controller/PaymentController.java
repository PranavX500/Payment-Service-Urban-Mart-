package com.example.Payment_Service.Controller;


import com.example.Payment_Service.Service.PaymentConsumer;
import com.example.Payment_Service.Service.PaymentProducer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    PaymentProducer producer;
    @Autowired
    PaymentConsumer consumer;
    @Autowired
    PaymentProducer paymentProducer;




    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestParam("razorpay_order_id") String razorpayOrderId,
            @RequestParam("razorpay_payment_id") String razorpayPaymentId,
            @RequestParam("razorpay_signature") String razorpaySignature) {

        System.out.println("Order ID: " + razorpayOrderId);
        System.out.println("Payment ID: " + razorpayPaymentId);
        System.out.println("Signature: " + razorpaySignature);

        try {
            consumer.verifyPayment(razorpayOrderId, razorpayPaymentId, razorpaySignature);
            return ResponseEntity.ok("Payment verified successfully!");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Payment verification failed: " + e.getMessage());
        }
    }
}


