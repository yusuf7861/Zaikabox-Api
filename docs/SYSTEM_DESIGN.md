# System Design Overview

## Scope
Zaikabox API powers user authentication, food catalog browsing, cart operations, order placement, payment verification, and admin order management.

## Key Components
- API Layer: Spring MVC controllers
- Domain/Application Layer: services for auth, billing, cart, user/admin workflows
- Persistence: MongoDB repositories
- Integrations: Razorpay, Azure Blob Storage, SMTP, Gemini API
- Real-time: WebSocket/STOMP order notifications

## Tradeoffs
- MongoDB flexibility vs relational integrity constraints.
- Stateless JWT performance vs revocation complexity.
- Real-time messaging UX gains vs added operational moving parts.

## Scale Limits (Current)
- Single service deployment with in-memory rate/idempotency controls.
- Horizontal scaling may require shared Redis-backed limits/keys.

## Future Improvements
- Redis-backed distributed idempotency and rate limiting
- Event bus (Kafka/RabbitMQ) for order lifecycle fan-out
- Blue/green deployment with canary verification
