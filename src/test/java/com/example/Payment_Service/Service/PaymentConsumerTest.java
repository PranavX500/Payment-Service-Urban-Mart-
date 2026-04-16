package com.example.Payment_Service.Service;

import com.example.Payment_Service.DTO.Emailsendtonot;
import com.example.Payment_Service.DTO.RazorpayOrderResponse;
import com.example.Payment_Service.DTO.ResponesToOrderService;
import com.example.Payment_Service.Model.PaymentEntity;
import com.example.Payment_Service.Model.PaymentSuccessEvent;
import com.example.Payment_Service.Repositery.PayementRepositery;
import com.razorpay.Order;
import com.razorpay.OrderClient;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConsumerTest {

    @Mock
    private PaymentProducer2 paymentProducer2;

    @Mock
    private PaymentProducer paymentProducer;

    @Mock
    private PayementRepositery paymentRepository;

    @Mock
    private RazorpayClient razorpayClient;

    @Mock
    private OrderClient orderClient;

    @Spy
    @InjectMocks
    private PaymentConsumer paymentConsumer;

    @BeforeEach
    void setUp() {
        razorpayClient.orders = orderClient;
        ReflectionTestUtils.setField(paymentConsumer, "razorpaySecret", "secret");
    }

    @Test
    void handlePaymentCreatesPendingPaymentAndPublishesOrderResponse() throws Exception {
        ResponesToOrderService orderDetails =
                new ResponesToOrderService(250.0, 10L, "req-1", "user@example.com", 99L);
        Order razorpayOrder = new Order(new JSONObject()
                .put("id", "order_razorpay_123")
                .put("amount", 25000)
                .put("currency", "INR")
                .put("receipt", "order_rcpt_10"));

        when(orderClient.create(any(JSONObject.class))).thenReturn(razorpayOrder);
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paymentConsumer.handlePayment(orderDetails);

        ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        PaymentEntity savedPayment = paymentCaptor.getValue();
        assertEquals(10L, savedPayment.getOrderId());
        assertEquals(250.0, savedPayment.getTotalprice());
        assertEquals("PENDING", savedPayment.getPaymentStatus());
        assertEquals("order_razorpay_123", savedPayment.getRazorpayOrderId());
        assertEquals("user@example.com", savedPayment.getEmailId());
        assertEquals(99L, savedPayment.getUserId());
        assertNotNull(savedPayment.getPaymentDate());

        ArgumentCaptor<RazorpayOrderResponse> responseCaptor =
                ArgumentCaptor.forClass(RazorpayOrderResponse.class);
        verify(paymentProducer2).sendRazorpayOrder(responseCaptor.capture());
        RazorpayOrderResponse response = responseCaptor.getValue();
        assertEquals(10L, response.getOrderId());
        assertEquals("order_razorpay_123", response.getRazorpayOrderId());
        assertEquals(25000, response.getAmount());
        assertEquals("INR", response.getCurrency());
        assertEquals("order_rcpt_10", response.getReceipt());
        assertEquals("req-1", response.getRequestId());
    }

    @Test
    void handlePaymentThrowsWhenOrderDetailsAreNull() {
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentConsumer.handlePayment(null));

        assertEquals("Order details are null", exception.getMessage());
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
        verify(paymentProducer2, never()).sendRazorpayOrder(any(RazorpayOrderResponse.class));
    }

    @Test
    void verifyPaymentMarksPaymentSuccessfulAndPublishesEvents() throws Exception {
        PaymentEntity payment = new PaymentEntity();
        payment.setOrderId(10L);
        payment.setTotalprice(250.0);
        payment.setPaymentStatus("PENDING");
        payment.setRazorpayOrderId("order_razorpay_123");
        payment.setEmailId("user@example.com");
        payment.setUserId(99L);

        when(paymentRepository.findByRazorpayOrderId("order_razorpay_123"))
                .thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(PaymentEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doReturn(true).when(paymentConsumer).isSignatureValid("order_razorpay_123|pay_123", "signature_123");

        paymentConsumer.verifyPayment("order_razorpay_123", "pay_123", "signature_123");

        ArgumentCaptor<PaymentEntity> paymentCaptor = ArgumentCaptor.forClass(PaymentEntity.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals("SUCCESS", paymentCaptor.getValue().getPaymentStatus());

        ArgumentCaptor<PaymentSuccessEvent> successEventCaptor =
                ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(paymentProducer).sendPaymentSuccess(successEventCaptor.capture());
        PaymentSuccessEvent successEvent = successEventCaptor.getValue();
        assertEquals(10L, successEvent.getOrderId());
        assertEquals(250.0, successEvent.getTotalprice());
        assertEquals("SUCCESS", successEvent.getStatus());
        assertNotNull(successEvent.getPaymentTime());

        ArgumentCaptor<Emailsendtonot> emailCaptor = ArgumentCaptor.forClass(Emailsendtonot.class);
        verify(paymentProducer).sendEmail(emailCaptor.capture());
        Emailsendtonot emailEvent = emailCaptor.getValue();
        assertEquals("user@example.com", emailEvent.getEmailId());
        assertEquals(99L, emailEvent.getUserId());
        assertEquals(10L, emailEvent.getOrderId());
        assertEquals(250.0, emailEvent.getTotalprice());
    }

    @Test
    void verifyPaymentThrowsWhenSignatureIsInvalid() throws Exception {
        doReturn(false).when(paymentConsumer).isSignatureValid("order_razorpay_123|pay_123", "bad_signature");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentConsumer.verifyPayment("order_razorpay_123", "pay_123", "bad_signature"));

        assertEquals("Invalid Razorpay signature", exception.getMessage());

        verify(paymentRepository, never()).findByRazorpayOrderId(any());
        verify(paymentRepository, never()).save(any(PaymentEntity.class));
        verify(paymentProducer, never()).sendPaymentSuccess(any(PaymentSuccessEvent.class));
        verify(paymentProducer, never()).sendEmail(any(Emailsendtonot.class));
    }

    @Test
    void verifyPaymentThrowsWhenPaymentDoesNotExist() throws Exception {
        when(paymentRepository.findByRazorpayOrderId("order_missing")).thenReturn(Optional.empty());
        doReturn(true).when(paymentConsumer).isSignatureValid("order_missing|pay_123", "signature_123");

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> paymentConsumer.verifyPayment("order_missing", "pay_123", "signature_123"));

        assertEquals("Payment not found for order ID: order_missing", exception.getMessage());

        verify(paymentRepository, never()).save(any(PaymentEntity.class));
        verify(paymentProducer, never()).sendPaymentSuccess(any(PaymentSuccessEvent.class));
        verify(paymentProducer, never()).sendEmail(any(Emailsendtonot.class));
    }
}
