# WebSocket Real-time Admin Notifications

## Overview
This feature sends real-time notifications to admin users whenever a new order is placed in the system using WebSocket/STOMP protocol.

## Implementation Details

### Components Added

1. **AdminOrderNotification** (`io/AdminOrderNotification.java`)
   - Lightweight DTO containing order summary data
   - Sent to admins over WebSocket (excludes sensitive PII)
   - Fields: orderId, totalAmountWithGST, paymentMode, status, orderDate

2. **OrderNotificationPublisher** (`util/OrderNotificationPublisher.java`)
   - Spring component that publishes order events to WebSocket
   - Uses `SimpMessagingTemplate` to send messages
   - Destination: `/topic/admin/orders`

3. **WebSocket Configuration** (`config/websocket/WebSocketConfig.java`)
   - Existing configuration already set up
   - Endpoint: `/ws` (with SockJS fallback)
   - Message broker: `/topic`
   - Application prefix: `/app`

4. **Security Configuration Updates** (`config/SecurityConfig.java`)
   - WebSocket handshake endpoint `/ws/**` is now permitted for all authenticated users
   - Note: Fine-grained destination security (admin-only subscriptions) requires `spring-security-messaging` dependency

### Integration Points

**BillingServiceImpl.createOrder()**
- After successfully saving a new order to MongoDB
- `orderNotificationPublisher.notifyAdminNewOrder(orderEntity)` is called
- Notification is sent immediately (order status = PENDING)

## How to Connect (Frontend)

### Using SockJS + STOMP.js

```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';

// 1. Establish WebSocket connection
const socket = new SockJS('http://localhost:8080/ws');
const stompClient = Stomp.over(socket);

// 2. Connect (Cookies are automatically sent if withCredentials is true or same-origin)
const headers = {}; 

stompClient.connect(headers, (frame) => {
  console.log('Connected: ' + frame);
  
  // 3. Subscribe to admin order notifications
  stompClient.subscribe('/topic/admin/orders', (message) => {
    const order = JSON.parse(message.body);
    console.log('New order received:', order);
    
    // Update admin dashboard UI
    showNotification(`New order ${order.orderId} - ₹${order.totalAmountWithGST}`);
  });
}, (error) => {
  console.error('STOMP error:', error);
});

// 4. Disconnect when done
function disconnect() {
  if (stompClient !== null) {
    stompClient.disconnect();
  }
  console.log("Disconnected");
}
```

### Example Notification Payload

```json
{
  "orderId": "FD24012945123",
  "totalAmountWithGST": 525.00,
  "paymentMode": "UPI",
  "status": "PENDING",
  "orderDate": "2026-01-29T19:45:30"
}
```

## Testing

### Manual Testing

1. Start the application: `./mvnw spring-boot:run`
2. Connect a WebSocket client to `ws://localhost:8080/ws`
3. Subscribe to `/topic/admin/orders`
4. Create a new order via REST API: `POST /api/v1/orders`
5. Observe the notification received on the WebSocket

### Using Browser Console

```javascript
// Quick test in browser console
const socket = new SockJS('http://localhost:8080/ws');
const client = Stomp.over(socket);

client.connect({}, () => {
  client.subscribe('/topic/admin/orders', (msg) => {
    console.log('ORDER:', JSON.parse(msg.body));
  });
});
```

## Security Considerations

### Current Implementation
- `/ws/**` endpoint is open to all authenticated users
- Any authenticated user can currently subscribe to `/topic/admin/orders`

### Recommended Enhancement
Add `spring-security-messaging` dependency to enable destination-level security:

```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-messaging</artifactId>
</dependency>
```

Then uncomment and complete `WebSocketSecurityConfig` to restrict admin topics:

```java
@Configuration
@EnableWebSocketSecurity
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {
    
    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
            .simpDestMatchers("/topic/admin/**").hasRole("ADMIN")
            .anyMessage().authenticated();
    }
}
```

## CORS Configuration
The WebSocket endpoint already allows connections from:
- `http://localhost:5174`
- `http://localhost:5173`
- `http://localhost:8081`
- `https://zaikabox.vercel.app`
- `https://zaikabox-audit-logs.vercel.app`

Update `WebSocketConfig.registerStompEndpoints()` to restrict origins in production.

## Troubleshooting

### Connection Refused
- Ensure Spring Boot application is running
- Check firewall/proxy settings
- Verify CORS settings match your frontend origin

### No Notifications Received
- Confirm subscription topic is exactly `/topic/admin/orders`
- Check server logs for order creation events
- Verify `OrderNotificationPublisher` bean is being injected correctly

### Authentication Issues
- Ensure JWT token is valid and not expired
- Check that the token is passed in the STOMP connect headers
- Verify `/ws/**` is permitted in SecurityConfig

## Admin Order Management Dashboard

### Overview
Admins can view recent orders and update their status in real-time. All status changes are instantly broadcasted to customers via WebSocket.

### Admin REST API Endpoints

#### Get Recent Orders
```
GET /api/v1/admin/orders/recent
Cookie: jwt=ADMIN_JWT_TOKEN
```

**Response (200 OK)**
```json
[
  {
    "orderId": "FD24012945123",
    "items": [
      {
        "foodId": "food-1",
        "name": "Butter Chicken",
        "quantity": 2,
        "unitPrice": 450.00,
        "total": 900.00
      }
    ],
    "subTotal": 900.00,
    "gstAmount": 45.00,
    "totalAmountWithGST": 945.00,
    "paymentMode": "UPI",
    "status": "PENDING",
    "orderDate": "2026-01-29T19:45:30",
    "firstName": "Rahul",
    "lastName": "Sharma",
    "email": "rahul@example.com",
    "address": "123, Main Street",
    "locality": "New Delhi"
  }
]
```

#### Get Orders by Status
```
GET /api/v1/admin/orders/status/{status}
Cookie: jwt=ADMIN_JWT_TOKEN
```
Possible status values: `PENDING`, `PROCESSING`, `DELIVERED`, `CANCELLED`

**Response (200 OK)**
```json
[
  {
    "orderId": "FD24012945123",
    "status": "PENDING",
    "totalAmountWithGST": 945.00,
    "firstName": "Rahul"
    // ... (same structure as above)
  }
]
```

#### Update Order Status
```
PUT /api/v1/admin/orders/{orderId}/status?status=PROCESSING
Cookie: jwt=ADMIN_JWT_TOKEN
```

**Response (200 OK)**
```json
{
  "orderId": "FD24012945123",
  "status": "PROCESSING",
  "totalAmountWithGST": 945.00,
  // ... (updated order details)
}
```

#### Get Order Statistics
```
GET /api/v1/admin/orders/stats/count
Cookie: jwt=ADMIN_JWT_TOKEN
```

**Response (200 OK)**
```json
{
  "pending": 5,
  "processing": 12,
  "delivered": 150,
  "cancelled": 3
}
```

### Real-time Status Update Flow

1. **Admin Updates Status** via PUT API
2. **Server Broadcasts to Customer** on `/topic/orders/{orderId}`
3. **Customer Receives Update** instantly via WebSocket

### Frontend Integration Examples

See detailed code examples in documentation below.

## Future Enhancements

1. ✅ Admin order dashboard with recent orders
2. ✅ Admin status update with real-time customer notification
3. Order assignment to specific kitchen staff
4. Kitchen preparation time estimation
5. Customer notification preferences
6. Delivery personnel tracking
7. Order refund processing
8. Notification history
9. Rate limiting for notifications
10. Bulk order operations
