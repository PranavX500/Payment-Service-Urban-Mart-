package com.example.Payment_Service.Service;

import com.example.Payment_Service.DTO.Emailsendtonot;
import com.example.Payment_Service.DTO.RazorpayOrderResponse;
import com.example.Payment_Service.DTO.ResponesToOrderService;
import com.example.Payment_Service.Model.PaymentEntity;
import com.example.Payment_Service.Model.PaymentSuccessEvent;
import com.example.Payment_Service.Repositery.PayementRepositery;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class PaymentConsumer {

    @Value("${razorpay.key.secret}")
    String razorpaySecret;

    @Autowired
    private PaymentProducer2 paymentProducer2;

    @Autowired
    private PaymentProducer paymentProducer;



    @Autowired
    private PayementRepositery paymentRepository;

    @Autowired
    private RazorpayClient razorpayClient;

    @KafkaListener(
            topics = "Orders",
            groupId = "Payment-group",
            containerFactory = "responesToOrderKafkaListenerContainerFactory"
    )
    public void handlePayment(ResponesToOrderService orderDetails) throws RazorpayException {

        if (orderDetails == null) {
            throw new RuntimeException("Order details are null");
        }

        JSONObject options = new JSONObject();
        options.put("amount", orderDetails.getTotalPrice() * 100);
        options.put("currency", "INR");
        options.put("receipt", "order_rcpt_" + orderDetails.getOrderId());

        Order razorpayOrder = razorpayClient.orders.create(options);



        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(orderDetails.getOrderId());
        payment.setTotalprice(orderDetails.getTotalPrice());
        payment.setPaymentStatus("PENDING");
        payment.setPaymentDate(LocalDateTime.now());
        payment.setRazorpayOrderId(razorpayOrder.get("id"));
        payment.setEmailId(orderDetails.getEmailId());
        payment.setUserId(orderDetails.getUserId());



        paymentRepository.save(payment);

        RazorpayOrderResponse response = new RazorpayOrderResponse();
        response.setOrderId(orderDetails.getOrderId());
        response.setRazorpayOrderId(razorpayOrder.get("id"));
        response.setAmount(razorpayOrder.get("amount"));
        response.setCurrency(razorpayOrder.get("currency"));
        response.setReceipt(razorpayOrder.get("receipt"));
        response.setRequestId(orderDetails.getRequestId());

        paymentProducer2.sendRazorpayOrder(response);
    }


    public void verifyPayment(String razorpayOrderId, String razorpayPaymentId, String razorpaySignature) throws Exception {



        String data = razorpayOrderId + "|" + razorpayPaymentId;


        boolean isValidSignature = Utils.verifySignature(data, razorpaySignature, razorpaySecret);

        if (!isValidSignature) {
            throw new RuntimeException("Invalid Razorpay signature");
        }


        PaymentEntity payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Payment not found for order ID: " + razorpayOrderId));


        payment.setPaymentStatus("SUCCESS");
        paymentRepository.save(payment);


        PaymentSuccessEvent event = new PaymentSuccessEvent();
        event.setOrderId(payment.getOrderId());
        event.setTotalprice(payment.getTotalprice());
        event.setStatus("SUCCESS");
        event.setPaymentTime(LocalDateTime.now());




        Emailsendtonot emailEvent = new Emailsendtonot(
                payment.getEmailId(),
                payment.getUserId(),
                payment.getOrderId(),
                payment.getTotalprice()
        );

        paymentProducer.sendPaymentSuccess(event);
        paymentProducer.sendEmail(emailEvent);
    }
}
