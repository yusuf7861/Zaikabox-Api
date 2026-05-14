# Engineering Decisions

## 1) JWT + Role-Based Access
- Chosen for stateless API authentication and straightforward authorization boundaries (`CUSTOMER` vs `ADMIN`).
- Tradeoff: token revocation is non-trivial without additional persistence (mitigated with refresh token rotation).

## 2) MongoDB for Product + Order Workloads
- Chosen for flexible document modeling (orders, billing details, cart items).
- Tradeoff: relational constraints are application-enforced; tests and validation compensate.

## 3) Razorpay Verification-First Payment Flow
- Payment requests are persisted first; final order state is created only after signature verification/webhook capture.
- Benefit: protects against fake client-side success responses.

## 4) WebSocket for Real-Time Order Updates
- Admin and customer status updates are pushed to topics for low-latency UX.
- Tradeoff: increased operational complexity compared to pure polling.

## 5) Containerized Build + CI
- Docker + GitHub Actions chosen for reproducible builds and deployment portability.
- Quality gates (tests + coverage + static checks) are used to reduce regressions.
