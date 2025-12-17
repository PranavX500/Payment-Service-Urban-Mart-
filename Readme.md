# Payment Service (Ecommerce Microservices)

The **Payment Service** is responsible for **verifying payments and communicating payment status** in the Ecommerce Microservices system.

Payment **order creation is handled asynchronously via Kafka** between **Order Service and Payment Service**.
The only public REST endpoint exposed is used for **Razorpay payment verification**.

---

## Tech Stack

* Spring Boot
* Kafka (Producer & Consumer)
* Razorpay Payment Gateway
* Microservices Architecture
* REST APIs
* API Gateway Integration
* Maven
* Lombok

---

## Features Implemented

### Payment Creation via Kafka

* Payment requests are received from Order Service via Kafka
* Razorpay orders are created internally
* No REST API is exposed for payment creation

### Payment Verification

* Verifies Razorpay payment signature
* Confirms authenticity of payment response

### Asynchronous Payment Result

* Sends payment status back to Order Service via Kafka
* Enables non-blocking checkout flow

---

## API Endpoints

All endpoints are exposed through the **API Gateway** under the `/payment` path.

---

### Verify Payment

**POST** `/payment/verify`

This endpoint is called after Razorpay checkout to verify payment authenticity.

#### Request Parameters

```
razorpay_order_id     = order_LM12345
razorpay_payment_id  = pay_LM67890
razorpay_signature   = signature_here
```

#### Success Response

```
Payment verified successfully!
```

#### Failure Response

```
Payment verification failed: <error-message>
```

---

## Kafka Communication Flow

### Consumed Events

* RequestToPayment
  Contains amount, email, userId, and requestId
  Triggers Razorpay order creation

### Produced Events

* RazorpayOrderResponse
  Includes payment status (SUCCESS / FAILED)
  Sent back to Order Service

---

## Payment Processing Flow

```
Frontend
   ↓
API Gateway
   ↓
Order Service
   ↓ (Kafka)
Payment Service
   ↓
Razorpay
   ↓
Payment Verification (/payment/verify)
   ↓ (Kafka)
Order Service
```

---

## Future Enhancements

* Refund support
* Payment retry mechanism
* Multiple payment gateway support (Stripe, PayPal)
* Payment audit and transaction logs

---

## Author

Pranav Sharma
Spring Boot | Kafka | Payment Systems | Microservices

---
