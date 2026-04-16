package com.example.Payment_Service.Controller;

import com.example.Payment_Service.Service.PaymentConsumer;
import com.example.Payment_Service.Service.PaymentProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentProducer producer;

    @Mock
    private PaymentConsumer consumer;

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
    }

    @Test
    void verifyPaymentReturnsOkWhenVerificationSucceeds() throws Exception {
        mockMvc.perform(post("/payment/verify")
                        .param("razorpay_order_id", "order_123")
                        .param("razorpay_payment_id", "pay_123")
                        .param("razorpay_signature", "signature_123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment verified successfully!"));

        verify(consumer).verifyPayment("order_123", "pay_123", "signature_123");
    }

    @Test
    void verifyPaymentReturnsServerErrorWhenVerificationFails() throws Exception {
        doThrow(new RuntimeException("Invalid Razorpay signature"))
                .when(consumer)
                .verifyPayment("order_123", "pay_123", "bad_signature");

        mockMvc.perform(post("/payment/verify")
                        .param("razorpay_order_id", "order_123")
                        .param("razorpay_payment_id", "pay_123")
                        .param("razorpay_signature", "bad_signature"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("Payment verification failed: Invalid Razorpay signature"));
    }
}
