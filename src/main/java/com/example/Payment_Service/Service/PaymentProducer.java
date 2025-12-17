package com.example.Payment_Service.Service;

import com.example.Payment_Service.DTO.Emailsendtonot;

import com.example.Payment_Service.Model.PaymentSuccessEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate;
    private final KafkaTemplate<String, Emailsendtonot> kafkaTemplatee;

    public PaymentProducer(KafkaTemplate<String, PaymentSuccessEvent> kafkaTemplate, KafkaTemplate<String, Emailsendtonot> kafkaTemplatee) {
        this.kafkaTemplate = kafkaTemplate;
        this.kafkaTemplatee = kafkaTemplatee;
    }

    public void sendPaymentSuccess(PaymentSuccessEvent event) {
        kafkaTemplate.send("payment-success-topic", event);
        System.out.println(" Sent event to Kafka: " + event);
    }

    public void sendEmail(Emailsendtonot emailsendtonot){
        kafkaTemplatee.send("Notification",emailsendtonot);
        System.out.println(" Sent event to Kafka: " +emailsendtonot);
    }


//    public RazorpayDto sendRazorpayOrder(RazorpayOrderResponse response) {
//      RazorpayDto dto=new RazorpayDto();
//      dto.setOrderId(response.getOrderId());
//      dto.setAmount(response.getAmount());
//      dto.setCurrency(response.getCurrency());
//      dto.setReceipt(response.getReceipt());
//      dto.setOrderId(response.getOrderId());
//
//      return dto;
//    }
}

