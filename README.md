# Zaikabox API Architecture and Workflows

Zaikabox API is a comprehensive Spring Boot application that provides a backend service for food ordering platforms. Built using Java 17 and MongoDB, it implements a secure JWT-based authentication system and follows a clean architecture pattern.

## Core Features

### 1. User Management System

The user management system handles all aspects of user accounts, authentication, and authorization.

#### Features:
- User registration with validation
- JWT-based secure authentication
- Role-based authorization (USER, ADMIN)
- Password reset functionality with OTP verification
- User profile management
- Session management with logout functionality
- Admin panel for user management

#### User Flow:
```
Registration → Email Verification → Login → Access Protected Resources
```

### 2. Food Management System

The food management system provides functionality for creating, retrieving, updating, and deleting food items in the catalog.

#### Features:
- Add new food items with images
- Retrieve food items individually or as a collection
- Update food item details
- Remove food items from the catalog
- Image storage and retrieval

#### Food Management Flow:
```
Admin Login → Add Food Item → Upload Image → Publish to Catalog
```

### 3. Shopping Cart System

The shopping cart system allows users to add, update, and remove food items from their cart before placing an order.

#### Features:
- Add items to cart
- Remove items from cart
- Update item quantities
- View cart contents
- Clear entire cart

#### Cart Flow:
```
Browse Foods → Add to Cart → Update Quantities → Proceed to Checkout
```

### 4. Order Management System

The order management system handles the creation, tracking, and management of food orders.

#### Features:
- Create new orders from cart items
- Track order status (PENDING, PROCESSING, DELIVERED, CANCELLED)
- View order history
- Update order status (admin functionality)
- Order cancellation

#### Order Flow:
```
Cart Checkout → Order Creation → Order Processing → Order Delivery
```

### 5. Billing System

The billing system generates and manages billing information for orders.

#### Features:
- Generate PDF bills for orders
- Generate text format bills
- Store billing details
- Associate billing information with orders

#### Billing Flow:
```
Order Confirmation → Billing Details Collection → Bill Generation
```

### 6. Contact Support System

The contact support system allows users to send enquiries and feedback to administrators.

#### Features:
- Submit contact form with user details and message
- Email notification to administrators

#### Contact Flow:
```
Fill Contact Form → Submit → Email Sent to Support
```

## Technical Features

### 1. Security

- JWT-based authentication
- Password encryption
- Role-based access control
- Cross-Origin Resource Sharing (CORS) configuration
- Secure HTTP headers

### 2. API Documentation

- OpenAPI/Swagger documentation
- Detailed endpoint descriptions
- Request/response schema definitions

### 3. Containerization

- Docker support for easy deployment
- Container orchestration readiness

## System Architecture

Zaikabox API follows the standard Spring Boot architecture with the following components:

### Layers:
1. **Controllers** - Handle HTTP requests and responses
2. **Services** - Implement business logic
3. **Repositories** - Data access layer for MongoDB interaction
4. **Entities** - Domain models representing database documents
5. **DTOs (Data Transfer Objects)** - Request/response objects
6. **Filters** - JWT authentication and request filtering
7. **Configuration** - Application configuration classes
8. **Utilities** - Helper classes for common functionality

### Data Flow:
```
Client Request → JWT Filter → Controller → Service → Repository → Database
                                   ↓
Client Response ← Controller ← Service ← Repository ← Database
```

## Database Schema

The application uses MongoDB with the following main collections:

1. **Users** - Store user account information
2. **Foods** - Food catalog items
3. **Carts** - User shopping carts
4. **Orders** - User orders
5. **BillingDetails** - Order billing information

## Integration Points

1. **Payment Gateways** - Ready for integration with payment processors
2. **Email Services** - For notifications and password resets
3. **Google Gemini AI** - For AI-powered features
4. **Azure Cloud Services** - For deployment and scaling
5. **Image Storage Services** - For food item images


## System Architecture

The Zaikabox API is built using a layered architecture pattern with Spring Boot, following industry best practices for separation of concerns and maintainability.

### Architectural Layers

```
┌─────────────────────────────────────────────────────┐
│                  Client Applications                │
└───────────────────────┬─────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│                  JWT Filter Layer                   │
└───────────────────────┬─────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│               Controller Layer (REST)               │
├─────────────────────────────────────────────────────┤
│  UserController │ FoodController │ BillingController│
│  CartController │ ContactController │ GeminiController│
└───────────────────────┬─────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│                   Service Layer                     │
├─────────────────────────────────────────────────────┤
│  UserService │ FoodService │ BillingService        │
│  CartService │ ContactService │ GeminiService      │
└───────────────────────┬─────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│                Repository Layer                     │
└───────────────────────┬─────────────────────────────┘
                        │
                        ▼
┌─────────────────────────────────────────────────────┐
│                   MongoDB Database                  │
├─────────────────────────────────────────────────────┤
│  Users │ Foods │ Carts │ Orders │ BillingDetails   │
└─────────────────────────────────────────────────────┘
```

