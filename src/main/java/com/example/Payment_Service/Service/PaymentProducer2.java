package com.example.Payment_Service.Service;

import com.example.Payment_Service.DTO.RazorpayOrderResponse;
import com.example.Payment_Service.Model.PaymentSuccessEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer2 {
    private final KafkaTemplate<String, RazorpayOrderResponse> kafkaTemplate;

    public PaymentProducer2(KafkaTemplate<String, RazorpayOrderResponse> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendRazorpayOrder(RazorpayOrderResponse response) {
        System.out.println(response);
        kafkaTemplate.send("Payment-Response", response);
    }
}
