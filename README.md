# CineTix — Online Movie Ticketing Platform

> Nền tảng đặt vé xem phim trực tuyến theo kiến trúc Microservices, tương tự CGV/Galaxy/BHD.  
> Cho phép người dùng duyệt phim, chọn rạp, chọn ghế real-time, thanh toán, nhận vé QR và chat với support.

---

## Mục lục

- [Tổng quan](#tổng-quan)
- [Tech Stack](#tech-stack)
- [Kiến trúc hệ thống](#kiến-trúc-hệ-thống)
- [Danh sách Microservices](#danh-sách-microservices)
- [Luồng hệ thống chi tiết](#luồng-hệ-thống-chi-tiết)
  - [Luồng đặt vé (Booking Saga)](#luồng-đặt-vé-booking-saga)
  - [Luồng xác thực (Auth Flow)](#luồng-xác-thực-auth-flow)
  - [Luồng real-time chọn ghế](#luồng-real-time-chọn-ghế)
  - [Luồng Event-Driven (Outbox → RabbitMQ)](#luồng-event-driven-outbox--rabbitmq)
  - [Luồng bù trừ lỗi (Compensation Flow)](#luồng-bù-trừ-lỗi-compensation-flow)
- [Cơ sở dữ liệu](#cơ-sở-dữ-liệu)
- [API Endpoints](#api-endpoints)
- [Cài đặt & Chạy local](#cài-đặt--chạy-local)
- [Biến môi trường](#biến-môi-trường)
- [Cấu trúc thư mục](#cấu-trúc-thư-mục)

---

## Tổng quan

**CineTix** là hệ thống đặt vé xem phim online end-to-end được xây dựng theo kiến trúc **Microservices** với **11 services** độc lập. Hệ thống áp dụng các pattern hiện đại:

| Pattern | Áp dụng tại |
|---------|------------|
| **Saga Orchestration** | Booking flow (hold seats → payment → confirm) |
| **Transactional Outbox** | Đảm bảo event publishing durability |
| **CQRS** | Booking Service (write model tách read model) |
| **Database per Service** | Mỗi service có PostgreSQL riêng |
| **Circuit Breaker + Retry** | gRPC calls tới Showtime, Payment |
| **Distributed Lock (Redis)** | Seat hold với TTL 10 phút |
| **DDD Tactical Patterns** | Aggregate, Entity, Value Object, Domain Event |

---

## Tech Stack

### Backend
| Layer | Technology |
|-------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.3.4, Spring Cloud 2023.0.3 |
| Build | Maven 3 |
| Sync IPC | gRPC (grpc-spring-boot-starter) |
| Async IPC | RabbitMQ 3.13 (topic exchange) |
| API Gateway | Spring Cloud Gateway (REST/HTTP) |
| Real-time | WebSocket STOMP over SockJS |

### Data
| Layer | Technology |
|-------|-----------|
| Database | PostgreSQL 16 (database-per-service) |
| Cache / Lock | Redis 7 |
| Migration | Flyway |
| Resilience | Resilience4j (Circuit Breaker, Retry, Bulkhead) |

### Security
| Component | Technology |
|-----------|-----------|
| Identity Provider | Keycloak 25 (OAuth2 / OIDC) |
| Token | JWT RS256 |
| Service Auth | Spring Security Resource Server |

### Frontend
| Layer | Technology |
|-------|-----------|
| Framework | Vue 3 + Vite |
| State | Pinia |
| HTTP | Axios + JWT interceptor |
| Auth | keycloak-js |
| Styles | Tailwind CSS |
| Real-time | STOMP client (WebSocket) |

### Infrastructure
| Component | Technology |
|-----------|-----------|
| Containers | Docker + Docker Compose |
| Orchestration | Kubernetes + Helm |
| GitOps | ArgoCD |
| Monitoring | Prometheus + Grafana |
| Ingress | NGINX Ingress Controller |

---

## Kiến trúc hệ thống

```mermaid
graph TB
    subgraph CLIENT["Client Layer"]
        B["🌐 Browser\n(Vue 3 + Vite)\nPort: 3000"]
    end

    subgraph GATEWAY["API Gateway Layer"]
        GW["🔀 API Gateway\n(Spring Cloud Gateway)\nPort: 8080\n• JWT Validation\n• Rate Limiting 50 req/s\n• CORS\n• Routing"]
    end

    subgraph AUTH["Identity & Auth"]
        KC["🔐 Keycloak\n(OAuth2/OIDC)\nPort: 8180\nRealm: cinetix"]
    end

    subgraph SERVICES["Microservices Layer"]
        direction TB
        IS["👤 Identity Service\nPort: 8081\nDB: identity_db"]
        MS["🎬 Movie Service\nPort: 8084\nDB: movie_db\n+ Redis Cache"]
        CS["🏟️ Cinema Service\nPort: 8085\nDB: cinema_db"]
        SS["🪑 Showtime Service\nPort: 8083\nDB: showtime_db\n+ Redis Seat Hold\ngRPC: 9083"]
        BS["📋 Booking Service\nPort: 8082\nDB: booking_db\ngRPC: 9082\n[SAGA Orchestrator]"]
        PS["💳 Payment Service\nPort: 8086\nDB: payment_db\ngRPC: 9086"]
        PROS["🎟️ Promotion Service\nPort: 8088\nDB: promo_db\ngRPC: 9088"]
        TS["🎫 Ticket Service\nPort: 8087\nDB: ticket_db"]
        NS["📧 Notification Service\nPort: 8089\nDB: notif_db"]
        CHS["💬 Chat Service\nPort: 8090\nDB: chat_db"]
    end

    subgraph INFRA["Infrastructure Layer"]
        RMQ["🐇 RabbitMQ\nTopic Exchange: cinetix.events\nPort: 5672"]
        REDIS["🔴 Redis\nCache + Seat Hold TTL\nDistributed Lock\nPort: 6379"]
        PG["🐘 PostgreSQL 16\n11 Databases\nPort: 5432"]
    end

    subgraph OBS["Observability"]
        PROM["📊 Prometheus\nMetrics Collection"]
        GRAF["📈 Grafana\nDashboards\nPort: 3001"]
    end

    B -- "HTTPS REST /api/v1/*" --> GW
    B -- "OAuth2 PKCE Login" --> KC
    B -- "WebSocket STOMP\n/ws/showtime/{id}" --> GW
    B -- "WebSocket STOMP\n/ws/chat/{id}" --> GW

    GW -- "JWT Validation" --> KC
    GW -- "Route /api/v1/users/*" --> IS
    GW -- "Route /api/v1/movies/*" --> MS
    GW -- "Route /api/v1/cinemas/*" --> CS
    GW -- "Route /api/v1/showtimes/*" --> SS
    GW -- "Route /api/v1/bookings/*" --> BS
    GW -- "Route /api/v1/payments/*" --> PS
    GW -- "Route /api/v1/promotions/*" --> PROS
    GW -- "Route /api/v1/tickets/*" --> TS
    GW -- "Route /ws/*" --> SS
    GW -- "Route /ws/chat/*" --> CHS

    BS -- "gRPC: HoldSeats\nReleaseSeats\nConfirmSeats" --> SS
    BS -- "gRPC: InitiatePayment\nRefundPayment\nCancelPayment" --> PS
    BS -- "gRPC: ValidateVoucher\nRedeemVoucher\nVoidVoucher" --> PROS

    BS -- "Publish Events\n(Outbox Pattern)" --> RMQ
    PS -- "Publish Events\n(Outbox Pattern)" --> RMQ
    SS -- "Publish Events\n(Outbox Pattern)" --> RMQ

    RMQ -- "BookingConfirmedEvent" --> TS
    RMQ -- "BookingConfirmed/Failed\nPaymentCompleted/Failed" --> NS
    RMQ -- "PaymentCompletedEvent\nPaymentFailedEvent" --> BS
    RMQ -- "SeatsHeld/Released" --> CHS

    SS -- "Seat Hold TTL\nDistributed Lock" --> REDIS
    MS -- "Movie Cache" --> REDIS
    GW -- "Rate Limit Counter" --> REDIS

    IS --> PG
    MS --> PG
    CS --> PG
    SS --> PG
    BS --> PG
    PS --> PG
    PROS --> PG
    TS --> PG
    NS --> PG
    CHS --> PG

    PROM -- "Scrape /actuator/prometheus" --> SERVICES
    GRAF --> PROM

    style CLIENT fill:#e3f2fd
    style GATEWAY fill:#fff3e0
    style AUTH fill:#fce4ec
    style SERVICES fill:#e8f5e9
    style INFRA fill:#f3e5f5
    style OBS fill:#fff8e1
```

---

## Danh sách Microservices

| Service | Port REST | Port gRPC | Database | Vai trò |
|---------|-----------|-----------|----------|---------|
| **api-gateway** | 8080 | — | Redis | Định tuyến, Rate Limit, JWT validate |
| **identity-service** | 8081 | — | identity_db | Hồ sơ người dùng, sync từ Keycloak JWT |
| **movie-service** | 8084 | — | movie_db | Danh mục phim, Redis cache |
| **cinema-service** | 8085 | — | cinema_db | Rạp, phòng chiếu, sơ đồ ghế |
| **showtime-service** | 8083 | 9083 | showtime_db | Suất chiếu, seat hold Redis, WebSocket |
| **booking-service** | 8082 | 9082 | booking_db | **Saga Orchestrator**, CQRS, Outbox |
| **payment-service** | 8086 | 9086 | payment_db | Thanh toán, refund, PSP webhook |
| **promotion-service** | 8088 | 9088 | promo_db | Khuyến mãi, voucher, mã giảm giá |
| **ticket-service** | 8087 | — | ticket_db | Tạo vé QR, validate tại rạp |
| **notification-service** | 8089 | — | notif_db | Email xác nhận đặt vé, receipt |
| **chat-service** | 8099 | — | chat_db | Chat real-time khách hàng & support |

---

## Luồng hệ thống chi tiết

### Luồng đặt vé (Booking Saga)

> Đây là luồng phức tạp nhất, áp dụng **Saga Orchestration Pattern** với cơ chế compensation đầy đủ.

```mermaid
sequenceDiagram
    actor User as 👤 User
    participant FE as 🌐 Frontend
    participant GW as 🔀 API Gateway
    participant BS as 📋 Booking Service
    participant SS as 🪑 Showtime Service
    participant PROS as 🎟️ Promotion Service
    participant PS as 💳 Payment Service
    participant RMQ as 🐇 RabbitMQ
    participant TS as 🎫 Ticket Service
    participant NS as 📧 Notification Service
    participant PSP as 🏦 PSP (Payment Gateway)
    participant REDIS as 🔴 Redis

    Note over User,REDIS: ═══ PHASE 1: KHỞI TẠO ĐẶT VÉ ═══

    User->>FE: Chọn phim → rạp → suất chiếu → ghế → nhập voucher
    FE->>GW: POST /api/v1/bookings\n{showtimeId, seatIds[], voucherCode?}
    GW->>BS: Route + JWT validation

    Note over BS: Tạo Booking aggregate\nStatus: INITIATED\nExpires: now + 10 phút

    Note over User,REDIS: ═══ PHASE 2: GIỮ GHẾ ═══

    BS->>SS: gRPC: HoldSeats(seatIds, TTL=600s)
    SS->>REDIS: SET showtime:{id}:seat:{id}:hold = bookingId (TTL 600s)
    REDIS-->>SS: OK
    SS-->>BS: SeatsHeld response
    Note over BS: Saga step: HOLD_SEATS ✅\nBooking Status: SEATS_HELD

    Note over User,REDIS: ═══ PHASE 3: ÁP DỤNG VOUCHER (tuỳ chọn) ═══

    alt Có voucherCode
        BS->>PROS: gRPC: ValidateVoucher(code, bookingContext)
        PROS-->>BS: VoucherValid + discountAmount
        Note over BS: Tính finalAmount = subtotal - discount\nBooking Status: VOUCHER_APPLIED
    end

    Note over User,REDIS: ═══ PHASE 4: KHỞI TẠO THANH TOÁN ═══

    BS->>PS: gRPC: InitiatePayment(bookingId, amount, method)
    PS-->>BS: {paymentId, paymentUrl}
    Note over BS: Lưu paymentId\nBooking Status: PAYMENT_PENDING

    BS-->>GW: 201 Created {bookingId, paymentUrl}
    GW-->>FE: Response
    FE->>User: Chuyển hướng tới paymentUrl

    Note over User,REDIS: ═══ PHASE 5: USER THANH TOÁN ═══

    User->>PSP: Hoàn tất thanh toán (VNPay/Momo/Visa)
    PSP->>PS: Webhook: POST /api/v1/payments/{id}/confirm
    Note over PS: Payment Status: COMPLETED
    PS->>PS: Outbox: PaymentCompletedEvent
    PS-->>PSP: 200 OK

    Note over User,REDIS: ═══ PHASE 6: XỬ LÝ SAU THANH TOÁN (ASYNC) ═══

    PS-->>RMQ: Publish PaymentCompletedEvent\nRouting: payment.events.completed
    RMQ->>BS: Deliver PaymentCompletedEvent
    
    BS->>SS: gRPC: ConfirmSeats(seatIds, bookingId)
    SS->>REDIS: DEL seat hold keys
    SS-->>BS: SeatsConfirmed

    Note over BS: Booking Status: CONFIRMED
    BS->>BS: Outbox: BookingConfirmedEvent

    BS-->>RMQ: Publish BookingConfirmedEvent\nRouting: booking.events.confirmed

    Note over User,REDIS: ═══ PHASE 7: PHÁT SINH VÉ & THÔNG BÁO (ASYNC) ═══

    RMQ->>TS: Deliver BookingConfirmedEvent
    Note over TS: Tạo Ticket records\n+ Sinh QR code cho mỗi ghế\nStatus: ACTIVE

    RMQ->>NS: Deliver BookingConfirmedEvent
    NS->>User: 📧 Email xác nhận đặt vé + QR vé

    Note over User,REDIS: ═══ PHASE 8: SỬ DỤNG VÉ ═══

    User->>GW: GET /api/v1/tickets/my
    GW->>TS: Route
    TS-->>User: Danh sách vé + QR codes

    User->>GW: POST /api/v1/tickets/{id}/validate (quét QR tại rạp)
    GW->>TS: Route
    Note over TS: Ticket Status: ACTIVE → USED\nGhi nhận used_at timestamp
    TS-->>User: ✅ Vào rạp OK
```

---

### Luồng xác thực (Auth Flow)

```mermaid
sequenceDiagram
    actor User as 👤 User
    participant FE as 🌐 Frontend (Vue 3)
    participant KC as 🔐 Keycloak
    participant GW as 🔀 API Gateway
    participant IS as 👤 Identity Service
    participant SVC as ⚙️ Any Microservice

    Note over User,SVC: ═══ ĐĂNG NHẬP LẦN ĐẦU ═══

    User->>FE: Click "Đăng nhập"
    FE->>KC: Redirect OAuth2 Authorization\n(PKCE flow, realm=cinetix)
    User->>KC: Nhập username/password
    KC-->>FE: Authorization Code
    FE->>KC: Exchange Code → Tokens\n(access_token, refresh_token, id_token)
    Note over FE: Lưu tokens trong memory\n(Pinia auth store)\nAuto-refresh mỗi 30s

    Note over User,SVC: ═══ GỌI API ═══

    FE->>GW: HTTP Request\nHeader: Authorization: Bearer {JWT}
    GW->>KC: Validate JWT signature\n(RS256, JWKS endpoint)
    KC-->>GW: Token valid + claims
    GW->>SVC: Forward request + JWT
    SVC->>SVC: Spring Security: extract user_id\nfrom JWT sub claim

    alt Lần đầu truy cập Identity Service
        SVC->>IS: GET /api/v1/users/me
        Note over IS: User chưa có profile\nAuto-create từ JWT claims\n(email, full_name)
        IS-->>SVC: User profile created
    end

    SVC-->>FE: Response data

    Note over User,SVC: ═══ TOKEN HẾT HẠN ═══

    FE->>KC: POST /realms/cinetix/protocol/openid-connect/token\n(grant_type=refresh_token)
    KC-->>FE: New access_token
    Note over FE: Axios interceptor tự động\nretry request với token mới
```

---

### Luồng real-time chọn ghế

```mermaid
sequenceDiagram
    actor UserA as 👤 User A
    actor UserB as 👤 User B
    participant FE_A as 🌐 Frontend A
    participant FE_B as 🌐 Frontend B
    participant GW as 🔀 API Gateway
    participant SS as 🪑 Showtime Service
    participant REDIS as 🔴 Redis
    participant RMQ as 🐇 RabbitMQ

    Note over UserA,RMQ: ═══ KHỞI TẠO KẾT NỐI WEBSOCKET ═══

    FE_A->>GW: WebSocket STOMP\n/ws/showtime/{showtimeId}
    FE_B->>GW: WebSocket STOMP\n/ws/showtime/{showtimeId}
    GW->>SS: Forward WebSocket connections
    Note over SS: 2 clients subscribe\n/topic/showtime/{id}/seats

    Note over UserA,RMQ: ═══ TẢI SƠ ĐỒ GHẾ ═══

    FE_A->>GW: GET /api/v1/showtimes/{id}/seats
    GW->>SS: Route request
    SS-->>FE_A: Seat map:\nAVAILABLE / HELD / BOOKED / UNAVAILABLE

    Note over UserA,RMQ: ═══ USER A CHỌN GHẾ → ĐẶT VÉ ═══

    UserA->>FE_A: Chọn ghế A1, A2
    FE_A->>GW: POST /api/v1/bookings\n{seatIds: [A1, A2]}
    GW->>SS: gRPC: HoldSeats([A1, A2], bookingId)
    SS->>REDIS: SET seat:A1:hold = bookingId (TTL 600s)\nSET seat:A2:hold = bookingId (TTL 600s)
    
    Note over SS: Publish SeatStatusChangedEvent
    SS->>RMQ: Publish: seats A1, A2 → HELD
    RMQ->>SS: Broadcast via WebSocket
    
    SS-->>FE_A: STOMP push: {A1: HELD, A2: HELD}
    SS-->>FE_B: STOMP push: {A1: HELD, A2: HELD}
    
    Note over FE_B: Ghế A1, A2 hiển thị màu vàng\n(đang được giữ bởi người khác)

    Note over UserA,RMQ: ═══ GHẾ HẾT TTL (10 PHÚT) ═══

    Note over SS: BookingExpiryJob chạy mỗi 1 phút\nKiểm tra booking hết hạn
    SS->>REDIS: DEL seat:A1:hold, seat:A2:hold
    SS->>RMQ: Publish: seats A1, A2 → AVAILABLE
    RMQ->>SS: Broadcast
    SS-->>FE_A: STOMP push: {A1: AVAILABLE, A2: AVAILABLE}
    SS-->>FE_B: STOMP push: {A1: AVAILABLE, A2: AVAILABLE}
```

---

### Luồng Event-Driven (Outbox → RabbitMQ)

```mermaid
flowchart TD
    subgraph BS_TX["Booking Service - Database Transaction"]
        direction LR
        AGG["📦 Booking Aggregate\n.confirm()"] -->|"same TX"| OUTBOX["📤 outbox_events table\nstatus: PENDING\npayload: BookingConfirmedEvent JSON"]
        AGG -->|"same TX"| BOOKING_DB["bookings table\nstatus: CONFIRMED"]
    end

    subgraph RELAY["Outbox Relay Job (500ms interval)"]
        POLL["🔄 Poll PENDING events\nbatch size: 100"] --> PUB["📡 Publish to RabbitMQ\nexchange: cinetix.events\nrouting: booking.events.confirmed"]
        PUB --> MARK["✅ Mark PROCESSED\nor ❌ FAILED (after 5 retries)"]
    end

    subgraph RMQ_ROUTING["RabbitMQ Topic Exchange: cinetix.events"]
        EXCHANGE["🐇 cinetix.events\n(topic exchange)"]
        Q1["Queue: ticket-svc.booking.events\nbinding: booking.events.*"]
        Q2["Queue: notif-svc.events\nbinding: booking.events.*\npayment.events.*"]
        Q3["Queue: booking-svc.events\nbinding: payment.events.*"]
        Q4["Queue: chat-svc.events\nbinding: *.events.*"]
    end

    subgraph CONSUMERS["Event Consumers"]
        TS_HANDLER["🎫 Ticket Service\nBookingConfirmedHandler\n→ Tạo QR tickets"]
        NS_HANDLER["📧 Notification Service\nBookingConfirmedHandler\n→ Gửi email xác nhận"]
        BS_HANDLER["📋 Booking Service\nPaymentCompletedHandler\n→ Xác nhận booking"]
    end

    OUTBOX --> RELAY
    RELAY --> EXCHANGE
    EXCHANGE --> Q1
    EXCHANGE --> Q2
    EXCHANGE --> Q3
    EXCHANGE --> Q4
    Q1 --> TS_HANDLER
    Q2 --> NS_HANDLER
    Q3 --> BS_HANDLER

    style BS_TX fill:#e8f5e9
    style RELAY fill:#fff3e0
    style RMQ_ROUTING fill:#fce4ec
    style CONSUMERS fill:#e3f2fd
```

---

### Luồng bù trừ lỗi (Compensation Flow)

```mermaid
flowchart TD
    START(["🚀 Booking Saga bắt đầu"]) --> HOLD

    HOLD["Step 1: HoldSeats\ngRPC → Showtime Service"] 
    HOLD -->|"✅ Success"| VOUCHER
    HOLD -->|"❌ Seats unavailable\nor Showtime gRPC fail"| FAIL_HOLD

    FAIL_HOLD["💥 Compensation:\nKhông cần rollback\n(seats chưa được giữ)"] --> BOOKING_FAILED_1(["❌ Booking FAILED"])

    VOUCHER["Step 2: ValidateVoucher\ngRPC → Promotion Service\n(nếu có voucher)"]
    VOUCHER -->|"✅ Valid"| PAYMENT
    VOUCHER -->|"❌ Voucher invalid/expired"| FAIL_VOUCHER

    FAIL_VOUCHER["💥 Compensation:\nReleaseSeats ← gRPC Showtime"] --> BOOKING_FAILED_2(["❌ Booking FAILED"])

    PAYMENT["Step 3: InitiatePayment\ngRPC → Payment Service"]
    PAYMENT -->|"✅ paymentUrl returned"| WAIT_PAYMENT
    PAYMENT -->|"❌ Payment Service error"| FAIL_PAYMENT

    FAIL_PAYMENT["💥 Compensation:\n1. VoidVoucher ← gRPC Promotion\n2. ReleaseSeats ← gRPC Showtime"] --> BOOKING_FAILED_3(["❌ Booking FAILED"])

    WAIT_PAYMENT["⏳ Chờ PaymentCompletedEvent\n(async RabbitMQ)\nTimeout: 10 phút"]
    WAIT_PAYMENT -->|"✅ PaymentCompletedEvent"| CONFIRM
    WAIT_PAYMENT -->|"❌ PaymentFailedEvent\nor Timeout"| FAIL_PAYMENT_ASYNC

    FAIL_PAYMENT_ASYNC["💥 Compensation:\n1. RefundPayment ← gRPC Payment\n2. VoidVoucher ← gRPC Promotion\n3. ReleaseSeats ← gRPC Showtime"] --> BOOKING_FAILED_4(["❌ Booking FAILED\nStatus: COMPENSATING → CANCELLED"])

    CONFIRM["Step 4: ConfirmSeats\ngRPC → Showtime Service\nSeats: HELD → BOOKED"]
    CONFIRM -->|"✅ Success"| SUCCESS
    CONFIRM -->|"❌ Showtime error\n(edge case)"| FAIL_CONFIRM

    FAIL_CONFIRM["💥 Compensation:\n1. RefundPayment ← gRPC Payment\n2. VoidVoucher ← gRPC Promotion\n(seats tự động expire)"] --> BOOKING_FAILED_5(["❌ Booking FAILED"])

    SUCCESS(["✅ Booking CONFIRMED\nPublish BookingConfirmedEvent\n→ Tickets + Notification"])

    style FAIL_HOLD fill:#ffcdd2
    style FAIL_VOUCHER fill:#ffcdd2
    style FAIL_PAYMENT fill:#ffcdd2
    style FAIL_PAYMENT_ASYNC fill:#ffcdd2
    style FAIL_CONFIRM fill:#ffcdd2
    style SUCCESS fill:#c8e6c9
    style BOOKING_FAILED_1 fill:#ef9a9a
    style BOOKING_FAILED_2 fill:#ef9a9a
    style BOOKING_FAILED_3 fill:#ef9a9a
    style BOOKING_FAILED_4 fill:#ef9a9a
    style BOOKING_FAILED_5 fill:#ef9a9a
```

---

## Cơ sở dữ liệu

### Sơ đồ quan hệ tổng thể

```mermaid
erDiagram
    %% Identity
    USER_PROFILES {
        uuid id PK
        string email
        string full_name
        string phone
        string avatar_url
        string status
        timestamp created_at
    }

    %% Movie Service
    MOVIES {
        uuid id PK
        string title
        string original_title
        int duration_minutes
        decimal imdb_score
        string status "COMING_SOON|NOW_SHOWING|ENDED"
        date release_date
        string poster_url
    }
    MOVIE_GENRES {
        uuid movie_id FK
        string genre
    }
    MOVIES ||--o{ MOVIE_GENRES : has

    %% Cinema Service
    CINEMAS {
        uuid id PK
        string name
        string address
        string city
        string district
        decimal latitude
        decimal longitude
    }
    SCREENS {
        uuid id PK
        uuid cinema_id FK
        string name
        string type "STANDARD|IMAX|4DX|SCREENX"
        int total_seats
    }
    SEATS {
        uuid id PK
        uuid screen_id FK
        string seat_code
        int row_num
        int col_num
        string category "STANDARD|VIP|COUPLE"
    }
    CINEMAS ||--o{ SCREENS : has
    SCREENS ||--o{ SEATS : has

    %% Showtime Service
    SHOWTIMES {
        uuid id PK
        uuid movie_id
        uuid cinema_id
        uuid screen_id
        timestamp start_time
        timestamp end_time
        string status "SCHEDULED|ON_SALE|FULL|CANCELLED"
        decimal base_price
        decimal vip_price
        int available_seats
        int version
    }
    SEAT_STATUS {
        uuid id PK
        uuid showtime_id FK
        uuid seat_id
        string seat_code
        string status "AVAILABLE|HELD|BOOKED|UNAVAILABLE"
        uuid booking_id
    }
    SHOWTIMES ||--o{ SEAT_STATUS : tracks

    %% Booking Service
    BOOKINGS {
        uuid id PK
        uuid customer_id
        uuid showtime_id
        string status "INITIATED|SEATS_HELD|PAYMENT_PENDING|CONFIRMED|CANCELLED|FAILED"
        decimal subtotal
        decimal discount_amount
        decimal final_amount
        string voucher_code
        uuid payment_id
        timestamp expires_at
        int version
    }
    BOOKING_ITEMS {
        uuid id PK
        uuid booking_id FK
        uuid seat_id
        string seat_code
        string seat_category
        decimal unit_price
    }
    SAGA_STATES {
        uuid booking_id PK
        string current_step
        jsonb step_history
        timestamp started_at
    }
    BOOKINGS ||--o{ BOOKING_ITEMS : contains
    BOOKINGS ||--|| SAGA_STATES : tracks

    %% Payment Service
    PAYMENTS {
        uuid id PK
        uuid booking_id
        uuid customer_id
        decimal amount
        string status "PENDING|COMPLETED|FAILED|REFUNDED"
        string method
        string payment_url
        string idempotency_key
        timestamp expires_at
    }
    REFUNDS {
        uuid id PK
        uuid payment_id FK
        decimal amount
        string status
        string reason
    }
    PAYMENTS ||--o{ REFUNDS : has

    %% Promotion Service
    PROMOTIONS {
        uuid id PK
        string name
        string discount_type "PERCENT|FIXED"
        decimal discount_percent
        decimal discount_amount
        decimal max_discount
        int max_uses
        int current_uses
        boolean active
    }
    VOUCHERS {
        uuid id PK
        string code UK
        uuid promotion_id FK
        string status "ACTIVE|REDEEMED|EXPIRED|VOIDED"
        uuid assigned_customer_id
        uuid booking_id
        timestamp expires_at
    }
    PROMOTIONS ||--o{ VOUCHERS : generates

    %% Ticket Service
    TICKETS {
        uuid id PK
        uuid booking_id
        uuid customer_id
        string seat_code
        uuid showtime_id
        string movie_title
        string cinema_name
        string qr_code
        string status "ACTIVE|USED|CANCELLED|EXPIRED"
        timestamp issued_at
        timestamp used_at
    }
```

---

## API Endpoints

### API Gateway — `http://localhost:8080`

#### Booking Service — `/api/v1/bookings`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `POST` | `/` | Tạo booking mới (trả về paymentUrl) | ✅ USER |
| `GET` | `/{bookingId}` | Lấy chi tiết booking | ✅ USER (own) |
| `GET` | `/my` | Danh sách booking của tôi (paginated) | ✅ USER |
| `DELETE` | `/{bookingId}` | Huỷ booking | ✅ USER |

#### Movie Service — `/api/v1/movies`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/` | Danh sách phim đang chiếu (cached) | ❌ Public |
| `GET` | `/coming-soon` | Phim sắp chiếu | ❌ Public |
| `GET` | `/{id}` | Chi tiết phim (cached) | ❌ Public |
| `GET` | `/search?q=` | Tìm kiếm theo tên | ❌ Public |
| `POST` | `/` | Thêm phim mới | ✅ ADMIN |
| `PUT` | `/{id}` | Cập nhật phim | ✅ ADMIN |

#### Showtime Service — `/api/v1/showtimes`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/` | Danh sách suất chiếu đang bán | ❌ Public |
| `GET` | `/{id}` | Chi tiết suất chiếu | ❌ Public |
| `GET` | `/{id}/seats` | Sơ đồ ghế + trạng thái | ✅ USER |
| `POST` | `/` | Tạo suất chiếu | ✅ ADMIN |
| `PATCH` | `/{id}/open-for-sale` | Mở bán vé | ✅ ADMIN |
| WS | `/ws/showtime/{id}` | Real-time seat updates (STOMP) | ✅ USER |

#### Cinema Service — `/api/v1/cinemas`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/` | Danh sách rạp (filter by city) | ❌ Public |
| `GET` | `/{id}` | Chi tiết rạp | ❌ Public |
| `GET` | `/{id}/screens` | Danh sách phòng chiếu | ❌ Public |
| `POST` | `/` | Thêm rạp | ✅ ADMIN |
| `POST` | `/{id}/screens` | Thêm phòng chiếu | ✅ ADMIN |

#### Ticket Service — `/api/v1/tickets`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/booking/{bookingId}` | Vé theo booking | ✅ USER |
| `GET` | `/my` | Tất cả vé của tôi | ✅ USER |
| `POST` | `/{id}/validate` | Xác thực vé tại cổng rạp | ✅ ADMIN |

#### Payment Service — `/api/v1/payments`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/{id}` | Chi tiết thanh toán | ✅ USER |
| `GET` | `/booking/{bookingId}` | Thanh toán theo booking | ✅ USER |
| `POST` | `/{id}/confirm` | PSP Webhook: xác nhận thành công | 🔑 PSP Secret |
| `POST` | `/{id}/fail` | PSP Webhook: báo thất bại | 🔑 PSP Secret |

#### Promotion Service — `/api/v1/promotions`
| Method | Endpoint | Mô tả | Auth |
|--------|----------|-------|------|
| `GET` | `/` | Danh sách khuyến mãi đang hoạt động | ❌ Public |
| `POST` | `/` | Tạo chương trình KM | ✅ ADMIN |
| `POST` | `/vouchers` | Tạo voucher codes | ✅ ADMIN |

---

## Cài đặt & Chạy local

### Yêu cầu
- Docker & Docker Compose v2+
- Java 21 (nếu build từ source)
- Node.js 20+ (cho frontend dev)
- Maven 3.9+

### Khởi động toàn bộ hệ thống với Docker Compose

```bash
# Clone repository
git clone <repo-url>
cd CinemaCinema

# Khởi động infrastructure (PostgreSQL, Redis, RabbitMQ, Keycloak)
docker compose -f infrastructure/docker/docker-compose.yml up -d postgres redis rabbitmq keycloak

# Chờ Keycloak khởi động (~60s), sau đó import realm config
# (Nếu có file realm-export.json)
# docker exec keycloak /opt/keycloak/bin/kc.sh import --file /realm-export.json

# Build và khởi động tất cả services
docker compose -f infrastructure/docker/docker-compose.yml up -d

# Kiểm tra logs
docker compose -f infrastructure/docker/docker-compose.yml logs -f booking-service
```

### Chạy frontend dev mode

```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

### Chạy từng service (development)

```bash
# Build tất cả modules
./mvnw clean install -DskipTests

# Chạy service cụ thể
cd services/booking-service
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Truy cập services

| Service | URL |
|---------|-----|
| Frontend | http://localhost:3000 |
| API Gateway | http://localhost:8080 |
| Keycloak Admin | http://localhost:8180 (admin/admin) |
| RabbitMQ Management | http://localhost:15672 (cinetix/cinetix_secret) |
| Grafana | http://localhost:3001 |
| Prometheus | http://localhost:9090 |

---

## Biến môi trường

### Backend Services (Docker Compose)

```env
# Database
DB_HOST=postgres
DB_PORT=5432
DB_USER=postgres
DB_PASS=postgres

# Redis
REDIS_HOST=redis
REDIS_PASSWORD=redis_secret
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=cinetix
RABBITMQ_PASS=cinetix_secret

# Keycloak
KEYCLOAK_URL=http://keycloak:8180
KEYCLOAK_REALM=cinetix
KEYCLOAK_CLIENT_ID=cinetix-backend

# Email (Notification Service)
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Frontend

```env
VITE_KEYCLOAK_URL=http://localhost:8180
VITE_KEYCLOAK_REALM=cinetix
VITE_KEYCLOAK_CLIENT=cinetix-frontend
VITE_API_BASE=/api/v1
```

---

## Cấu trúc thư mục

```
CinemaCinema/
├── frontend/                   # Vue 3 + Vite SPA
│   └── src/
│       ├── pages/              # Trang: Home, MovieDetail, SeatSelection...
│       ├── components/         # UI components
│       ├── stores/             # Pinia: auth, booking
│       ├── api/                # Axios clients per service
│       ├── composables/        # useSeatMap.js, ...
│       └── router/             # Vue Router
│
├── services/                   # 11 Microservices (Spring Boot)
│   ├── api-gateway/
│   ├── identity-service/
│   ├── movie-service/
│   ├── cinema-service/
│   ├── showtime-service/
│   ├── booking-service/        # ← Core: Saga Orchestrator
│   ├── payment-service/
│   ├── promotion-service/
│   ├── ticket-service/
│   ├── notification-service/
│   └── chat-service/
│
├── libs/                       # Shared Libraries
│   ├── cinetix-common/         # ApiResponse, DDD base classes, exceptions
│   ├── cinetix-security/       # JWT / Spring Security config
│   ├── cinetix-outbox/         # Outbox pattern implementation
│   └── cinetix-grpc-stubs/     # Generated gRPC stubs
│
├── proto/                      # Protobuf definitions (.proto files)
│   ├── showtime.proto
│   ├── payment.proto
│   └── promotion.proto
│
├── infrastructure/
│   ├── docker/
│   │   └── docker-compose.yml  # Local dev environment
│   ├── helm/                   # Kubernetes Helm charts
│   │   ├── charts/             # Chart per service
│   │   └── values/             # Environment-specific values
│   └── argocd/                 # GitOps ArgoCD apps
│
└── pom.xml                     # Maven parent POM
```

---

## Luồng trạng thái Booking

```mermaid
stateDiagram-v2
    [*] --> INITIATED: POST /api/v1/bookings
    INITIATED --> SEATS_HELD: gRPC HoldSeats OK
    INITIATED --> FAILED: HoldSeats fail

    SEATS_HELD --> VOUCHER_APPLIED: ValidateVoucher OK
    SEATS_HELD --> PAYMENT_PENDING: Không có voucher
    SEATS_HELD --> FAILED: ValidateVoucher fail\n→ ReleaseSeats

    VOUCHER_APPLIED --> PAYMENT_PENDING: InitiatePayment OK
    VOUCHER_APPLIED --> FAILED: InitiatePayment fail\n→ VoidVoucher + ReleaseSeats

    PAYMENT_PENDING --> CONFIRMING: PaymentCompletedEvent received
    PAYMENT_PENDING --> COMPENSATING: PaymentFailedEvent / Timeout

    CONFIRMING --> CONFIRMED: ConfirmSeats OK\n→ BookingConfirmedEvent
    CONFIRMING --> COMPENSATING: ConfirmSeats fail

    COMPENSATING --> CANCELLED: Compensation complete\n(Refund + VoidVoucher + ReleaseSeats)

    CONFIRMED --> [*]: Vé đã phát hành
    CANCELLED --> [*]: Đã hoàn tiền
    FAILED --> [*]: Không hoàn tiền (không charge)
```

---

## Kiến trúc Domain Events

```mermaid
graph LR
    subgraph BOOKING["📋 Booking Service"]
        B1["BookingInitiatedEvent"]
        B2["BookingConfirmedEvent"]
        B3["BookingCancelledEvent"]
        B4["BookingFailedEvent"]
    end

    subgraph PAYMENT["💳 Payment Service"]
        P1["PaymentCompletedEvent"]
        P2["PaymentFailedEvent"]
        P3["PaymentRefundedEvent"]
    end

    subgraph SHOWTIME["🪑 Showtime Service"]
        S1["SeatsHeldEvent"]
        S2["SeatsConfirmedEvent"]
        S3["SeatsReleasedEvent"]
    end

    subgraph EXCHANGE["🐇 RabbitMQ\ncinetix.events"]
        EX["Topic Exchange"]
    end

    subgraph CONSUMERS["Consumers"]
        TS["🎫 Ticket Service\n← BookingConfirmedEvent"]
        NS["📧 Notification Service\n← Booking* + Payment*"]
        BS_C["📋 Booking Service\n← PaymentCompleted/Failed"]
        FE["🌐 Frontend WebSocket\n← SeatsHeld/Released"]
    end

    B2 --> EX
    B3 --> EX
    P1 --> EX
    P2 --> EX
    S1 --> EX
    S2 --> EX
    S3 --> EX

    EX --> TS
    EX --> NS
    EX --> BS_C
    EX --> FE

    style BOOKING fill:#e8f5e9
    style PAYMENT fill:#e3f2fd
    style SHOWTIME fill:#fff3e0
    style EXCHANGE fill:#fce4ec
    style CONSUMERS fill:#f3e5f5
```

---

*CineTix — Built with ❤️ using Java 21, Spring Boot 3, Vue 3, and modern cloud-native patterns.*
