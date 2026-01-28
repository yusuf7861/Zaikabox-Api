# ZaikaBox API Documentation

## Table of Contents
1. [Overview](#overview)
2. [Authentication APIs](#authentication-apis)
3. [Shopping Cart APIs](#shopping-cart-apis)
4. [Payment APIs](#payment-apis)
5. [Order APIs](#order-apis)
6. [Error Handling](#error-handling)
7. [Rate Limiting](#rate-limiting)
8. [Complete Flow Examples](#complete-flow-examples)

---

## Overview

**Project:** ZaikaBox - Online Food Ordering Platform  
**Base URL:** `http://localhost:8080/api/v1`  
**API Version:** 1.0  
**Last Updated:** January 2026

### Key Features
- User authentication with JWT tokens
- Shopping cart management
- Razorpay payment integration
- Order creation and tracking
- Real-time order status updates

---

## Authentication APIs

### 1. User Registration
**Endpoint:** `POST /auth/register`

**Description:** Register a new user account

**Request Body:**
```json
{
  "name": "string (min 3, max 20 characters)",
  "email": "string (unique, valid email format)",
  "password": "string (min 8, max 16 characters, must contain uppercase, lowercase, digit, and special character)"
}
```

**Password Requirements:**
- Minimum 8 characters, Maximum 16 characters
- At least one uppercase letter (A-Z)
- At least one lowercase letter (a-z)
- At least one digit (0-9)
- At least one special character (@$!%*?&)

**Example valid password:** `Pass@1234`

**Success Response (201 Created):**
```json
{
  "id": "string (UUID)",
  "name": "string",
  "email": "string",
  "role": "USER"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Email already exists",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "message": "Registration failed: {error details}",
  "status": "INTERNAL_SERVER_ERROR",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `201` - User registered successfully
- `400` - Invalid input or email already exists
- `500` - Server error

---

### 2. User Login
**Endpoint:** `POST /auth/login`

**Description:** Authenticate user and receive JWT token

**Authentication:** Not required

**Request Body:**
```json
{
  "email": "string (valid email format)",
  "password": "string (min 8, max 16 characters)"
}
```

**Success Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "email": "user@example.com"
}
```

**Headers:**
```
Set-Cookie: jwt=<token>; HttpOnly; Secure; SameSite=None; Max-Age=86400; Path=/
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid credentials",
  "status": "UNAUTHORIZED",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Login successful
- `401` - Invalid email or password
- `400` - Bad request

---

### 3. Check Authentication Status
**Endpoint:** `GET /auth/is-authenticated`

**Description:** Verify if current user is authenticated

**Authentication:** Optional (JWT Token)

**Success Response (200 OK):**
```json
{
  "isAuthenticated": true,
  "username": "user@example.com"
}
```

**Status Codes:**
- `200` - Status retrieved

---

### 4. User Logout
**Endpoint:** `POST /auth/logout`

**Description:** Logout the current user

**Authentication:** Required (JWT Token)

**Success Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

**Headers:**
```
Set-Cookie: jwt=; HttpOnly; Secure; SameSite=None; Max-Age=0; Path=/
```

**Status Codes:**
- `200` - Logout successful

---

## Shopping Cart APIs

### 1. Add Item to Cart
**Endpoint:** `POST /api/v1/carts/items/{foodId}`

**Description:** Add a product to the shopping cart

**Authentication:** Required (JWT Token)

**Path Parameters:**
- `foodId` (string): The unique food product ID

**Success Response (201 Created):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "items": {
    "foodId1": 2,
    "foodId2": 1
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Food item not found",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `201` - Item added successfully
- `401` - Unauthorized
- `404` - Food item not found
- `500` - Server error

---

### 2. View Cart
**Endpoint:** `GET /api/v1/carts`

**Description:** Retrieve the current user's shopping cart

**Authentication:** Required (JWT Token)

**Success Response (200 OK):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "items": {
    "foodId1": 2,
    "foodId2": 1
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Cart not found or is empty",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Cart retrieved successfully
- `401` - Unauthorized
- `404` - Cart not found
- `500` - Server error

---

### 3. Update Cart
**Endpoint:** `PUT /api/v1/carts`

**Description:** Update the shopping cart with new items and quantities

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "userId": "string (UUID)",
  "items": {
    "foodId1": 5,
    "foodId2": 2
  }
}
```

**Success Response (200 OK):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "items": {
    "foodId1": 5,
    "foodId2": 2
  }
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid cart data",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Cart updated successfully
- `400` - Invalid data
- `401` - Unauthorized
- `500` - Server error

---

### 4. Remove Item from Cart
**Endpoint:** `DELETE /api/v1/carts/items/{foodId}`

**Description:** Remove a product from the shopping cart

**Authentication:** Required (JWT Token)

**Path Parameters:**
- `foodId` (string): The unique food item ID

**Success Response (200 OK):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "items": {
    "foodId1": 2
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Item not found in cart",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Item removed successfully
- `401` - Unauthorized
- `404` - Item not found in cart
- `500` - Server error

---

### 5. Clear Cart
**Endpoint:** `DELETE /api/v1/carts`

**Description:** Clear all items from the shopping cart

**Authentication:** Required (JWT Token)

**Success Response (200 OK):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "items": {}
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Cart is already empty",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Cart cleared successfully
- `400` - Cart is empty
- `401` - Unauthorized
- `500` - Server error

---

### 6. Get Cart Item Count
**Endpoint:** `GET /api/v1/carts/count`

**Description:** Get the total number of items in the cart

**Authentication:** Required (JWT Token)

**Success Response (200 OK):**
```json
{
  "itemCount": 5
}
```

**Status Codes:**
- `200` - Count retrieved successfully
- `401` - Unauthorized
- `404` - Cart not found

---

## Payment APIs

### 1. Initiate Razorpay Payment
**Endpoint:** `POST /api/v1/payment/initiate`

**Description:** Create a Razorpay payment order for the cart items

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "amount": 5000,
  "currency": "INR",
  "receipt": "receipt#1",
  "notes": {
    "cartId": "string (UUID)",
    "userId": "string (UUID)"
  }
}
```

**Success Response (200 OK):**
```json
{
  "orderId": "order_1A2B3C4D5E6F",
  "amount": 5000,
  "amountDue": 5000,
  "currency": "INR",
  "status": "created",
  "keyId": "rzp_live_XXXXXXXXXXXXXXXX",
  "receipt": "receipt#1",
  "createdAt": "2026-01-28T10:45:00Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid amount or cart is empty",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp",
  "errorCode": "INVALID_AMOUNT"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "message": "Payment initialization failed: {error details}",
  "status": "INTERNAL_SERVER_ERROR",
  "timestamp": "timestamp",
  "errorCode": "PAYMENT_INIT_FAILED"
}
```

**Status Codes:**
- `200` - Payment order created successfully
- `400` - Invalid amount or cart is empty
- `401` - Unauthorized
- `500` - Payment initialization failed

**Important Notes:**
- Use the `orderId` in the Razorpay checkout
- The amount should be in paise (multiply by 100 if in rupees)
- Store the orderId for payment verification

---

### 2. Verify Razorpay Payment
**Endpoint:** `POST /api/v1/payment/verify`

**Description:** Verify the Razorpay payment signature and create the order

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "razorpayOrderId": "order_1A2B3C4D5E6F",
  "razorpayPaymentId": "pay_1A2B3C4D5E6F",
  "razorpaySignature": "9ef4dffbfd84f1318f6739a3ce19f9d85851857ae648f114332d8401e0949a3d",
  "orderId": "string (custom order ID)"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Payment verified successfully",
  "razorpayOrderId": "order_1A2B3C4D5E6F",
  "razorpayPaymentId": "pay_1A2B3C4D5E6F",
  "amount": 5000,
  "currency": "INR",
  "status": "COMPLETED",
  "orderId": "string (custom order ID, e.g., FD12345)",
  "customerId": "string (UUID)",
  "items": [
    {
      "name": "string",
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Payment verification failed",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp",
  "errorCode": "VERIFICATION_FAILED"
}
```

**Error Response (401 Unauthorized):**
```json
{
  "message": "Invalid signature. Payment verification failed",
  "status": "UNAUTHORIZED",
  "timestamp": "timestamp",
  "errorCode": "INVALID_SIGNATURE"
}
```

**Error Response (500 Internal Server Error):**
```json
{
  "message": "Error processing payment: {error details}",
  "status": "INTERNAL_SERVER_ERROR",
  "timestamp": "timestamp",
  "errorCode": "PAYMENT_PROCESSING_ERROR"
}
```

**Status Codes:**
- `200` - Payment verified and order created
- `400` - Verification failed
- `401` - Invalid signature
- `500` - Server error

**Important Notes:**
- Verify the signature on your backend (never rely on client-side verification)
- After successful verification, the cart is automatically cleared
- The user order is automatically created
- Audit logs are recorded for all payment transactions

---

### 3. Get Payment Status
**Endpoint:** `GET /api/v1/payment/{paymentId}`

**Description:** Retrieve payment status from Razorpay

**Authentication:** Required (JWT Token)

**Path Parameters:**
- `paymentId` (string): Razorpay Payment ID

**Success Response (200 OK):**
```json
{
  "paymentId": "pay_1A2B3C4D5E6F",
  "orderId": "order_1A2B3C4D5E6F",
  "amount": 5000,
  "currency": "INR",
  "status": "captured",
  "method": "card",
  "email": "user@example.com",
  "contact": "+91-9999999999",
  "description": "ZaikaBox Food Order",
  "fee": 118,
  "tax": 18,
  "acquirerData": {
    "auth_code": "100000"
  },
  "createdAt": "2026-01-28T10:50:00Z",
  "capturedAt": "2026-01-28T10:50:05Z"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Payment not found",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Payment status retrieved
- `401` - Unauthorized
- `404` - Payment not found

---

### 4. Retry Failed Payment
**Endpoint:** `POST /api/v1/payment/retry`

**Description:** Retry a failed payment

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "orderId": "string (UUID)",
  "amount": 5000
}
```

**Success Response (200 OK):**
```json
{
  "orderId": "order_1A2B3C4D5E7G",
  "amount": 5000,
  "currency": "INR",
  "status": "created",
  "keyId": "rzp_live_XXXXXXXXXXXXXXXX",
  "createdAt": "2026-01-28T11:00:00Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Cannot retry this payment. Order status is not valid for retry",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp",
  "errorCode": "INVALID_RETRY_STATUS"
}
```

**Status Codes:**
- `200` - New payment order created
- `400` - Cannot retry this payment
- `401` - Unauthorized
- `404` - Order not found

---

## Order APIs

### 1. Create Order (Post Payment)
**Endpoint:** `POST /api/v1/orders`

**Description:** Create a user order (automatically created after successful payment verification in most cases)

**Authentication:** Required (JWT Token)

**Request Body:**
```json
{
  "paymentMode": "RAZORPAY",
  "items": [
    {
      "foodId": "string",
      "quantity": 2
    }
  ],
  "useCart": true,
  "billingDetails": {
    "firstName": "John",
    "lastName": "Doe",
    "email": "user@example.com",
    "phoneNumber": "+91-9999999999",
    "address": "123 Main Street",
    "zip": "12345",
    "locality": "City",
    "landmark": "Near Park",
    "country": "India",
    "state": "State"
  }
}
```

**Success Response (201 Created):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "paymentId": "pay_1A2B3C4D5E6F",
  "razorpayOrderId": "order_1A2B3C4D5E6F",
  "items": [
    {
      "productId": "string (UUID)",
      "productName": "Biryani",
      "price": 250.00,
      "quantity": 2,
      "totalPrice": 500.00
    }
  ],
  "deliveryAddress": "123 Main Street, City, State 12345",
  "phoneNumber": "+91-9999999999",
  "specialInstructions": "Ring bell twice",
  "subtotal": 5000.00,
  "tax": 900.00,
  "deliveryCharges": 50.00,
  "totalAmount": 5950.00,
  "paymentStatus": "COMPLETED",
  "paymentMethod": "RAZORPAY",
  "orderStatus": "PENDING",
  "trackingId": "TRACK123456789",
  "createdAt": "2026-01-28T10:50:00Z",
  "estimatedDelivery": "2026-01-28T11:50:00Z"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Invalid payment or order creation failed",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Payment not found",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `201` - Order created successfully
- `400` - Invalid payment data
- `401` - Unauthorized
- `404` - Payment not found
- `500` - Server error

---

### 2. Get User Orders
**Endpoint:** `GET /api/v1/orders`

**Description:** Retrieve all orders for the authenticated user with pagination and filtering

**Authentication:** Required (JWT Token)

**Query Parameters (Optional):**
- `page` (integer): Page number (0-indexed, default: 0)
- `size` (integer): Page size (default: 10, max: 100)

**Example URL:**
```
GET /api/v1/orders?status=PENDING&page=0&size=10&sortBy=createdAt&sortDirection=DESC
```

**Success Response (200 OK):**
```json
{
  "content": [
    {
      "id": "string (UUID)",
      "userId": "string (UUID)",
      "paymentId": "pay_1A2B3C4D5E6F",
      "items": [
        {
          "productId": "string (UUID)",
          "productName": "Biryani",
          "price": 250.00,
          "quantity": 2,
          "totalPrice": 500.00
        }
      ],
      "deliveryAddress": "123 Main Street, City, State 12345",
      "phoneNumber": "+91-9999999999",
      "subtotal": 5000.00,
      "tax": 900.00,
      "deliveryCharges": 50.00,
      "totalAmount": 5950.00,
      "paymentStatus": "COMPLETED",
      "orderStatus": "PENDING",
      "trackingId": "TRACK123456789",
      "createdAt": "2026-01-28T10:50:00Z",
      "estimatedDelivery": "2026-01-28T11:50:00Z"
    }
  ],
  "pageNumber": 0,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "isFirst": true,
  "isLast": false
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "No orders found for this user",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Orders retrieved successfully
- `401` - Unauthorized
- `404` - No orders found
- `500` - Server error

---

### 3. Get Order Details
**Endpoint:** `GET /api/v1/orders/{orderId}`

**Description:** Retrieve detailed information about a specific order

**Authentication:** Required (JWT Token)

**Path Parameters:**
- `orderId` (string): The unique order ID (custom order ID or MongoDB ID)

**Success Response (200 OK):**
```json
{
  "id": "string (UUID)",
  "userId": "string (UUID)",
  "paymentId": "pay_1A2B3C4D5E6F",
  "razorpayOrderId": "order_1A2B3C4D5E6F",
  "items": [
    {
      "productId": "string (UUID)",
      "productName": "Biryani",
      "description": "Hyderabadi style biryani with raita",
      "price": 250.00,
      "quantity": 2,
      "totalPrice": 500.00,
      "image": "url"
    }
  ],
  "deliveryAddress": "123 Main Street, City, State 12345",
  "phoneNumber": "+91-9999999999",
  "specialInstructions": "Ring bell twice",
  "subtotal": 5000.00,
  "tax": 900.00,
  "deliveryCharges": 50.00,
  "totalAmount": 5950.00,
  "paymentStatus": "COMPLETED",
  "paymentMethod": "RAZORPAY",
  "orderStatus": "PROCESSING",
  "trackingId": "TRACK123456789",
  "createdAt": "2026-01-28T10:50:00Z",
  "estimatedDelivery": "2026-01-28T11:50:00Z",
  "deliveredAt": null
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Order not found",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Error Response (403 Forbidden):**
```json
{
  "message": "You are not authorized to access this order",
  "status": "FORBIDDEN",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Order details retrieved
- `401` - Unauthorized
- `403` - Forbidden (accessing another user's order)
- `404` - Order not found
- `500` - Server error

---

### 4. Track Order
**Endpoint:** `GET /api/v1/orders/{orderId}/track`

**Description:** Track the real-time status of an order with delivery updates

**Authentication:** Required (JWT Token)

**Path Parameters:**
- `orderId` (string): The unique order ID

**Success Response (200 OK):**
```json
{
  "orderId": "string (UUID)",
  "trackingId": "TRACK123456789",
  "orderStatus": "PROCESSING",
  "currentLocation": "Restaurant Kitchen",
  "estimatedDelivery": "2026-01-28T11:50:00Z",
  "deliveryPartner": {
    "name": "John Doe",
    "phoneNumber": "+91-9999988888",
    "vehicleNumber": "MH12AB1234",
    "rating": 4.8
  },
  "timeline": [
    {
      "status": "PENDING",
      "timestamp": "2026-01-28T10:50:00Z",
      "description": "Order confirmed",
      "location": "Restaurant"
    },
    {
      "status": "PROCESSING",
      "timestamp": "2026-01-28T10:55:00Z",
      "description": "Order is being prepared",
      "location": "Restaurant Kitchen"
    },
    {
      "status": "READY_FOR_PICKUP",
      "timestamp": "2026-01-28T11:15:00Z",
      "description": "Order ready for pickup",
      "location": "Restaurant"
    }
  ],
  "lastUpdated": "2026-01-28T11:15:00Z"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Tracking information not found",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Tracking information retrieved
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Order or tracking not found
- `500` - Server error

**Possible Order Statuses:**
- `PENDING` - Order placed, awaiting confirmation
- `PROCESSING` - Order is being prepared
- `DELIVERED` - Order delivered
- `CANCELLED` - Order cancelled
- `PAID` - Order paid

---

### 5. Cancel Order
**Endpoint:** `DELETE /api/v1/orders/{orderId}`

**Description:** Cancel an order (only if status is PENDING or PROCESSING)

**Authentication:** Required (JWT Token)

**Path Parameters:**
- `orderId` (string): The unique order ID

**Request Body:**
```json
{
  "reason": "string (e.g., 'Changed my mind', 'Emergency')"
}
```

**Success Response (200 OK):**
```json
{
  "success": true,
  "message": "Order cancelled successfully",
  "orderId": "string (UUID)",
  "refundStatus": "INITIATED",
  "refundAmount": 5950.00,
  "refundInitiatedAt": "2026-01-28T11:20:00Z",
  "estimatedRefundTime": "3-5 business days"
}
```

**Error Response (400 Bad Request):**
```json
{
  "message": "Order cannot be cancelled at this stage. Current status: COMPLETED",
  "status": "BAD_REQUEST",
  "timestamp": "timestamp"
}
```

**Error Response (404 Not Found):**
```json
{
  "message": "Order not found",
  "status": "NOT_FOUND",
  "timestamp": "timestamp"
}
```

**Status Codes:**
- `200` - Order cancelled successfully
- `400` - Order cannot be cancelled
- `401` - Unauthorized
- `403` - Forbidden
- `404` - Order not found
- `500` - Server error

---

## Request Headers

All API requests should include the following headers:

### Content-Type Header
```
Content-Type: application/json
```

### Authorization Header (for authenticated endpoints)
```
Authorization: Bearer <JWT_TOKEN>
```

### Example Request with Headers
```bash
curl -X POST "http://localhost:8080/api/v1/orders" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{...}'
```

---

## Input Validation Requirements

The API enforces strict validation on all inputs:

### User Registration Validation
- **name**: 
  - Minimum length: 3 characters
  - Maximum length: 20 characters
  - Cannot be blank
  
- **email**: 
  - Must be valid email format
  - Must be unique (not already registered)
  - Cannot be blank
  
- **password**: 
  - Minimum length: 8 characters
  - Maximum length: 16 characters
  - Must contain at least one uppercase letter (A-Z)
  - Must contain at least one lowercase letter (a-z)
  - Must contain at least one digit (0-9)
  - Must contain at least one special character (@$!%*?&)
  - Cannot be blank

### Login Validation
- **email**: Valid email format, cannot be blank
- **password**: 8-16 characters, cannot be blank

### Order Item Validation
- **foodId**: Must be a valid product ID
- **quantity**: Must be integer, minimum 1, maximum 100

### Billing Details Validation
- **firstName**: Cannot be blank, required for billing
- **lastName**: Cannot be blank, required for billing
- **email**: Valid email format, required
- **phoneNumber**: Valid phone format, required
- **address**: Cannot be blank, required
- **zip**: Cannot be blank, required
- **locality**: Cannot be blank, required
- **country**: Cannot be blank, required
- **state**: Cannot be blank, required
- **landmark**: Optional field

---

## Order Response Field Definitions

When you receive an order response from any endpoint, the following fields are included:

| Field | Type | Description |
|---|---|---|
| `orderId` | string | Custom order identifier (e.g., FD12345). Each order has a unique ID |
| `customerId` | string | UUID of the customer who placed the order |
| `items` | array | List of OrderItemResponse objects containing item details |
| `items[].name` | string | Name of the food item |
| `items[].quantity` | integer | Quantity ordered |
| `items[].unitPrice` | number | Price per unit of the item |
| `items[].total` | number | Total price for this item (quantity × unitPrice) |
| `subTotal` | number | Total price before tax and charges |
| `gstRate` | number | GST (Goods and Services Tax) rate in percentage (e.g., 18.0 for 18%) |
| `gstAmount` | number | Calculated GST amount on subtotal |
| `totalAmountWithGST` | number | Final total including GST (subTotal + gstAmount) |
| `paymentMode` | string | Payment method used (RAZORPAY, UPI, CARD, etc.) |
| `orderDate` | timestamp | Date and time when the order was placed |
| `status` | string | Current order status (PENDING, PROCESSING, DELIVERED, CANCELLED, PAID) |
| `paymentStatus` | string | Payment status (COMPLETED, PENDING, FAILED) |
| `paymentDate` | timestamp | Date and time when payment was processed |
| `razorpayOrderId` | string | Razorpay order ID for reference |
| `razorpayPaymentId` | string | Razorpay payment ID for reference |
| `firstName` | string | Customer's first name |
| `lastName` | string | Customer's last name |
| `email` | string | Customer's email address |
| `address` | string | Customer's delivery address |
| `zip` | string | Postal/ZIP code |
| `locality` | string | City or locality name |
| `landmark` | string | Nearby landmark for delivery |
| `country` | string | Country name |
| `state` | string | State/Province name |

---

## Cart Response Field Definitions

Cart responses contain the following fields:

| Field | Type | Description |
|---|---|---|
| `id` | string | Unique cart ID (MongoDB ObjectId) |
| `userId` | string | UUID of the user who owns the cart |
| `items` | object | Map of food IDs to quantities. Key: foodId, Value: quantity |

**Example items object:**
```json
{
  "items": {
    "food-id-1": 2,
    "food-id-2": 1,
    "food-id-3": 5
  }
}
```

---

## Payment Response Field Definitions

Payment-related responses contain the following fields:

| Field | Type | Description |
|---|---|---|
| `orderId` | string | Razorpay order ID created for this payment |
| `amount` | number | Payment amount in paise (for INR, multiply rupees by 100) |
| `amountDue` | number | Remaining amount due (if any) |
| `currency` | string | Currency code (e.g., INR) |
| `status` | string | Payment status (created, authorized, captured, refunded) |
| `keyId` | string | Razorpay API key ID for frontend integration |
| `receipt` | string | Receipt ID/reference for this payment |
| `createdAt` | timestamp | When the payment order was created |

---

## Error Response Field Definitions

All error responses follow this structure:

| Field | Type | Description |
|---|---|---|
| `message` | string | Human-readable error description |
| `status` | string | HTTP status code as string |
| `timestamp` | timestamp | When the error occurred |
| `errorCode` | string | Machine-readable error code (optional) |

---

## User Response Field Definitions

User responses contain the following fields:

| Field | Type | Description |
|---|---|---|
| `id` | string | Unique user ID (MongoDB ObjectId) |
| `name` | string | Full name of the user |
| `email` | string | User's email address (unique) |
| `role` | string | User role (USER, ADMIN, SELLER) |

---

## Authentication Response Field Definitions

Authentication/Login responses contain:

| Field | Type | Description |
|---|---|---|
| `token` | string | JWT token for authenticated requests |
| `email` | string | Email of the authenticated user |

---

## Billing Details Field Definitions

When providing billing details for orders:

| Field | Type | Required | Description |
|---|---|---|---|
| `firstName` | string | Yes | Customer's first name |
| `lastName` | string | Yes | Customer's last name |
| `email` | string | Yes | Valid email address |
| `phoneNumber` | string | Yes | Contact phone number |
| `address` | string | Yes | Street address |
| `zip` | string | Yes | Postal/ZIP code |
| `locality` | string | Yes | City or locality |
| `landmark` | string | No | Nearby landmark for delivery |
| `country` | string | Yes | Country name |
| `state` | string | Yes | State/Province name |

---

## Complete Field Type Reference

### Data Types Used in API

| Type | Format | Example |
|---|---|---|
| `string` | Text | "John Doe" |
| `integer` | Whole number | 5, 100 |
| `number` | Decimal number | 250.00, 18.5 |
| `boolean` | True/False | true, false |
| `timestamp` | ISO 8601 format | "2026-01-28T10:50:00Z" |
| `object` | JSON object | `{"key": "value"}` |
| `array` | JSON array | `[item1, item2]` |



### Standard Error Response Format
All error responses follow this format:

```json
{
  "message": "Descriptive error message",
  "status": "HTTP_STATUS_CODE",
  "timestamp": "2026-01-28T11:25:00Z",
  "errorCode": "ERROR_CODE (optional)"
}
```

### HTTP Status Codes Reference

| Status Code | Meaning | When to Use |
|---|---|---|
| 200 | OK | Successful GET, PUT, DELETE requests |
| 201 | Created | Successful POST requests creating a resource |
| 204 | No Content | Successful DELETE requests with no response body |
| 400 | Bad Request | Invalid input data or business logic error |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Authenticated but unauthorized to access resource |
| 404 | Not Found | Resource does not exist |
| 409 | Conflict | Resource conflict (e.g., email already exists) |
| 500 | Internal Server Error | Unexpected server-side error |

### Common Error Codes

| Error Code | Description | HTTP Status |
|---|---|---|
| `INVALID_CREDENTIALS` | Email or password is incorrect | 401 |
| `EMAIL_ALREADY_EXISTS` | Email is already registered | 400 |
| `INVALID_TOKEN` | JWT token is invalid or expired | 401 |
| `PRODUCT_NOT_FOUND` | Product does not exist | 404 |
| `CART_NOT_FOUND` | Cart does not exist | 404 |
| `ORDER_NOT_FOUND` | Order does not exist | 404 |
| `INVALID_PAYMENT` | Payment data is invalid | 400 |
| `PAYMENT_FAILED` | Payment processing failed | 500 |
| `INVALID_SIGNATURE` | Razorpay signature verification failed | 401 |
| `INVALID_QUANTITY` | Quantity is out of valid range | 400 |
| `UNAUTHORIZED_ACCESS` | User cannot access this resource | 403 |

---

## Rate Limiting

Rate limiting is implemented to prevent abuse:

| Endpoint Group | Limit | Window |
|---|---|---|
| Authentication | 10 requests | 1 minute |
| Shopping Cart | 100 requests | 1 minute |
| Payment | 50 requests | 1 minute |
| Orders | 100 requests | 1 minute |

**Response Headers:**
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1643378700
```

**Rate Limit Exceeded Response (429):**
```json
{
  "message": "Rate limit exceeded. Try again after 60 seconds",
  "status": "TOO_MANY_REQUESTS",
  "retryAfter": 60
}
```

---

## Authentication

### JWT Token
All authenticated endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Token Expiration
- **Access Token Lifetime:** 24 hours
- **Refresh Token Lifetime:** 7 days (if implemented)

### Cookie Authentication
JWT token is also set as an HTTP-only cookie:
```
Set-Cookie: jwt=<token>; HttpOnly; Secure; SameSite=None; Max-Age=86400; Path=/
```

---

## Complete Flow Examples

### Example 1: Complete Checkout Flow

#### Step 1: View Cart
```bash
curl -X GET "http://localhost:8080/api/v1/cart" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

#### Step 2: Initiate Payment
```bash
curl -X POST "http://localhost:8080/api/v1/payment/initiate" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 5950,
    "currency": "INR",
    "notes": {
      "cartId": "cart-uuid",
      "userId": "user-uuid"
    }
  }'
```

**Response:**
```json
{
  "orderId": "order_1A2B3C4D5E6F",
  "amount": 5950,
  "keyId": "rzp_live_XXXXXXXXXXXXXXXX"
}
```

#### Step 3: Display Razorpay Checkout (Frontend)
Use the `orderId` and `keyId` from the response to open Razorpay checkout

#### Step 4: Verify Payment
```bash
curl -X POST "http://localhost:8080/api/v1/payment/verify" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "razorpayOrderId": "order_1A2B3C4D5E6F",
    "razorpayPaymentId": "pay_1A2B3C4D5E6F",
    "razorpaySignature": "9ef4dffbfd84f1318f6739a3ce19f9d85851857ae648f114332d8401e0949a3d",
    "notes": {
      "deliveryAddress": "123 Main Street, City",
      "phoneNumber": "+91-9999999999"
    }
  }'
```

#### Step 5: View Order
```bash
curl -X GET "http://localhost:8080/api/v1/orders" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

#### Step 6: Track Order
```bash
curl -X GET "http://localhost:8080/api/v1/orders/{orderId}/track" \
  -H "Authorization: Bearer <JWT_TOKEN>"
```

---

### Example 2: Add Item to Cart

```bash
curl -X POST "http://localhost:8080/api/v1/cart/add" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "productId": "prod-uuid-123",
    "quantity": 2,
    "customizations": {
      "notes": "Less spicy",
      "specialRequests": "No onions"
    }
  }'
```

---

### Example 3: Update Quantity

```bash
curl -X PUT "http://localhost:8080/api/v1/cart/items/cartitem-uuid-123" \
  -H "Authorization: Bearer <JWT_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "quantity": 5
  }'
```

---

## Security Considerations

1. **Always use HTTPS** in production
2. **Never expose JWT tokens** in client-side logs
3. **Verify Razorpay signatures** server-side only
4. **Use secure cookies** for token storage
5. **Implement CSRF protection** for state-changing operations
6. **Validate all user inputs** server-side
7. **Use parameterized queries** to prevent SQL injection
8. **Implement API key rotation** for service-to-service communication
9. **Log all sensitive operations** for audit trails
10. **Use environment variables** for sensitive configuration

---

## Testing

### Using Postman

1. **Import Collection:** Import the provided Postman collection
2. **Set Environment Variables:**
   - `baseUrl`: `http://localhost:8080/api/v1`
   - `jwtToken`: (obtained from login response)
3. **Run Requests:** Execute requests in sequence

### Using cURL

Refer to the examples provided in the "Complete Flow Examples" section.

---

## Support & Contact

For API issues, bugs, or feature requests:

- **Email:** api-support@zaikabox.com
- **GitHub Issues:** https://github.com/zaikabox/api/issues
- **Documentation:** https://docs.zaikabox.com

---

## Changelog

### Version 1.0 (January 28, 2026)
- Initial API release
- Shopping Cart APIs
- Razorpay Payment Integration
- Order Management APIs
- Order Tracking APIs

---

## License

This API is licensed under the MIT License. See LICENSE file for details.

---

**Last Updated:** January 28, 2026  
**API Version:** 1.0  
**Status:** Production Ready