## Technology Stack
![Technology Stack](https://i.postimg.cc/vmZdYx6v/Tech-Stack.png)

## Key Workflows

### 1. User Authentication Flow
![User Authentication Flow](https://i.postimg.cc/63x9Xb0g/User-Auth-flow.png)

### 2. Order Creation Flow
![Order Creation Flow](https://i.postimg.cc/W1tp6kzW/Order-Creation-Flow.png)

## Authentication and Authorization Process
![Authentication and Authorization Process](https://i.postimg.cc/7hVPX1Sc/Authentication-Authorization-Process02.png)

### 3. Food Management Flow
![Food Management Flow](https://i.postimg.cc/KvG8jV2y/Food-Management-Flow.png)

### 4. User Registration Flow
![User-Registration-Flow](https://i.postimg.cc/xdz0BVKL/User-Registration-Flow.png)

### 5. Cart Operations Flow
![Cart Operations Flow](https://i.postimg.cc/L5R6zQgL/Cart-Operations-Flow.png)

## Security Architecture
![Security Architecture](https://i.postimg.cc/XYhj2YyV/Security-Architecture.png)

## Database Schema

```
┌───────────────────────┐       ┌───────────────────────┐
│      UserEntity       │       │      FoodEntity       │
├───────────────────────┤       ├───────────────────────┤
│ id: String            │       │ id: String            │
│ name: String          │       │ name: String          │
│ email: String         │       │ description: String   │
│ password: String      │       │ price: Double         │
│ role: Role            │       │ imageUrl: String      │
└─────────┬─────────────┘       └───────────┬───────────┘
          │                                 │
          │                                 │
          │                                 │
          ▼                                 ▼
┌───────────────────────┐       ┌───────────────────────┐
│      CartEntity       │       │     OrderEntity       │
├───────────────────────┤       ├───────────────────────┤
│ id: String            │       │ id: String            │
│ userId: String        │       │ userId: String        │
│ items: Map<Food,Int>  │◄─────►│ items: List<OrderItem>│
└───────────────────────┘       │ status: Status        │
                                │ orderDate: Date       │
                                │ totalAmount: Double   │
                                └─────────┬─────────────┘
                                          │
                                          │
                                          ▼
                                ┌───────────────────────┐
                                │   BillingDetails      │
                                ├───────────────────────┤
                                │ id: String            │
                                │ orderId: String       │
                                │ paymentMethod: String │
                                │ address: String       │
                                │ contactNumber: String │
                                └───────────────────────┘
```

## API Request-Response Flow
![API Request-Response Flow](https://i.postimg.cc/DZ1w4307/API-Request-Response-Flow.png)

## Deployment Architecture
![Deployment Architecture](https://i.postimg.cc/HW4LpmF2/Deployment-Architecture.png)

## Integration Architecture

```
┌───────────────────────────────────────────────────────────────┐
│                       Zaikabox API                            │
└───────┬─────────────────┬──────────────────┬─────────┬────────┘
        │                 │                  │         │
        ▼                 ▼                  ▼         ▼
┌───────────────┐  ┌────────────┐  ┌──────────────┐  ┌─────────┐
│               │  │            │  │              │  │         │
│Payment Gateway│  │Email Service│  │Storage Service│  │Gemini AI│
│               │  │            │  │              │  │         │
└───────────────┘  └────────────┘  └──────────────┘  └─────────┘
```

## Razorpay Payment & Order APIs (new)

- **Flow overview**: (1) Auth user calls `POST /api/v1/orders` to get `razorpay_order_id` + amount; (2) Frontend opens Razorpay Checkout with that order id and collects `razorpay_payment_id` and `razorpay_signature`; (3) Frontend calls `POST /api/v1/orders/verify-payment` to mark the order paid; (4) User views/tracks via GET endpoints below.
- **Auth**: All order endpoints require JWT except `/api/v1/orders/verify-payment` (open for Razorpay callback from frontend). Admin-only routes are unchanged (`DELETE /api/v1/orders/{orderId}`, `PUT /api/v1/orders/{orderId}`).

### Create order
- `POST /api/v1/orders`
- Body:
  ```json
  {
    "paymentMode": "UPI|CARD|COD",
    "useCart": true,
    "items": [{"foodId": "string", "quantity": 2}],
    "billingDetails": {"firstName": "", "lastName": "", "email": "", "address": "", "locality": "", "landmark": "", "zip": "", "country": "", "state": ""}
  }
  ```
- Response: order with `orderId`, totals, `status` (PENDING), `razorpayOrderId`, `paymentStatus`, item lines, billing fields.

### Verify payment (Razorpay)
- `POST /api/v1/orders/verify-payment`
- Body:
  ```json
  {
    "razorpayPaymentId": "pay_xxx",
    "razorpayOrderId": "order_xxx",
    "razorpaySignature": "signature",
    "orderId": "FD..." // optional helper
  }
  ```
- Response: updated order with `status`=PAID, `paymentStatus`="paid", `razorpayPaymentId`, `paymentDate`.

### Track / fetch orders (JWT)
- `GET /api/v1/orders` → list current user orders.
- `GET /api/v1/orders/{orderId}` → order detail.
- `GET /api/v1/orders/{orderId}/track` → same as detail for tracking.
- `GET /api/v1/orders/status/{status}` → filter by `PENDING|PROCESSING|PAID|DELIVERED|CANCELLED`.

### Order schema (response keys)
- Core: `orderId`, `customerId`, `items[{name,quantity,unitPrice,total}]`, `subTotal`, `gstRate`, `gstAmount`, `totalAmountWithGST`, `paymentMode`, `orderDate`, `status`.
- Payment: `razorpayOrderId`, `razorpayPaymentId`, `paymentStatus`, `paymentDate`.
- Billing: `firstName`, `lastName`, `email`, `address`, `locality`, `landmark`, `zip`, `country`, `state`.
