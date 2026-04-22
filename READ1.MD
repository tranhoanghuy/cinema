# ROLE
B?n l? m?t Senior Software Architect c? 10+ n?m kinh nghi?m thi?t k? h? th?ng                                                                                                                                           
microservices quy m? l?n b?ng Java/Spring Boot, chuy?n s?u v? Domain-Driven                                                                                                                                             
Design, Event-Driven Architecture, v? Cloud-Native deployment tr?n Kubernetes.                                                                                                                                          
B?n s? h??ng d?n t?i build m?t project end-to-end t? ??u ??n khi deploy production.

# PROJECT
T?n project: CineTix ― Online Movie Ticketing Platform                                                                                                                                                                  
M? t?: N?n t?ng ??t v? xem phim online t??ng t? CGV/Galaxy/BHD, cho ph?p                                                                                                                                                
ng??i d?ng duy?t phim ?ang chi?u, ch?n r?p/su?t chi?u, ch?n gh? tr?n s? ??                                                                                                                                              
real-time, thanh to?n, nh?n v? QR, v? chat v?i support. Admin qu?n l? phim,                                                                                                                                             
r?p, su?t chi?u, khuy?n m?i.

# TECH STACK B?T BU?C
## Backend
- Java 21, Spring Boot 3.3+, Spring Cloud 2023.x
- Build tool: Maven
- Ki?n tr?c n?i b? m?i service: DDD Tactical Patterns (Aggregate, Entity, Value Object, Domain Event,                                                                                                                   
  Repository, Domain Service, Application Service)
- Spring Modulith ?? enforce module boundary trong t?ng service

## Communication
- gRPC (grpc-spring-boot-starter): giao ti?p SYNC n?i b? gi?a c?c service
- REST + HTTPS: API Gateway expose ra ngo?i (Spring Cloud Gateway)
- RabbitMQ (topic exchange): ASYNC event bus
- WebSocket (STOMP over SockJS): real-time seat map + chat

## Data & Patterns
- PostgreSQL 16: m?i service m?t database ri?ng (database-per-service)
- Redis 7: cache + distributed lock + seat-hold TTL
- Flyway: migration
- Saga Pattern: Orchestration (c? m?t Booking Orchestrator service)
- Transactional Outbox Pattern: d?ng Debezium CDC ??c WAL c?a Postgres                                                                                                                                                  
  ??y event sang RabbitMQ
- CQRS nh? ? Booking service (write model + read model t?ch ri?ng)

## Security
- Keycloak: Identity Provider (OAuth2 / OIDC)
- JWT (RS256): access token
- Spring Security Resource Server ? m?i service
- mTLS gi?a c?c service qua Istio service mesh (optional, b?n t? v?n c? n?n kh?ng)

## Observability
- OpenTelemetry SDK (auto + manual instrumentation)
- Prometheus + Micrometer: metrics
- Grafana Loki: log aggregation
- Grafana Tempo: distributed tracing
- Grafana dashboards

## Infrastructure
- Docker + Docker Compose (dev local)
- Kubernetes (production): d?ng kind cho local-k8s, GKE/EKS ho?c k3s cho prod
- Helm charts cho t?ng service
- Istio service mesh (optional)
- NGINX Ingress Controller
- cert-manager + Let's Encrypt cho HTTPS

## CI/CD
- GitHub Actions: build, unit test, integration test, SAST (SonarQube),                                                                                                                                                 
  container scan (Trivy), push image l?n GHCR/Docker Hub
- ArgoCD: GitOps deployment
- Semantic versioning + conventional commits

## Testing
- JUnit 5, AssertJ, Mockito
- Testcontainers: integration test v?i Postgres, RabbitMQ, Redis th?t
- Spring Cloud Contract ho?c Pact: consumer-driven contract testing
- k6 ho?c Gatling: load test

# NHI?M V? C?A B?N
H??ng d?n t?i thi?t k? v? build project n?y theo th? t? c?c phase d??i ??y.                                                                                                                                             
V?i m?i phase, b?n ??a ra output c? th?, c? code m?u, diagram (d?ng text/                                                                                                                                               
mermaid), v? gi?i th?ch trade-off c?a t?ng quy?t ??nh k? thu?t.

## Phase 1 ― Strategic Design (DDD)
1.1. Event Storming: li?t k? t?t c? Domain Event c?a business.                                                                                                                                                          
1.2. X?c ??nh Bounded Context v? Context Map (quan h? gi?a c?c context:                                                                                                                                                 
Customer-Supplier, Conformist, ACL, Shared Kernel, v.v.).                                                                                                                                                               
1.3. Map m?i Bounded Context th?nh m?t microservice, n?u r? tr?ch nhi?m,                                                                                                                                                
aggregate ch?nh, v? ng?n ng? chung (Ubiquitous Language).                                                                                                                                                               
1.4. ?? xu?t Core Domain vs Supporting vs Generic subdomain.

## Phase 2 ― High-Level Architecture
2.1. V? s? ?? ki?n tr?c t?ng th? (C4 model ― Context + Container diagram,                                                                                                                                               
d?ng mermaid).                                                                                                                                                                                                          
2.2. Li?t k? to?n b? microservices k?m port, dependency, database, v?                                                                                                                                                   
giao th?c in/out c?a t?ng service.                                                                                                                                                                                      
2.3. S? ?? lu?ng d? li?u ch?nh (??t v? end-to-end).                                                                                                                                                                     
2.4. Quy?t ??nh: service n?o SYNC (gRPC), service n?o ASYNC (RabbitMQ),                                                                                                                                                 
v? l? do cho t?ng quy?t ??nh.

## Phase 3 ― Detailed Design cho Booking Saga (flagship feature)
3.1. Saga orchestration flow ??y ?? cho lu?ng "User ??t v?":
- Reserve seats (hold TTL 10 ph?t ? Showtime Service, d?ng Redis)
- Apply promotion (n?u c?)
- Create booking (PENDING)
- Initiate payment
- Confirm booking + Issue ticket QR
- Send notification                                                                                                                                                                                                     
  K?m compensation cho t?ng b??c khi fail.                                                                                                                                                                                
  3.2. State machine c?a Booking aggregate (Spring StateMachine ho?c t? implement).                                                                                                                                       
  3.3. Chi ti?t Transactional Outbox: schema b?ng outbox, c?ch Debezium                                                                                                                                                   
  capture WAL, routing sang RabbitMQ exchange n?o, c?ch handle duplicate                                                                                                                                                  
  (idempotency key ? consumer).                                                                                                                                                                                           
  3.4. Timeout & retry strategy: ai ch?u tr?ch nhi?m timeout, d?ng Quartz                                                                                                                                                 
  scheduler hay message TTL c?a RabbitMQ.

## Phase 4 ― Database Design
4.1. Schema chi ti?t cho t?ng service (DDL):
- identity-service: users, roles
- movie-service: movies, genres, casts
- cinema-service: cinemas, screens, seats_layout
- showtime-service: showtimes, seat_status (Redis + Postgres snapshot)
- booking-service: bookings, booking_items, outbox, saga_state
- payment-service: payments, payment_methods, refunds, outbox
- promotion-service: promotions, vouchers, usage_log
- notification-service: notification_log
- chat-service: conversations, messages                                                                                                                                                                                 
  4.2. Ch? r? Aggregate Root, Entity, Value Object trong code vs b?ng DB.                                                                                                                                                 
  4.3. Index strategy v? partitioning (n?u c?n cho b?ng l?n nh? seat_status).

## Phase 5 ― Contract Design
5.1. Vi?t ??y ?? file .proto cho c?c service giao ti?p qua gRPC:
- showtime.proto (GetShowtime, HoldSeats, ReleaseSeats, ConfirmSeats)
- payment.proto (InitiatePayment, RefundPayment)
- promotion.proto (ValidateVoucher, RedeemVoucher)                                                                                                                                                                      
  5.2. ??nh ngh?a Event Schema (JSON Schema ho?c Avro) cho RabbitMQ:
- BookingCreated, BookingConfirmed, BookingCancelled
- PaymentInitiated, PaymentCompleted, PaymentFailed
- SeatsHeld, SeatsReleased
- NotificationRequested                                                                                                                                                                                                 
  K?m routing key convention (vd: booking.events.created.v1).                                                                                                                                                             
  5.3. ??nh ngh?a REST API spec (OpenAPI 3.1) cho API Gateway.                                                                                                                                                            
  5.4. ??nh ngh?a WebSocket topic:
- /topic/showtime/{showtimeId}/seats ― broadcast seat status
- /user/queue/chat/{conversationId} ― chat message
- /user/queue/booking/{bookingId} ― booking status update

## Phase 6 ― Code Structure
6.1. C?u tr?c th? m?c monorepo (?? xu?t c? Gradle composite build v?                                                                                                                                                    
Maven multi-module, khuy?n d?ng c?i n?o).                                                                                                                                                                               
6.2. C?u tr?c th? m?c Hexagonal cho m?t service m?u (booking-service):
- domain/ (model, event, port)
- application/ (usecase, saga)
- infrastructure/ (adapter: persistence, messaging, grpc, rest)
- config/                                                                                                                                                                                                               
  6.3. Code m?u cho Booking aggregate, BookingSagaOrchestrator,                                                                                                                                                           
  OutboxPublisher, SeatHoldRedisAdapter.

## Phase 7 ― Deployment & DevOps
7.1. Dockerfile multi-stage t?i ?u cho Spring Boot (layered jar + JRE slim,                                                                                                                                             
ho?c GraalVM native image ― t? v?n c? n?n d?ng kh?ng).                                                                                                                                                                  
7.2. docker-compose.yml cho dev local (t?t c? service + infra).                                                                                                                                                         
7.3. Helm chart template cho m?t service (values.yaml, deployment, service,                                                                                                                                             
hpa, ingress, configmap, secret).                                                                                                                                                                                       
7.4. K8s manifest cho infra: PostgreSQL StatefulSet, RabbitMQ cluster                                                                                                                                                   
(ho?c d?ng operator), Redis cluster, Keycloak, observability stack.                                                                                                                                                     
7.5. GitHub Actions workflow:
- ci.yml: build + test + scan
- release.yml: build image + push + bump chart version
- ArgoCD ApplicationSet ?? auto-sync.

## Phase 8 ― Roadmap (8 tu?n)
Chia th?nh sprint 1 tu?n, m?i sprint c? m?c ti?u r? r?ng, deliverable c? th?,                                                                                                                                           
v? definition of done.

# Y?U C?U CHUNG KHI TR? L?I
- Tr? l?i b?ng ti?ng Vi?t, thu?t ng? k? thu?t gi? nguy?n ti?ng Anh.
- Code m?u ??y ??, production-ready, c? comment gi?i th?ch.
- Khi c? nhi?u l?a ch?n, lu?n n?u 2?3 option + trade-off + khuy?n ngh?.
- D?ng mermaid cho diagram.
- N?u c? th? g? t?i ch?a ngh? t?i nh?ng quan tr?ng (vd: rate-limit, idempotency,                                                                                                                                        
  DLQ, circuit breaker, graceful shutdown, zero-downtime migration), ch? ??ng                                                                                                                                           
  nh?c t?i.



? Orchestrating… (3s ? ↓ 131 tokens ? thought for 2s)

──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
?? 
──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
paste again to expand                                                                                                                                                                                 ● high ? /effort
hpa, ingress, configmap, secret).                                                                                                                                                                                       
7.4. K8s manifest cho infra: PostgreSQL StatefulSet, RabbitMQ cluster                                                                                                                                                   
(ho?c d?ng operator), Redis cluster, Keycloak, observability stack.                                                                                                                                                     
7.5. GitHub Actions workflow:
- ci.yml: build + test + scan
- release.yml: build image + push + bump chart version
- ArgoCD ApplicationSet ?? auto-sync.

## Phase 8 ― Roadmap (8 tu?n)
Chia th?nh sprint 1 tu?n, m?i sprint c? m?c ti?u r? r?ng, deliverable c? th?,                                                                                                                                           
v? definition of done.

# Y?U C?U CHUNG KHI TR? L?I
- Tr? l?i b?ng ti?ng Vi?t, thu?t ng? k? thu?t gi? nguy?n ti?ng Anh.
- Code m?u ??y ??, production-ready, c? comment gi?i th?ch.
- Khi c? nhi?u l?a ch?n, lu?n n?u 2?3 option + trade-off + khuy?n ngh?.
- D?ng mermaid cho diagram.
- N?u c? th? g? t?i ch?a ngh? t?i nh?ng quan tr?ng (vd: rate-limit, idempotency,                                                                                                                                        
  DLQ, circuit breaker, graceful shutdown, zero-downtime migration), ch? ??ng                                                                                                                                           
  nh?c t?i.



● ?? b?t ??u, t?i s? ki?m tra c?u tr?c project hi?n t?i tr??c khi ?i v?o Phase 1.

Listed 1 directory (ctrl+o to expand)

● Project kh?i ?i?m s?ch ― ho?n h?o. T?i s? d?n d?t to?n b? 8 phase. B?t ??u v?i Phase 1.
                                                                                                                                                                                                                        
---                                                                                                                                                                                                                     
PHASE 1 ― STRATEGIC DESIGN (DDD)

1.1 Event Storming

Event Storming l? workshop k? thu?t kh?m ph? domain b?ng c?ch li?t k? t?t c? Domain Events (?i?u ?? x?y ra, qu? kh?) tr?n timeline. M?u cam = Event, m?u v?ng = Command, m?u t?m = Policy/Reaction.

TIMELINE ──────────────────────────────────────────────────────────────────────?

[AUTHENTICATION DOMAIN]
CMD: RegisterAccount   → UserRegistered                                                                                                                                                                               
CMD: VerifyEmail        → EmailVerified                                                                                                                                                                               
CMD: Login              → UserLoggedIn                                                                                                                                                                                
CMD: RefreshToken       → TokenRefreshed                                                                                                                                                                              
CMD: Logout             → UserLoggedOut

[MOVIE CATALOG DOMAIN]                                                                                                                                                                                                  
CMD: CreateMovie        → MovieCreated
CMD: PublishMovie       → MoviePublished                                                                                                                                                                              
CMD: UpdateMovieInfo    → MovieInfoUpdated
CMD: AddCast            → CastAdded                                                                                                                                                                                   
CMD: SetMovieStatus     → MovieStatusChanged (COMING_SOON → NOW_SHOWING → ENDED)

[CINEMA MANAGEMENT DOMAIN]
CMD: RegisterCinema     → CinemaRegistered                                                                                                                                                                            
CMD: AddScreen          → ScreenAdded                                                                                                                                                                                 
CMD: ConfigureSeats     → SeatLayoutConfigured
CMD: SetSeatCategory    → SeatCategoryUpdated (STANDARD / VIP / COUPLE)

[SHOWTIME DOMAIN]                                                                                                                                                                                                       
CMD: CreateShowtime     → ShowtimeCreated                                                                                                                                                                             
CMD: OpenShowtime       → ShowtimeOpened      (cho ph?p ??t v?)
CMD: CancelShowtime     → ShowtimeCancelled                                                                                                                                                                           
CMD: HoldSeats          → SeatsHeld           (TTL 10 ph?t)
CMD: ReleaseSeats       → SeatsReleased       (timeout / cancel)                                                                                                                                                      
CMD: ConfirmSeats       → SeatsConfirmed      (sau payment)                                                                                                                                                           
POLICY: TTL expired      → SeatsAutoReleased

[BOOKING DOMAIN]   ?── CORE DOMAIN
CMD: InitiateBooking    → BookingInitiated                                                                                                                                                                            
CMD: ApplyVoucher       → VoucherApplied                                                                                                                                                                              
CMD: ConfirmBooking     → BookingConfirmed
CMD: CancelBooking      → BookingCancelled                                                                                                                                                                            
CMD: ExpireBooking      → BookingExpired      (payment timeout)
SAGA: BookingCompleted  → TicketIssued

[PAYMENT DOMAIN]
CMD: InitiatePayment    → PaymentInitiated                                                                                                                                                                            
CMD: ProcessPayment     → PaymentProcessed    (callback t? PSP)                                                                                                                                                       
CMD: CompletePayment    → PaymentCompleted                                                                                                                                                                            
CMD: FailPayment        → PaymentFailed                                                                                                                                                                               
CMD: RequestRefund      → RefundRequested                                                                                                                                                                             
CMD: ProcessRefund      → RefundProcessed

[PROMOTION DOMAIN]
CMD: CreatePromotion    → PromotionCreated                                                                                                                                                                            
CMD: CreateVoucher      → VoucherCreated
CMD: ValidateVoucher    → VoucherValidated
CMD: RedeemVoucher      → VoucherRedeemed                                                                                                                                                                             
CMD: VoidVoucher        → VoucherVoided       (compensation)

[TICKET DOMAIN]
CMD: IssueTicket        → TicketIssued                                                                                                                                                                                
CMD: ValidateTicket     → TicketValidated     (scan QR t?i r?p)
CMD: UseTicket          → TicketUsed

[NOTIFICATION DOMAIN]                                                                                                                                                                                                   
POLICY: BookingConfirmed    → SendEmailNotification                                                                                                                                                                   
POLICY: TicketIssued        → SendTicketEmail + PushNotification                                                                                                                                                      
POLICY: BookingCancelled    → SendCancellationEmail
POLICY: RefundProcessed     → SendRefundEmail                                                                                                                                                                         
POLICY: ShowtimeCancelled   → BroadcastCancellationToAffectedUsers

[CHAT/SUPPORT DOMAIN]
CMD: OpenConversation   → ConversationOpened                                                                                                                                                                          
CMD: SendMessage        → MessageSent                                                                                                                                                                                 
CMD: CloseConversation  → ConversationClosed
CMD: AssignAgent        → AgentAssigned

[REVIEW DOMAIN]  (future)
CMD: SubmitReview       → ReviewSubmitted                                                                                                                                                                             
CMD: ModerateReview     → ReviewApproved / ReviewRejected
                                                                                                                                                                                                                          
---
1.2 Bounded Contexts & Context Map

M?i Bounded Context (BC) l? m?t v?ng ng?n ng? nh?t qu?n. D??i ??y l? Context Map v?i quan h? r? r?ng:

graph TB
subgraph "Core Domain"                                                                                                                                                                                              
BC_BOOKING["? Booking Context<br/>(Booking Orchestrator)"]                                                                                                                                                     
BC_SHOWTIME["? Showtime Context<br/>(Seat Availability)"]                                                                                                                                                      
end

      subgraph "Supporting Subdomain"
          BC_MOVIE["? Movie Catalog Context"]                                                                                                                                                                            
          BC_CINEMA["??Cinema Context"]
          BC_PAYMENT["? Payment Context"]                                                                                                                                                                                
          BC_TICKET["? Ticket Context"]
          BC_PROMOTION["? Promotion Context"]                                                                                                                                                                            
          BC_NOTIFICATION["? Notification Context"]
      end                                                                                                                                                                                                                 
   
      subgraph "Generic Subdomain"                                                                                                                                                                                        
          BC_IDENTITY["? Identity Context<br/>(Keycloak)"]
          BC_CHAT["? Chat/Support Context"]                                                                                                                                                                              
          BC_REVIEW["? Review Context"]
      end                                                                                                                                                                                                                 
                  
      %% Quan h? gi?a c?c context                                                                                                                                                                                         
      BC_IDENTITY -- "Upstream ?<br/>Shared Kernel (JWT claims)" --> BC_BOOKING
      BC_IDENTITY -- "Upstream ?<br/>Shared Kernel (JWT claims)" --> BC_SHOWTIME                                                                                                                                          
      BC_IDENTITY -- "Upstream ?<br/>Shared Kernel (JWT claims)" --> BC_PAYMENT
                                                                                                                                                                                                                          
      BC_MOVIE -- "Customer/Supplier<br/>(movie info → showtime)" --> BC_SHOWTIME                                                                                                                                         
      BC_CINEMA -- "Customer/Supplier<br/>(screen/seat config → showtime)" --> BC_SHOWTIME                                                                                                                                
                                                                                                                                                                                                                          
      BC_BOOKING -- "Orchestrator ?<br/>gRPC calls" --> BC_SHOWTIME                                                                                                                                                       
      BC_BOOKING -- "Orchestrator ?<br/>gRPC calls" --> BC_PAYMENT                                                                                                                                                        
      BC_BOOKING -- "Orchestrator ?<br/>gRPC calls" --> BC_PROMOTION                                                                                                                                                      
      BC_BOOKING -- "Orchestrator ?<br/>Domain Event (async)" --> BC_TICKET                                                                                                                                               
      BC_BOOKING -- "Orchestrator ?<br/>Domain Event (async)" --> BC_NOTIFICATION                                                                                                                                         
                                                                                                                                                                                                                          
      BC_PAYMENT -- "Conformist<br/>(PSP callback via webhook)" --> EXTERNAL_PSP["? External PSP<br/>(VNPay/Momo)"]                                                                                                      
                  
      BC_TICKET -- "ACL<br/>(anti-corruption layer)" --> BC_BOOKING                                                                                                                                                       
      BC_CHAT -- "Standalone" --> BC_IDENTITY
      BC_REVIEW -- "Conformist" --> BC_MOVIE                                                                                                                                                                              
   
      style BC_BOOKING fill:#ff6b6b,color:#fff                                                                                                                                                                            
      style BC_SHOWTIME fill:#ff6b6b,color:#fff
      style EXTERNAL_PSP fill:#95a5a6,color:#fff                                                                                                                                                                          

Gi?i th?ch c?c quan h?:

┌───────────────────┬──────────┬────────────────────────────┬───────────────┬───────────────────────────────────────────────────────────────────────────┐                                                               
│      Quan h?      │    T?    │            ??n             │     Lo?i      │                                   L? do                                   │
├───────────────────┼──────────┼────────────────────────────┼───────────────┼───────────────────────────────────────────────────────────────────────────┤                                                               
│ Shared Kernel     │ Identity │ All services               │ Shared Kernel │ JWT token structure, UserID type d?ng chung qua th? vi?n                  │
├───────────────────┼──────────┼────────────────────────────┼───────────────┼───────────────────────────────────────────────────────────────────────────┤
│ Customer/Supplier │ Movie    │ Showtime                   │ C/S           │ Showtime ph? thu?c movie info nh?ng kh?ng control Movie domain            │                                                               
├───────────────────┼──────────┼────────────────────────────┼───────────────┼───────────────────────────────────────────────────────────────────────────┤                                                               
│ Customer/Supplier │ Cinema   │ Showtime                   │ C/S           │ Screen/seat layout l? input c?a Showtime                                  │                                                               
├───────────────────┼──────────┼────────────────────────────┼───────────────┼───────────────────────────────────────────────────────────────────────────┤                                                               
│ Orchestrator      │ Booking  │ Showtime/Payment/Promotion │ Partnership   │ Booking Saga ?i?u ph?i, c?c service kh?c conform                          │
├───────────────────┼──────────┼────────────────────────────┼───────────────┼───────────────────────────────────────────────────────────────────────────┤                                                               
│ Conformist        │ Ticket   │ Booking                    │ Conformist    │ Ticket nh?n event t? Booking, adapt sang model ri?ng                      │
├───────────────────┼──────────┼────────────────────────────┼───────────────┼───────────────────────────────────────────────────────────────────────────┤                                                               
│ ACL               │ Payment  │ VNPay/Momo                 │ ACL           │ C?n Anti-Corruption Layer ?? isolate external PSP model kh?i domain model │
└───────────────────┴──────────┴────────────────────────────┴───────────────┴───────────────────────────────────────────────────────────────────────────┘
                  
---                                                                                                                                                                                                                     
1.3 Mapping Bounded Context → Microservice

? BC1: Booking Context → booking-service

Tr?ch nhi?m: ?i?u ph?i to?n b? lu?ng ??t v? qua Saga Orchestrator. ??y l? Aggregate Root quan tr?ng nh?t c?a h? th?ng.

Ubiquitous Language:
- Booking = m?t phi?n ??t v? (1 user + 1 showtime + N gh?)
- BookingItem = m?t d?ng trong booking (1 gh? + gi?)
- BookingStatus = INITIATED → SEATS_HELD → PAYMENT_PENDING → CONFIRMED → CANCELLED | EXPIRED
- SagaState = tr?ng th?i saga orchestration
- BookingExpiry = th?i ?i?m timeout (10 ph?t t? khi hold seats)

Aggregates ch?nh: Booking (root), BookingItem (entity b?n trong)
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? BC2: Showtime Context → showtime-service

Tr?ch nhi?m: Qu?n l? l?ch chi?u, s? ?? gh? real-time (Redis), cung c?p gRPC endpoint cho Booking.

Ubiquitous Language:
- Showtime = m?t bu?i chi?u c? th? (phim + r?p + ph?ng + th?i ?i?m)
- SeatSlot = m?t ? gh? trong m?t Showtime c? th? (kh?c v?i Seat layout)
- SeatStatus = AVAILABLE | HELD | CONFIRMED | UNAVAILABLE
- SeatHold = lock t?m th?i 10 ph?t (stored in Redis)

Aggregates ch?nh: Showtime, SeatAvailability (l? projection, kh?ng ph?i aggregate)
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? BC3: Movie Catalog Context → movie-service

Tr?ch nhi?m: Qu?n l? th?ng tin phim, th? lo?i, di?n vi?n, poster. Read-heavy, ?t write.

Ubiquitous Language:
- Movie = th?c th? phim v?i metadata ??y ??
- Genre = th? lo?i (Action, Comedy, ...)
- Cast = th?nh vi?n di?n xu?t
- MovieStatus = DRAFT | COMING_SOON | NOW_SHOWING | ENDED
- Rating = x?p h?ng ?? tu?i (P, K, T13, T16, T18)

Aggregates ch?nh: Movie
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? BC4: Cinema Context → cinema-service

Tr?ch nhi?m: Qu?n l? r?p chi?u, ph?ng chi?u, layout gh? (static data, ?t thay ??i).

Ubiquitous Language:
- Cinema = m?t r?p chi?u phim (??a ?i?m v?t l?)
- Screen = m?t ph?ng chi?u trong r?p
- SeatTemplate = c?u h?nh gh? c?a ph?ng (row, number, category)
- SeatCategory = STANDARD | VIP | COUPLE | WHEELCHAIR

Aggregates ch?nh: Cinema, Screen
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? BC5: Payment Context → payment-service

Tr?ch nhi?m: T?ch h?p PSP (VNPay, Momo), qu?n l? giao d?ch, refund. ACL v?i external PSP.

Ubiquitous Language:
- Payment = m?t giao d?ch thanh to?n g?n v?i 1 booking
- PaymentMethod = VNPAY | MOMO | CREDIT_CARD | BANK_TRANSFER
- PaymentStatus = PENDING | PROCESSING | COMPLETED | FAILED | REFUNDED
- Refund = y?u c?u ho?n ti?n (partial ho?c full)
- PSPTransaction = value object ch?a data t? external PSP (ACL boundary)

Aggregates ch?nh: Payment, Refund
                  
---                                                                                                                                                                                                                     
? BC6: Ticket Context → ticket-service

Tr?ch nhi?m: Ph?t h?nh v? QR, validate t?i r?p, track usage.

Ubiquitous Language:
- Ticket = v? ?i?n t? cho 1 gh? trong 1 showtime
- QRCode = value object ch?a encoded data ?? scan t?i r?p
- TicketStatus = ISSUED | USED | EXPIRED | VOIDED

Aggregates ch?nh: Ticket
                                                                                                                                                                                                                          
---             
? BC7: Promotion Context → promotion-service

Tr?ch nhi?m: Qu?n l? khuy?n m?i, voucher, validate/redeem trong booking flow.

Ubiquitous Language:
- Promotion = ch??ng tr?nh khuy?n m?i (% ho?c fixed amount)
- Voucher = m? code c? th? thu?c v? 1 promotion
- VoucherUsage = l?n s? d?ng voucher (idempotency critical)
- DiscountResult = k?t qu? t?nh gi?m gi? (value object tr? v?)

Aggregates ch?nh: Promotion, Voucher
                                                                                                                                                                                                                          
---             
? BC8: Notification Context → notification-service

Tr?ch nhi?m: G?i email, push notification theo events. Pure consumer, kh?ng c? business logic ph?c t?p.

Ubiquitous Language:
- NotificationRequest = y?u c?u g?i th?ng b?o (t? event)
- NotificationChannel = EMAIL | PUSH | SMS
- NotificationTemplate = template n?i dung
- NotificationLog = log k?t qu? g?i (idempotency check)

Aggregates ch?nh: Notification (simple)
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? BC9: Identity Context → identity-service (thin wrapper around Keycloak)

Tr?ch nhi?m: Wrapper m?ng tr?n Keycloak ― handle user registration flow, profile enrichment, role management. Kh?ng ph?i thay th? Keycloak, ch? l? orchestration layer.

Ubiquitous Language:
- User = account ?? ??ng k?
- UserProfile = th?ng tin m? r?ng (phone, dob, preferences)
- Role = CUSTOMER | STAFF | ADMIN

Aggregates ch?nh: UserProfile
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? BC10: Chat Context → chat-service

Tr?ch nhi?m: WebSocket-based support chat gi?a customer v? staff.

Ubiquitous Language:
- Conversation = cu?c h?i tho?i
- Message = tin nh?n
- Participant = th?nh vi?n trong conversation (customer/agent)

  ---                                                                                                                                                                                                                     
1.4 Core Domain vs Supporting vs Generic Subdomain

quadrantChart   
title Subdomain Classification ― CinemaTix                                                                                                                                                                          
x-axis Low Business Value --> High Business Value                                                                                                                                                                   
y-axis Low Complexity --> High Complexity
quadrant-1 Core Domain ― Invest Most                                                                                                                                                                                
quadrant-2 Supporting ― Build Carefully                                                                                                                                                                             
quadrant-3 Generic ― Buy/Use OSS                                                                                                                                                                                    
quadrant-4 Supporting ― Build Simply

      Booking Saga: [0.90, 0.90]                                                                                                                                                                                          
      Seat Availability: [0.85, 0.80]                                                                                                                                                                                     
      Payment Integration: [0.70, 0.75]                                                                                                                                                                                   
      Promotion Engine: [0.65, 0.55]                                                                                                                                                                                      
      Movie Catalog: [0.55, 0.30]
      Cinema Management: [0.45, 0.25]                                                                                                                                                                                     
      Ticket Issuance: [0.60, 0.40]                                                                                                                                                                                       
      Identity/Auth: [0.30, 0.50]
      Notification: [0.25, 0.20]                                                                                                                                                                                          
      Chat Support: [0.35, 0.45]

Ph?n lo?i chi ti?t & khuy?n ngh? ??u t?:

┌───────────────────┬─────────────┬──────────────────────────────────────┬───────────────────────────────────────────────────────────────────────────────────┐                                                          
│     Subdomain     │    Lo?i     │              Chi?n l??c              │                                       L? do                                       │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤
│ Booking (Saga)    │ Core Domain │ Build in-house, invest maximum       │ Competitive advantage: UX ??t v? m??t m?, x? l? race condition gh?, saga ph?c t?p │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤
│ Seat Availability │ Core Domain │ Build in-house                       │ Real-time seat map l? differentiator, c?n control ho?n to?n Redis locking         │                                                          
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Payment           │ Supporting  │ Build v?i ACL, integrate PSP         │ Business c?n nh?ng kh?ng ph?i l?i th? c?nh tranh                                  │                                                          
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Promotion         │ Supporting  │ Build in-house                       │ Rule engine c?n flex theo business                                                │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Ticket/QR         │ Supporting  │ Build, c? th? d?ng lib QR            │ Quan tr?ng nh?ng kh?ng ph?c t?p                                                   │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Movie Catalog     │ Supporting  │ Build simple CRUD                    │ Content management, ?t logic                                                      │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Cinema Mgmt       │ Supporting  │ Build simple CRUD                    │ Master data, ?t thay ??i                                                          │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Identity/Auth     │ Generic     │ Use Keycloak                         │ Commodity, ??ng build auth from scratch                                           │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Notification      │ Generic     │ Build thin + d?ng SaaS (SendGrid)    │ Plumbing code, kh?ng t?o gi? tr?                                                  │
├───────────────────┼─────────────┼──────────────────────────────────────┼───────────────────────────────────────────────────────────────────────────────────┤                                                          
│ Chat Support      │ Generic     │ Build ho?c d?ng Intercom/Zendesk SDK │ Nice-to-have, kh?ng ph?i core                                                     │
└───────────────────┴─────────────┴──────────────────────────────────────┴───────────────────────────────────────────────────────────────────────────────────┘

?? Trade-offs quan tr?ng c?n ghi nh?:

1. T?ch booking-service v? showtime-service hay g?p?
- G?p: ??n gi?n h?n, tr?nh distributed transaction. Nh?ng violate SRP ― booking logic v? seat availability r?t kh?c nhau v? scalability pattern (booking: write-heavy saga; showtime: extreme read + real-time
  broadcast).
- T?ch (recommended): Showtime service scale ??c l?p (??c bi?t gi? cao ?i?m), team ownership r? r?ng. Chi ph?: c?n Saga + gRPC call.

2. C? n?n c? ticket-service ri?ng kh?ng?
- C?: Ticket issuance v? validation t?i r?p (QR scan) l? use case ??c l?p, c? th? offline-capable sau n?y. T?ch cho ph?p scale kiosk validation ri?ng.
- Kh?ng c? (alternative): Merge v?o booking-service ?? ??n gi?n h?n.
- Khuy?n ngh?: T?ch ― v? QR validation t?i r?p c?n availability cao v? c? th? c?n SDK ri?ng cho turnstile/kiosk.

3. identity-service thin wrapper hay full service?
- Keycloak ?? handle authentication/authorization r?t t?t. identity-service ch? n?n l? thin layer ?? sync user profile v?o domain (l?u UserProfile v?o Postgres c?a m?nh), kh?ng duplicate auth logic.

  ---                                                                                                                                                                                                                     

? Ch? ??ng nh?c: B?n ch?a ?? c?p ??n Review/Rating domain. T?i ?? ??a v?o diagram nh?ng mark l? "future". N?u launch MVP, n?n defer ?? t?p trung v?o core booking flow. T??ng t?, Chat n?n d?ng third-party (Intercom   
? free tier) cho MVP thay v? build t? ??u.
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
Phase 1 ho?n th?nh. G? "phase 2" ?? t?i tri?n khai High-Level Architecture v?i C4 diagram ??y ?? v? quy?t ??nh SYNC vs ASYNC cho t?ng service.

? Baked for 1m 23s

? phase 2

● PHASE 2 ― HIGH-LEVEL ARCHITECTURE

2.1 C4 Model Diagrams

Level 1 ― System Context Diagram

C4Context       
title CinemaTix ― System Context Diagram

      Person(customer, "Customer", "Ng??i d?ng ??t v? xem phim qua web/mobile")                                                                                                                                           
      Person(admin, "Cinema Admin", "Qu?n l? phim, r?p, su?t chi?u, khuy?n m?i")
      Person(staff, "Cinema Staff", "Scan QR t?i r?p, support chat")                                                                                                                                                      
                  
      System(cinetix, "CinemaTix Platform", "N?n t?ng ??t v? xem phim online ― microservices tr?n K8s")                                                                                                                   
                  
      System_Ext(keycloak, "Keycloak", "Identity Provider ― OAuth2/OIDC, SSO")                                                                                                                                            
      System_Ext(vnpay, "VNPay / Momo", "Payment Service Provider (PSP)")
      System_Ext(sendgrid, "SendGrid", "Email delivery service")                                                                                                                                                          
      System_Ext(firebase, "Firebase FCM", "Push notification (mobile)")
      System_Ext(s3, "AWS S3 / MinIO", "Object storage ― poster, QR images")                                                                                                                                              
                                                                                                                                                                                                                          
      Rel(customer, cinetix, "Browse phim, ch?n gh?, ??t v?, nh?n QR", "HTTPS/WSS")                                                                                                                                       
      Rel(admin, cinetix, "Qu?n l? n?i dung & c?u h?nh", "HTTPS")                                                                                                                                                         
      Rel(staff, cinetix, "Scan QR, h? tr? kh?ch h?ng", "HTTPS/WSS")                                                                                                                                                      
                                                                                                                                                                                                                          
      Rel(cinetix, keycloak, "Authenticate & authorize users", "HTTPS/OIDC")                                                                                                                                              
      Rel(cinetix, vnpay, "Process payments & refunds", "HTTPS/webhook")                                                                                                                                                  
      Rel(cinetix, sendgrid, "Send transactional emails", "HTTPS/SMTP")                                                                                                                                                   
      Rel(cinetix, firebase, "Send push notifications", "HTTPS")
      Rel(cinetix, s3, "Store & retrieve media files", "HTTPS/S3 API")                                                                                                                                                    
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
Level 2 ― Container Diagram

graph TB
subgraph CLIENTS["? Clients"]
WEB["Web App<br/>(React/Next.js)<br/>port: 3000"]                                                                                                                                                               
MOBILE["Mobile App<br/>(React Native)<br/>iOS / Android"]                                                                                                                                                       
ADMIN_UI["Admin Dashboard<br/>(React)<br/>port: 3001"]                                                                                                                                                          
end

      subgraph EDGE["? Edge Layer"]                                                                                                                                                                                      
          NGINX["NGINX Ingress<br/>TLS termination<br/>cert-manager"]
          GW["API Gateway<br/>spring-cloud-gateway<br/>port: 8080<br/>― Rate limiting<br/>― JWT validation<br/>― Routing<br/>― CORS"]                                                                                     
      end                                                                                                                                                                                                                 
   
      subgraph IDENTITY["? Identity"]                                                                                                                                                                                    
          KC["Keycloak<br/>port: 8180<br/>Postgres: kc_db"]
          ID_SVC["identity-service<br/>port: 8081<br/>Postgres: identity_db<br/>― User profile sync<br/>― Role management"]                                                                                               
      end                                                                                                                                                                                                                 
                                                                                                                                                                                                                          
      subgraph CORE["? Core Services"]                                                                                                                                                                                   
          BOOKING["booking-service<br/>port: 8082 (REST/WS)<br/>port: 9082 (gRPC)<br/>Postgres: booking_db<br/>Redis: booking_cache<br/>― Saga Orchestrator<br/>― CQRS write/read<br/>― Outbox publisher"]
          SHOWTIME["showtime-service<br/>port: 8083 (REST)<br/>port: 9083 (gRPC)<br/>Postgres: showtime_db<br/>Redis: seat_hold_cache<br/>― Seat availability<br/>― Seat hold TTL<br/>― WS broadcast"]                    
      end                                                                                                                                                                                                                 
                                                                                                                                                                                                                          
      subgraph SUPPORTING["?? Supporting Services"]                                                                                                                                                                       
          MOVIE["movie-service<br/>port: 8084<br/>Postgres: movie_db<br/>Redis: movie_cache<br/>― Movie CRUD<br/>― Search/Filter"]
          CINEMA["cinema-service<br/>port: 8085<br/>Postgres: cinema_db<br/>― Cinema/Screen CRUD<br/>― Seat layout config"]                                                                                               
          PAYMENT["payment-service<br/>port: 8086<br/>port: 9086 (gRPC)<br/>Postgres: payment_db<br/>― PSP integration ACL<br/>― Outbox publisher"]                                                                       
          TICKET["ticket-service<br/>port: 8087<br/>Postgres: ticket_db<br/>― QR generation<br/>― Ticket validation"]                                                                                                     
          PROMOTION["promotion-service<br/>port: 8088<br/>port: 9088 (gRPC)<br/>Postgres: promotion_db<br/>Redis: voucher_lock<br/>― Voucher engine"]                                                                     
          NOTIF["notification-service<br/>port: 8089<br/>Postgres: notif_db<br/>― Email (SendGrid)<br/>― Push (FCM)"]                                                                                                     
          CHAT["chat-service<br/>port: 8090 (REST/WS)<br/>Postgres: chat_db<br/>Redis: presence_cache<br/>― STOMP/WebSocket<br/>― Message history"]                                                                       
      end                                                                                                                                                                                                                 
                                                                                                                                                                                                                          
      subgraph INFRA["? Infrastructure"]                                                                                                                                                                                 
          MQ["RabbitMQ Cluster<br/>port: 5672 / 15672<br/>Topic Exchange:<br/>cinetix.events"]
          DEBEZIUM["Debezium Connect<br/>port: 8083<br/>CDC: WAL → RabbitMQ<br/>for booking + payment"]                                                                                                                   
          REDIS["Redis Cluster<br/>port: 6379<br/>― Seat holds<br/>― Distributed locks<br/>― Session cache"]                                                                                                              
          PG["PostgreSQL 16<br/>per-service DB<br/>port: 5432 (each)"]                                                                                                                                                    
      end                                                                                                                                                                                                                 
                                                                                                                                                                                                                          
      subgraph OBS["? Observability"]                                                                                                                                                                                    
          OTEL["OTel Collector<br/>port: 4317/4318"]
          PROM["Prometheus<br/>port: 9090"]                                                                                                                                                                               
          LOKI["Grafana Loki<br/>port: 3100"]
          TEMPO["Grafana Tempo<br/>port: 3200"]                                                                                                                                                                           
          GRAFANA["Grafana<br/>port: 3000"]
      end                                                                                                                                                                                                                 
                  
      %% Client → Edge                                                                                                                                                                                                    
      WEB & MOBILE & ADMIN_UI --> NGINX
      NGINX --> GW
      NGINX --> KC
                                                                                                                                                                                                                          
      %% Gateway → Services
      GW --> ID_SVC                                                                                                                                                                                                       
      GW --> BOOKING
      GW --> SHOWTIME
      GW --> MOVIE
      GW --> CINEMA
      GW --> TICKET                                                                                                                                                                                                       
      GW --> PROMOTION
      GW --> CHAT                                                                                                                                                                                                         
                  
      %% Sync gRPC (internal)                                                                                                                                                                                             
      BOOKING -. "gRPC: HoldSeats<br/>ReleaseSeats<br/>ConfirmSeats" .-> SHOWTIME
      BOOKING -. "gRPC: InitiatePayment<br/>RefundPayment" .-> PAYMENT                                                                                                                                                    
      BOOKING -. "gRPC: ValidateVoucher<br/>RedeemVoucher" .-> PROMOTION                                                                                                                                                  
                                                                                                                                                                                                                          
      %% Async RabbitMQ                                                                                                                                                                                                   
      BOOKING == "BookingConfirmed\nBookingCancelled" ==> MQ                                                                                                                                                              
      PAYMENT == "PaymentCompleted\nPaymentFailed" ==> MQ                                                                                                                                                                 
      SHOWTIME == "SeatsHeld\nSeatsReleased" ==> MQ
      MQ == "events" ==> TICKET                                                                                                                                                                                           
      MQ == "events" ==> NOTIF
      MQ == "events" ==> SHOWTIME                                                                                                                                                                                         
                                                                                                                                                                                                                          
      %% Debezium CDC
      BOOKING --> DEBEZIUM                                                                                                                                                                                                
      PAYMENT --> DEBEZIUM
      DEBEZIUM --> MQ
                                                                                                                                                                                                                          
      %% Infrastructure connections
      BOOKING & SHOWTIME & MOVIE & CINEMA & PAYMENT & TICKET & PROMOTION & NOTIF & CHAT --> PG                                                                                                                            
      BOOKING & SHOWTIME & PROMOTION & CHAT --> REDIS                                                                                                                                                                     
      KC --> PG
                                                                                                                                                                                                                          
      %% Observability
      BOOKING & SHOWTIME & PAYMENT & MOVIE & CINEMA & TICKET & PROMOTION & NOTIF & CHAT --> OTEL                                                                                                                          
      OTEL --> PROM & LOKI & TEMPO
      PROM & LOKI & TEMPO --> GRAFANA                                                                                                                                                                                     
   
      style BOOKING fill:#e74c3c,color:#fff                                                                                                                                                                               
      style SHOWTIME fill:#e74c3c,color:#fff
      style MQ fill:#e67e22,color:#fff                                                                                                                                                                                    
      style DEBEZIUM fill:#e67e22,color:#fff
                                                                                                                                                                                                                          
---
2.2 Microservice Catalog ― Full Specification

B?ng t?ng h?p to?n b? services:

┌──────────────────────┬───────────┬───────────┬──────────────┬────────────────────────────────┬───────────────────────────────┬──────────────────────────┐                                                             
│       Service        │ HTTP Port │ gRPC Port │   Database   │         External Deps          │         Giao th?c In          │      Giao th?c Out       │                                                             
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ api-gateway          │ 8080      │ ―         │ ―            │ Keycloak (token introspection) │ REST/HTTP2, WSS               │ REST, gRPC, WS           │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤
│ identity-service     │ 8081      │ ―         │ identity_db  │ Keycloak Admin API             │ REST                          │ Keycloak REST            │                                                             
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ booking-service      │ 8082      │ 9082      │ booking_db   │ Redis, RabbitMQ                │ REST, gRPC                    │ gRPC (out), RabbitMQ, WS │                                                             
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ showtime-service     │ 8083      │ 9083      │ showtime_db  │ Redis, RabbitMQ                │ REST, gRPC (in)               │ RabbitMQ, WS broadcast   │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ movie-service        │ 8084      │ ―         │ movie_db     │ Redis, S3                      │ REST                          │ ―                        │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ cinema-service       │ 8085      │ ―         │ cinema_db    │ ―                              │ REST                          │ ―                        │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ payment-service      │ 8086      │ 9086      │ payment_db   │ VNPay/Momo, RabbitMQ           │ gRPC (in), REST (PSP webhook) │ RabbitMQ, REST (PSP)     │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ ticket-service       │ 8087      │ ―         │ ticket_db    │ S3 (QR storage), RabbitMQ      │ RabbitMQ (in)                 │ RabbitMQ, S3             │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ promotion-service    │ 8088      │ 9088      │ promotion_db │ Redis                          │ gRPC (in)                     │ ―                        │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ notification-service │ 8089      │ ―         │ notif_db     │ SendGrid, FCM, RabbitMQ        │ RabbitMQ (in)                 │ SendGrid API, FCM API    │
├──────────────────────┼───────────┼───────────┼──────────────┼────────────────────────────────┼───────────────────────────────┼──────────────────────────┤                                                             
│ chat-service         │ 8090      │ ―         │ chat_db      │ Redis                          │ REST, WS/STOMP                │ WS/STOMP                 │
└──────────────────────┴───────────┴───────────┴──────────────┴────────────────────────────────┴───────────────────────────────┴──────────────────────────┘

Chi ti?t t?ng service:

booking-service (Core ― highest complexity)                                                                                                                                                                             
├── REST endpoints (via Gateway):
│   POST   /api/v1/bookings                    ← t?o booking m?i                                                                                                                                                        
│   GET    /api/v1/bookings/{id}               ← get booking detail                                                                                                                                                     
│   DELETE /api/v1/bookings/{id}               ← cancel booking                                                                                                                                                         
│   GET    /api/v1/bookings/my                 ← list my bookings                                                                                                                                                       
│   POST   /api/v1/bookings/{id}/apply-voucher ← apply promo code
├── gRPC server: kh?ng expose (ch? call out)                                                                                                                                                                            
├── gRPC client calls:                                                                                                                                                                                                  
│   → showtime-service: HoldSeats, ReleaseSeats, ConfirmSeats                                                                                                                                                           
│   → payment-service:  InitiatePayment, RefundPayment                                                                                                                                                                  
│   → promotion-service: ValidateVoucher, RedeemVoucher                                                                                                                                                                 
├── RabbitMQ publish (via Outbox):                                                                                                                                                                                      
│   booking.events.initiated.v1                                                                                                                                                                                         
│   booking.events.confirmed.v1                                                                                                                                                                                         
│   booking.events.cancelled.v1                                                                                                                                                                                         
│   booking.events.expired.v1                                                                                                                                                                                           
├── WebSocket subscribe (consume from showtime-service broadcast):
│   /topic/booking/{bookingId}/status                                                                                                                                                                                   
└── Dependencies: booking_db (Postgres), booking_cache (Redis)

showtime-service (Core ― high throughput)                                                                                                                                                                               
├── REST endpoints:                                                                                                                                                                                                     
│   GET  /api/v1/showtimes?movieId=&date=&cinemaId=
│   GET  /api/v1/showtimes/{id}                                                                                                                                                                                         
│   GET  /api/v1/showtimes/{id}/seats          ← snapshot seat map
│   WS   /ws/showtimes/{id}/seats              ← real-time seat updates                                                                                                                                                 
├── gRPC server (port 9083):                                                                                                                                                                                            
│   ShowtimeService.GetShowtime(id)                                                                                                                                                                                     
│   ShowtimeService.HoldSeats(showtimeId, seatIds, bookingId, ttlSeconds)                                                                                                                                               
│   ShowtimeService.ReleaseSeats(showtimeId, seatIds, bookingId)                                                                                                                                                        
│   ShowtimeService.ConfirmSeats(showtimeId, seatIds, bookingId)                                                                                                                                                        
├── RabbitMQ publish (via Outbox):                                                                                                                                                                                      
│   showtime.events.seats_held.v1                                                                                                                                                                                       
│   showtime.events.seats_released.v1                                                                                                                                                                                   
│   showtime.events.seats_confirmed.v1
└── Redis: HASH key = seat:{showtimeId}:{seatId} → {status, bookingId, expiresAt}                                                                                                                                       
SORTED SET = seat_holds:{showtimeId} (score = expiresAt timestamp)
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
2.3 Lu?ng D? Li?u Ch?nh ― ??t V? End-to-End

sequenceDiagram
actor User                                                                                                                                                                                                          
participant GW as API Gateway                                                                                                                                                                                       
participant BK as booking-service
participant ST as showtime-service                                                                                                                                                                                  
participant PR as promotion-service
participant PY as payment-service                                                                                                                                                                                   
participant TK as ticket-service
participant NT as notification-service                                                                                                                                                                              
participant MQ as RabbitMQ
participant RD as Redis

      Note over User, RD: ? BROWSE & SELECT SEATS                                                                                                                                                                         
      User->>GW: GET /showtimes/{id}/seats
      GW->>ST: GET /showtimes/{id}/seats                                                                                                                                                                                  
      ST->>RD: HGETALL seat:{showtimeId}:*
      ST-->>User: SeatMap snapshot                                                                                                                                                                                        
                                                                                                                                                                                                                          
      User->>GW: WS CONNECT /ws/showtimes/{id}/seats                                                                                                                                                                      
      Note over GW, ST: WebSocket connection opened ― real-time updates                                                                                                                                                   
                                                                                                                                                                                                                          
      Note over User, RD: ? INITIATE BOOKING (Saga starts)
      User->>GW: POST /bookings {showtimeId, seatIds[], voucherCode?}                                                                                                                                                     
      GW->>BK: POST /bookings (JWT validated by Gateway)                                                                                                                                                                  
      BK->>BK: Create Booking aggregate (status=INITIATED)                                                                                                                                                                
      BK->>BK: Persist to DB + write Outbox record                                                                                                                                                                        
                                                                                                                                                                                                                          
      Note over BK, ST: ? STEP 1 ― HOLD SEATS (gRPC, sync)                                                                                                                                                                
      BK->>ST: gRPC HoldSeats(showtimeId, seatIds, bookingId, ttl=600s)                                                                                                                                                   
      ST->>RD: SET seat:{showtimeId}:{seatId} = {HELD, bookingId, expiresAt}                                                                                                                                              
      ST->>RD: ZADD seat_holds:{showtimeId} expiresAt bookingId                                                                                                                                                           
      ST->>MQ: Publish SeatsHeld event (via Outbox)                                                                                                                                                                       
      MQ-->>GW: Broadcast /topic/showtime/{id}/seats (WS ― other users see seats as HELD)                                                                                                                                 
      ST-->>BK: HoldSeatsResponse {success: true, expiresAt}                                                                                                                                                              
      BK->>BK: Update Booking status = SEATS_HELD                                                                                                                                                                         
                                                                                                                                                                                                                          
      Note over BK, PR: ? STEP 2 ― VALIDATE VOUCHER (gRPC, sync, optional)                                                                                                                                                
      alt voucherCode present                                                                                                                                                                                             
          BK->>PR: gRPC ValidateVoucher(code, userId, totalAmount)                                                                                                                                                        
          PR->>PR: Check expiry, usage limit, eligibility                                                                                                                                                                 
          PR-->>BK: DiscountResult {discountAmount, finalAmount}                                                                                                                                                          
          BK->>BK: Apply discount to Booking                                                                                                                                                                              
      end                                                                                                                                                                                                                 
                                                                                                                                                                                                                          
      Note over BK, PY: ? STEP 3 ― INITIATE PAYMENT (gRPC, sync)                                                                                                                                                          
      BK->>PY: gRPC InitiatePayment(bookingId, amount, method, userId)
      PY->>PY: Create Payment record (status=PENDING)                                                                                                                                                                     
      PY->>PY: Call PSP API (VNPay/Momo) → get payment URL                                                                                                                                                                
      PY-->>BK: PaymentResponse {paymentId, paymentUrl, expiresAt}                                                                                                                                                        
      BK->>BK: Update Booking status = PAYMENT_PENDING                                                                                                                                                                    
      BK-->>User: 200 OK {bookingId, paymentUrl, seatHoldExpiresAt}                                                                                                                                                       
                                                                                                                                                                                                                          
      Note over User, PY: ? USER PAYS (external PSP flow)                                                                                                                                                                 
      User->>PY: Redirected to VNPay/Momo page                                                                                                                                                                            
      User-->>PY: PSP Webhook callback (IPN)                                                                                                                                                                              
      PY->>PY: Verify HMAC signature (ACL anti-corruption)                                                                                                                                                                
      PY->>PY: Update Payment status = COMPLETED                                                                                                                                                                          
      PY->>MQ: Publish PaymentCompleted event (via Outbox)                                                                                                                                                                
                                                                                                                                                                                                                          
      Note over MQ, BK: ? STEP 4 ― CONFIRM BOOKING (async, event-driven)                                                                                                                                                  
      MQ->>BK: Consume PaymentCompleted {paymentId, bookingId}                                                                                                                                                            
      BK->>BK: Saga: validate bookingId matches                                                                                                                                                                           
      BK->>ST: gRPC ConfirmSeats(showtimeId, seatIds, bookingId)
      ST->>RD: SET seat:{showtimeId}:{seatId} = {CONFIRMED}                                                                                                                                                               
      ST->>MQ: Broadcast SeatsConfirmed → WS /topic/showtime/{id}/seats
      BK->>PR: gRPC RedeemVoucher(code, bookingId) [if voucher was used]                                                                                                                                                  
      BK->>BK: Update Booking status = CONFIRMED                                                                                                                                                                          
      BK->>MQ: Publish BookingConfirmed event (via Outbox)                                                                                                                                                                
      BK->>GW: Push WS /user/queue/booking/{bookingId} {status: CONFIRMED}                                                                                                                                                
                                                                                                                                                                                                                          
      Note over MQ, NT: ? DOWNSTREAM ― TICKET + NOTIFICATION (async)
      MQ->>TK: Consume BookingConfirmed                                                                                                                                                                                   
      TK->>TK: Generate QR Code per seat                                                                                                                                                                                  
      TK->>TK: Store Ticket records                                                                                                                                                                                       
      TK->>MQ: Publish TicketIssued {ticketIds[], qrUrls[]}                                                                                                                                                               
                                                                                                                                                                                                                          
      MQ->>NT: Consume TicketIssued
      NT->>NT: Render email template with QR codes                                                                                                                                                                        
      NT->>NT: Send via SendGrid + FCM push                                                                                                                                                                               
                                                                                                                                                                                                                          
      Note over User, NT: ? COMPENSATION FLOW (if PaymentFailed)                                                                                                                                                          
      MQ-->>BK: Consume PaymentFailed {bookingId}                                                                                                                                                                         
      BK->>ST: gRPC ReleaseSeats(showtimeId, seatIds, bookingId)                                                                                                                                                          
      ST->>RD: DEL seat:{showtimeId}:{seatId} hold                                                                                                                                                                        
      ST->>MQ: Publish SeatsReleased → WS broadcast (seats AVAILABLE again)                                                                                                                                               
      BK->>BK: Update Booking status = CANCELLED                                                                                                                                                                          
      BK->>MQ: Publish BookingCancelled event                                                                                                                                                                             
      MQ->>NT: Send cancellation email to user                                                                                                                                                                            
                                                                                                                                                                                                                          
---             
2.4 Quy?t ??nh SYNC (gRPC) vs ASYNC (RabbitMQ)

Framework ra quy?t ??nh:

D?ng SYNC (gRPC) khi:
? Caller c?n k?t qu? NGAY ?? ti?p t?c (blocking operation)                                                                                                                                                            
? Failure c?a callee ph?i block caller (c?n bi?t ngay ?? rollback)                                                                                                                                                    
? Latency y?u c?u < 100ms                                                                                                                                                                                             
? Strong consistency b?t bu?c t?i th?i ?i?m ??                                                                                                                                                                       ─

D?ng ASYNC (RabbitMQ) khi:                                                                                                                                                                                              
? Caller kh?ng c?n k?t qu? ngay (fire-and-forget ho?c eventual)
? Fan-out: 1 event → N consumers                                                                                                                                                                                      
? Temporal decoupling: consumer c? th? down t?m th?i                                                                                                                                                                  
? Failure tolerance: retry v?i DLQ                                                                                                                                                                                   ─
? Cross-service choreography sau m?t b??c saga ho?n th?nh

B?ng quy?t ??nh chi ti?t:

graph LR        
subgraph SYNC_GRPC["? SYNC ― gRPC (Blocking, Strong Consistency)"]
S1["Booking → Showtime<br/>HoldSeats()<br/>? PH?I bi?t ngay gh? c? available kh?ng\nN?u kh?ng hold ???c → abort ngay, kh?ng t?o booking"]                                                                      
S2["Booking → Showtime<br/>ConfirmSeats()<br/>? Sau payment success, ph?i confirm ngay\ntr??c khi publish event downstream"]                                                                                   
S3["Booking → Showtime<br/>ReleaseSeats()<br/>? Compensation step, ph?i ??ng b?\ntr? gh? v? AVAILABLE ngay l?p t?c"]                                                                                           
S4["Booking → Payment<br/>InitiatePayment()<br/>? Ph?i c? paymentUrl tr? v? user\nUser c?n redirect ngay sang PSP"]                                                                                            
S5["Booking → Payment<br/>RefundPayment()<br/>? Compensation c?n bi?t refund ??\ninitiated hay ch?a ?? update saga state"]                                                                                     
S6["Booking → Promotion<br/>ValidateVoucher()<br/>? Ph?i bi?t voucher c? valid kh?ng\ntr??c khi t?nh to?n final amount"]                                                                                       
S7["Booking → Promotion<br/>RedeemVoucher()<br/>? Mark voucher ?? d?ng ngay khi\nconfirm ― tr?nh race condition"]                                                                                              
end

      subgraph ASYNC_MQ["? ASYNC ― RabbitMQ (Eventual Consistency)"]                                                                                                                                                     
          A1["Payment → Booking<br/>PaymentCompleted/Failed<br/>? PSP callback l? async webhook\nBooking ch? event, kh?ng block"]
          A2["Booking → Ticket<br/>BookingConfirmed<br/>? QR generation c? th? ch?m\nUser ?? nh?n booking confirmed\nticket ??n sau v?i gi?y l? OK"]                                                                     
          A3["Booking → Notification<br/>BookingConfirmed/Cancelled<br/>? Email g?i sau v?i gi?y ch?p nh?n\n1 event → N channels (email + push)\nFail email kh?ng ???c rollback booking"]                                
          A4["Showtime → WebSocket\nSeatsHeld/Released/Confirmed<br/>? Fan-out ??n t?t c? clients\nbrowsing showtime ??"]                                                                                                
          A5["Ticket → Notification<br/>TicketIssued<br/>? Notification sau khi QR s?n s?ng\nsequential pipeline, OK async"]                                                                                             
          A6["Booking/Payment → Debezium\nOutbox table → RabbitMQ<br/>? Transactional guarantee\nkh?ng m?t event khi service crash"]                                                                                     
      end                                                                                                                                                                                                                 

B?ng t?m t?t:

┌──────────────────┬────────────────────────┬──────────┬─────────────────────────────────────────────────────────────────────┐                                                                                          
│        Call        │       Direction        │   Protocol    │                           L? do k? thu?t                            │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤
│ HoldSeats          │ Booking → Showtime     │ gRPC          │ Race condition: 2 users c?ng ch?n 1 gh?, ph?i atomic check-and-lock │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤
│ ConfirmSeats       │ Booking → Showtime     │ gRPC          │ Ph?i confirm tr??c khi issue ticket ― strong ordering               │                                                                                   
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ ReleaseSeats       │ Booking → Showtime     │ gRPC          │ Compensation ph?i ??ng b?, kh?ng ?? gh? "zombie"                    │                                                                                   
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ InitiatePayment    │ Booking → Payment      │ gRPC          │ Ph?i c? paymentUrl ?? redirect user, blocking                       │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ RefundPayment      │ Booking → Payment      │ gRPC          │ Saga compensation, ph?i confirm initiated                           │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ ValidateVoucher    │ Booking → Promotion    │ gRPC          │ Calculate final amount tr??c khi initiate payment                   │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ RedeemVoucher      │ Booking → Promotion    │ gRPC          │ Atomic mark-as-used v?i booking confirmation                        │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ PaymentCompleted   │ Payment → Booking      │ RabbitMQ      │ PSP webhook v?n l? async; Booking poll hay event ― event t?t h?n    │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ PaymentFailed      │ Payment → Booking      │ RabbitMQ      │ Same as above                                                       │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ BookingConfirmed   │ Booking → Ticket       │ RabbitMQ      │ QR generation l? CPU-bound, kh?ng c?n blocking                      │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ BookingConfirmed   │ Booking → Notification │ RabbitMQ      │ Fan-out, eventual delivery, failure isolated                        │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ SeatsHeld/Released │ Showtime → WS clients  │ RabbitMQ → WS │ Fan-out ??n N browser clients qua STOMP broker                      │
├────────────────────┼────────────────────────┼───────────────┼─────────────────────────────────────────────────────────────────────┤                                                                                   
│ TicketIssued       │ Ticket → Notification  │ RabbitMQ      │ Pipeline step, eventual OK                                          │
└────────────────────┴────────────────────────┴───────────────┴─────────────────────────────────────────────────────────────────────┘

?? Nh?ng ?i?m b?n c? th? ch?a ngh? t?i:

1. Circuit Breaker cho gRPC calls:                                                                                                                                                                                      
   booking-service g?i showtime-service qua gRPC.
   N?u showtime-service b? qu? t?i → booking-service b? cascade failure.

→ C?n Resilience4j CircuitBreaker + Retry + Bulkhead tr?n m?i gRPC client.                                                                                                                                              
→ Config: CLOSED → OPEN sau 5 failures/10s → HALF_OPEN sau 30s

2. Dead Letter Queue (DLQ) cho m?i consumer:                                                                                                                                                                            
   M?i queue trong RabbitMQ ph?i c? DLQ policy.                                                                                                                                                                            
   Message fail 3 l?n → v?o DLQ.                                                                                                                                                                                           
   Alert Grafana khi DLQ c? message.                                                                                                                                                                                       
   Kh?ng ???c ?? message b? discard silently.

3. Idempotency tr?n t?t c? event consumers:                                                                                                                                                                             
   RabbitMQ ?t nh?t 1 l?n delivery (at-least-once).                                                                                                                                                                        
   Consumer ph?i check idempotency key tr??c khi process.
   Table: processed_events(event_id UUID, processed_at TIMESTAMP)

4. API Gateway: Rate Limiting per user:                                                                                                                                                                                 
   POST /bookings → 5 requests/minute per userId                                                                                                                                                                           
   POST /payments → 10 requests/minute per userId                                                                                                                                                                          
   D?ng Redis token bucket trong Spring Cloud Gateway.                                                                                                                                                                     
   Tr?nh DoS b?ng c?ch t?o flood booking requests.

5. Graceful Shutdown:                                                                                                                                                                                                   
   M?i service c?n:
- preStop hook: sleep 5s (cho load balancer drain connections)
- terminationGracePeriodSeconds: 30
- ??ng RabbitMQ consumer tr??c, drain in-flight messages
- ??ng gRPC server sau khi RPCs hi?n t?i ho?n th?nh

6. mTLS v?i Istio ― C? n?n d?ng kh?ng?                                                                                                                                                                                  
   ? N?n d?ng n?u:                                                                                                                                                                                                         
   - Multi-te?ant, services c?a team kh?c c?ng cluster                                                                                                                                                                   
   - Compliance y?u c?u (PCI-DSS cho payment)                                                                                                                                                                            
   - Production tr?n GKE/EKS public cloud

?? Trade-off:                                                                                                                                                                                                           
- Th?m ~1-2ms latency per call (Envoy sidecar overhead)                                                                                                                                                               
- Debug ph?c t?p h?n (c?n istioctl, kh?ng xem ???c plain traffic)                                                                                                                                                     
- T?n th?m RAM: m?i pod c? th?m Envoy sidecar ~50-100MB

? Khuy?n ngh?: Enable mTLS PERMISSIVE mode ban ??u                                                                                                                                                                     
(accept both plain v? mTLS), chuy?n sang STRICT khi stable.                                                                                                                                                           
??c bi?t b?t bu?c cho payment-service ? booking-service.
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
Phase 2 ho?n th?nh. G? "phase 3" ?? t?i ?i s?u v?o Booking Saga ― ph?n ph?c t?p nh?t c?a h? th?ng ― v?i state machine ??y ??, Outbox schema, v? timeout/retry strategy.

? Baked for 1m 42s

? phase 3

● PHASE 3 ― DETAILED DESIGN: BOOKING SAGA

3.1 Saga Orchestration Flow ― Full State Machine + Compensations

T?ng quan lu?ng Happy Path & Compensation:

stateDiagram-v2                                                                                                                                                                                                         
[*] --> INITIATED : POST /bookings

      INITIATED --> SEATS_HELD : HoldSeats() OK
      INITIATED --> FAILED : HoldSeats() FAILED\n(seats unavailable)                                                                                                                                                      
                                                                                                                                                                                                                          
      SEATS_HELD --> VOUCHER_APPLIED : ValidateVoucher() OK
      SEATS_HELD --> PAYMENT_PENDING : No voucher                                                                                                                                                                         
      SEATS_HELD --> COMPENSATING : ValidateVoucher() FAILED                                                                                                                                                              
      VOUCHER_APPLIED --> PAYMENT_PENDING : InitiatePayment() OK                                                                                                                                                          
      VOUCHER_APPLIED --> COMPENSATING : InitiatePayment() FAILED                                                                                                                                                         
                                                                                                                                                                                                                          
      PAYMENT_PENDING --> CONFIRMING : PaymentCompleted event
      PAYMENT_PENDING --> COMPENSATING : PaymentFailed event                                                                                                                                                              
      PAYMENT_PENDING --> COMPENSATING : 10min TTL expired                                                                                                                                                                
   
      CONFIRMING --> CONFIRMED : ConfirmSeats() OK\n+ RedeemVoucher() OK                                                                                                                                                  
      CONFIRMING --> COMPENSATING : ConfirmSeats() FAILED\n(rare ― seats already held)
                                                                                                                                                                                                                          
      CONFIRMED --> [*] : BookingConfirmed published\nDownstream: Ticket + Notification                                                                                                                                   
                                                                                                                                                                                                                          
      COMPENSATING --> CANCELLED : All compensations done\nSeatsReleased + VoucherVoided\n+ RefundInitiated (if paid)                                                                                                     
                  
      CANCELLED --> [*]                                                                                                                                                                                                   
      FAILED --> [*]
                                                                                                                                                                                                                          
      note right of PAYMENT_PENDING
          Redis TTL = 600s (10 ph?t)
          Quartz job poll m?i 30s                                                                                                                                                                                         
          check expired bookings
      end note                                                                                                                                                                                                            
                  
      note right of COMPENSATING                                                                                                                                                                                          
          Compensation l? sequential:
          1. RefundPayment (n?u ?? thanh to?n)                                                                                                                                                                            
          2. ReleaseSeats
          3. VoidVoucher (n?u ?? redeem)                                                                                                                                                                                  
      end note    

Chi ti?t t?ng b??c + Compensation:

flowchart TD                                                                                                                                                                                                            
START([User g?i POST /bookings]) --> S0

      S0["Step 0: Create Booking\n―――――――――\n? T?o Booking aggregate\n? status = INITIATED\n? correlationId = UUID\n? Write to booking_db\n? Write to outbox table\n? Return 202 Accepted + bookingId"]                   
                                                                                                                                                                                                                          
      S0 --> S1                                                                                                                                                                                                           
                  
      S1{"Step 1: Hold Seats\ngRPC → showtime-service\n―――――――――\nHoldSeats(showtimeId, seatIds[],\nbookingId, ttl=600s)"}                                                                                                
   
      S1 -->|SUCCESS| S1_OK["Redis: HSET seat:showtime:seatId\n= HELD, bookingId, expiresAt\nBooking: status = SEATS_HELD"]                                                                                               
      S1 -->|SEATS_TAKEN| C1["? Compensation C1\nBooking: status = FAILED\nReason: SEATS_UNAVAILABLE\n→ Kh?ng c?n release\n→ Publish BookingFailed event"]
                                                                                                                                                                                                                          
      S1_OK --> S2                                                                                                                                                                                                        
                                                                                                                                                                                                                          
      S2{"Step 2: Validate Voucher\ngRPC → promotion-service\n[OPTIONAL ― n?u c? voucher]\n―――――――――\nValidateVoucher(code, userId,\ntotalAmount)"}                                                                       
                  
      S2 -->|VALID| S2_OK["discountAmount applied\nBooking: status = VOUCHER_APPLIED\nfinalAmount calculated"]                                                                                                            
      S2 -->|INVALID/EXPIRED| C2["? Compensation C2\n1. ReleaseSeats() gRPC\n   showtime: HELD→AVAILABLE\n2. Booking: status = FAILED\nReason: INVALID_VOUCHER"]
      S2 -->|NO_VOUCHER| S3                                                                                                                                                                                               
                                                                                                                                                                                                                          
      S2_OK --> S3                                                                                                                                                                                                        
                                                                                                                                                                                                                          
      S3{"Step 3: Initiate Payment\ngRPC → payment-service\n―――――――――\nInitiatePayment(bookingId,\namount, method, userId)"}                                                                                              
   
      S3 -->|SUCCESS| S3_OK["Payment record: PENDING\npaymentUrl returned\nBooking: status = PAYMENT_PENDING\nQuartz job scheduled: T+600s"]                                                                              
      S3 -->|PSP_ERROR| C3["? Compensation C3\n1. ReleaseSeats() gRPC\n2. VoidVoucher() [if applied]\n3. Booking: status = FAILED\nReason: PAYMENT_INIT_FAILED"]
                                                                                                                                                                                                                          
      S3_OK --> WAIT_PAY
                                                                                                                                                                                                                          
      WAIT_PAY(["? Waiting for Payment...\nUser on PSP page\nTTL countdown: 10 min"])                                                                                                                                    ─
   
      WAIT_PAY -->|"PaymentCompleted\n(RabbitMQ event)"| S4                                                                                                                                                               
      WAIT_PAY -->|"PaymentFailed\n(RabbitMQ event)"| C4A
      WAIT_PAY -->|"TTL Expired\n(Quartz job)"| C4B                                                                                                                                                                       
                  
      C4A["? Compensation C4A\n1. Booking: status=COMPENSATING\n2. ReleaseSeats() gRPC\n3. VoidVoucher() [if applied]\n4. Booking: status=CANCELLED\nReason: PAYMENT_FAILED"]                                            ─
                  
      C4B["? Compensation C4B\n1. Booking: status=COMPENSATING\n2. CancelPayment() gRPC\n3. ReleaseSeats() gRPC\n4. VoidVoucher() [if applied]\n5. Booking: status=CANCELLED\nReason: PAYMENT_TIMEOUT"]                  ─
                  
      S4{"Step 4: Confirm Seats\ngRPC → showtime-service\n―――――――――\nConfirmSeats(showtimeId,\nseatIds[], bookingId)"}                                                                                                    
                  
      S4 -->|SUCCESS| S5                                                                                                                                                                                                  
      S4 -->|FAILED| C5["? Compensation C5 (RARE)\nSeats ?? HELD b?i m?nh\nn?n ConfirmSeats g?n nh? kh?ng fail\nN?u fail: Refund + Release\nBooking: CANCELLED"]
                                                                                                                                                                                                                          
      S5{"Step 5: Redeem Voucher\ngRPC → promotion-service\n[if voucher was applied]\n―――――――――\nRedeemVoucher(code, bookingId)"}                                                                                         
                                                                                                                                                                                                                          
      S5 -->|SUCCESS| S6                                                                                                                                                                                                  
      S5 -->|FAILED| C5

      S6["Step 6: Finalize\n―――――――――\n? Booking: status = CONFIRMED\n? Write BookingConfirmed to outbox\n? Debezium picks up → RabbitMQ\n? Downstream async:\n  → ticket-service (issue QR)\n  → notification-service    
(email+push)"]

      S6 --> END([? Booking Complete])                                                                                                                                                                                   ─
   
      style S0 fill:#3498db,color:#fff                                                                                                                                                                                    
      style S6 fill:#27ae60,color:#fff
      style C1 fill:#e74c3c,color:#fff                                                                                                                                                                                    
      style C2 fill:#e74c3c,color:#fff
      style C3 fill:#e74c3c,color:#fff                                                                                                                                                                                    
      style C4A fill:#e74c3c,color:#fff
      style C4B fill:#e74c3c,color:#fff                                                                                                                                                                                   
      style C5 fill:#e74c3c,color:#fff                                                                                                                                                                                    
      style END fill:#27ae60,color:#fff
                                                                                                                                                                                                                          
---             
3.2 State Machine ― Code Implementation

T?i khuy?n ngh? t? implement thay v? d?ng Spring StateMachine v?:
- Spring StateMachine config ph?c t?p, kh? debug trong distributed env
- Custom implementation d? unit test, d? persist state h?n
- ?t dependency, ?t "magic"

// domain/model/BookingStatus.java
public enum BookingStatus {                                                                                                                                                                                             
INITIATED,  
SEATS_HELD,                                                                                                                                                                                                         
VOUCHER_APPLIED,
PAYMENT_PENDING,                                                                                                                                                                                                    
CONFIRMING,
CONFIRMED,                                                                                                                                                                                                          
COMPENSATING,
CANCELLED,
FAILED;

      // ??nh ngh?a valid transitions ― enforce t?i Aggregate level                                                                                                                                                       
      private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = Map.of(
          INITIATED,        Set.of(SEATS_HELD, FAILED),                                                                                                                                                                   
          SEATS_HELD,       Set.of(VOUCHER_APPLIED, PAYMENT_PENDING, COMPENSATING),                                                                                                                                       
          VOUCHER_APPLIED,  Set.of(PAYMENT_PENDING, COMPENSATING),                                                                                                                                                        
          PAYMENT_PENDING,  Set.of(CONFIRMING, COMPENSATING),                                                                                                                                                             
          CONFIRMING,       Set.of(CONFIRMED, COMPENSATING),
          COMPENSATING,     Set.of(CANCELLED),                                                                                                                                                                            
          CONFIRMED,        Set.of(),  // terminal                                                                                                                                                                        
          CANCELLED,        Set.of(),  // terminal
          FAILED,           Set.of()   // terminal                                                                                                                                                                        
      );                                                                                                                                                                                                                  
   
      public boolean canTransitionTo(BookingStatus next) {                                                                                                                                                                
          return TRANSITIONS.getOrDefault(this, Set.of()).contains(next);
      }
}

// domain/model/Booking.java                                                                                                                                                                                            
@Entity         
@Table(name = "bookings")
public class Booking {

      @Id
      private BookingId id;                    // Value Object
                                                                                                                                                                                                                          
      @Embedded
      private CustomerId customerId;           // Value Object                                                                                                                                                            
                  
      @Embedded
      private ShowtimeId showtimeId;           // Value Object
                                                                                                                                                                                                                          
      @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
      private List<BookingItem> items;         // Entities                                                                                                                                                                
                                                                                                                                                                                                                          
      @Enumerated(EnumType.STRING)
      private BookingStatus status;                                                                                                                                                                                       
                  
      private Money subtotal;                  // Value Object (amount + currency)                                                                                                                                        
      private Money discountAmount;
      private Money finalAmount;                                                                                                                                                                                          
                  
      @Embedded                                                                                                                                                                                                           
      private VoucherCode appliedVoucher;      // Value Object (nullable)

      @Embedded
      private PaymentId paymentId;             // Value Object (nullable, set after initiate)
                                                                                                                                                                                                                          
      private Instant createdAt;
      private Instant expiresAt;               // createdAt + 10 min                                                                                                                                                      
      private Instant updatedAt;                                                                                                                                                                                          
   
      // Version cho Optimistic Locking ― CRITICAL cho concurrent updates                                                                                                                                                 
      @Version    
      private Long version;                                                                                                                                                                                               
                  
      @Transient
      private List<DomainEvent> domainEvents = new ArrayList<>();
                                                                                                                                                                                                                          
      // ─── Factory Method ──────────────────────────────────────────────────────
      public static Booking initiate(                                                                                                                                                                                     
              CustomerId customerId,                                                                                                                                                                                      
              ShowtimeId showtimeId,
              List<SeatSelection> selections,                                                                                                                                                                             
              Clock clock) {
                                                                                                                                                                                                                          
          var booking = new Booking();
          booking.id = BookingId.generate();                                                                                                                                                                              
          booking.customerId = customerId;
          booking.showtimeId = showtimeId;
          booking.status = BookingStatus.INITIATED;                                                                                                                                                                       
          booking.items = selections.stream()
              .map(s -> BookingItem.of(booking.id, s.seatId(), s.price(), s.category()))                                                                                                                                  
              .toList();                                                                                                                                                                                                  
          booking.subtotal = booking.calculateSubtotal();
          booking.discountAmount = Money.ZERO;                                                                                                                                                                            
          booking.finalAmount = booking.subtotal;
          booking.createdAt = clock.instant();                                                                                                                                                                            
          booking.expiresAt = booking.createdAt.plus(Duration.ofMinutes(10));                                                                                                                                             
          booking.updatedAt = booking.createdAt;
                                                                                                                                                                                                                          
          booking.registerEvent(new BookingInitiatedEvent(booking.id, customerId, showtimeId));                                                                                                                           
          return booking;
      }                                                                                                                                                                                                                   
                  
      // ─── State Transitions ───────────────────────────────────────────────────                                                                                                                                        
      public void markSeatsHeld(Instant heldAt) {
          transitionTo(BookingStatus.SEATS_HELD);                                                                                                                                                                         
          this.updatedAt = heldAt;                                                                                                                                                                                        
      }
                                                                                                                                                                                                                          
      public void applyVoucher(VoucherCode code, Money discount, Instant appliedAt) {                                                                                                                                     
          transitionTo(BookingStatus.VOUCHER_APPLIED);
          this.appliedVoucher = code;                                                                                                                                                                                     
          this.discountAmount = discount;                                                                                                                                                                                 
          this.finalAmount = subtotal.minus(discount);
          this.updatedAt = appliedAt;                                                                                                                                                                                     
      }           

      public void markPaymentPending(PaymentId paymentId, Instant markedAt) {                                                                                                                                             
          // SEATS_HELD ho?c VOUCHER_APPLIED ??u c? th? → PAYMENT_PENDING
          if (status != BookingStatus.SEATS_HELD && status != BookingStatus.VOUCHER_APPLIED) {                                                                                                                            
              throw new InvalidBookingStateException(id, status, BookingStatus.PAYMENT_PENDING);
          }                                                                                                                                                                                                               
          this.status = BookingStatus.PAYMENT_PENDING;
          this.paymentId = paymentId;                                                                                                                                                                                     
          this.updatedAt = markedAt;                                                                                                                                                                                      
      }
                                                                                                                                                                                                                          
      public void startConfirming(Instant confirmedAt) {
          transitionTo(BookingStatus.CONFIRMING);
          this.updatedAt = confirmedAt;
      }                                                                                                                                                                                                                   
   
      public void confirm(Instant confirmedAt) {                                                                                                                                                                          
          transitionTo(BookingStatus.CONFIRMED);
          this.updatedAt = confirmedAt;
          registerEvent(new BookingConfirmedEvent(
              id, customerId, showtimeId,                                                                                                                                                                                 
              items.stream().map(BookingItem::getSeatId).toList(),
              finalAmount, appliedVoucher, confirmedAt                                                                                                                                                                    
          ));     
      }                                                                                                                                                                                                                   
                  
      public void startCompensating(String reason, Instant at) {                                                                                                                                                          
          transitionTo(BookingStatus.COMPENSATING);
          this.updatedAt = at;                                                                                                                                                                                            
          registerEvent(new BookingCompensationStartedEvent(id, reason, at));
      }                                                                                                                                                                                                                   
   
      public void cancel(String reason, Instant cancelledAt) {                                                                                                                                                            
          transitionTo(BookingStatus.CANCELLED);
          this.updatedAt = cancelledAt;                                                                                                                                                                                   
          registerEvent(new BookingCancelledEvent(id, customerId, reason, cancelledAt));
      }
                                                                                                                                                                                                                          
      public void fail(String reason, Instant failedAt) {
          transitionTo(BookingStatus.FAILED);                                                                                                                                                                             
          this.updatedAt = failedAt;
          registerEvent(new BookingFailedEvent(id, reason, failedAt));
      }
                                                                                                                                                                                                                          
      public boolean isExpired(Clock clock) {
          return clock.instant().isAfter(expiresAt);                                                                                                                                                                      
      }           

      // ─── Private helpers ─────────────────────────────────────────────────────                                                                                                                                        
      private void transitionTo(BookingStatus next) {
          if (!status.canTransitionTo(next)) {                                                                                                                                                                            
              throw new InvalidBookingStateException(id, status, next);
          }                                                                                                                                                                                                               
          this.status = next;
      }

      private Money calculateSubtotal() {
          return items.stream()
              .map(BookingItem::getPrice)                                                                                                                                                                                 
              .reduce(Money.ZERO, Money::add);
      }                                                                                                                                                                                                                   
                  
      public void registerEvent(DomainEvent event) {
          domainEvents.add(event);
      }                                                                                                                                                                                                                   
   
      public List<DomainEvent> pullDomainEvents() {                                                                                                                                                                       
          var events = List.copyOf(domainEvents);
          domainEvents.clear();                                                                                                                                                                                           
          return events;
      }                                                                                                                                                                                                                   
}

// application/saga/BookingSagaOrchestrator.java
@Service                                                                                                                                                                                                                
@Transactional
@RequiredArgsConstructor                                                                                                                                                                                                
@Slf4j          
public class BookingSagaOrchestrator {

      private final BookingRepository bookingRepository;                                                                                                                                                                  
      private final ShowtimeGrpcClient showtimeClient;
      private final PaymentGrpcClient paymentClient;                                                                                                                                                                      
      private final PromotionGrpcClient promotionClient;
      private final OutboxEventPublisher outboxPublisher;                                                                                                                                                                 
      private final SagaStateRepository sagaStateRepository;
      private final Clock clock;                                                                                                                                                                                          
                  
      // ─── STEP 0: Initiate Saga ────────────────────────────────────────────────                                                                                                                                       
      public BookingId initiateBooking(InitiateBookingCommand cmd) {
          var booking = Booking.initiate(                                                                                                                                                                                 
              cmd.customerId(), cmd.showtimeId(), cmd.selections(), clock
          );                                                                                                                                                                                                              
          bookingRepository.save(booking);
                                                                                                                                                                                                                          
          // Persist saga state ?? track progress (separate table)                                                                                                                                                        
          var sagaState = SagaState.create(booking.getId(), SagaStep.HOLD_SEATS);                                                                                                                                         
          sagaStateRepository.save(sagaState);                                                                                                                                                                            
                  
          // Flush domain events to outbox (sync within same transaction)                                                                                                                                                 
          outboxPublisher.publishAll(booking.pullDomainEvents());
                                                                                                                                                                                                                          
          // Trigger step 1 ngay trong same thread (saga is in-process)                                                                                                                                                   
          executeHoldSeats(booking);
                                                                                                                                                                                                                          
          return booking.getId();
      }

      // ─── STEP 1: Hold Seats ───────────────────────────────────────────────────                                                                                                                                       
      private void executeHoldSeats(Booking booking) {
          var seatIds = booking.getItems().stream()                                                                                                                                                                       
              .map(BookingItem::getSeatId)
              .toList();                                                                                                                                                                                                  
   
          try {                                                                                                                                                                                                           
              var response = showtimeClient.holdSeats(HoldSeatsRequest.newBuilder()
                  .setShowtimeId(booking.getShowtimeId().value())                                                                                                                                                         
                  .addAllSeatIds(seatIds.stream().map(SeatId::value).toList())
                  .setBookingId(booking.getId().value().toString())                                                                                                                                                       
                  .setTtlSeconds(600)                                                                                                                                                                                     
                  .build());                                                                                                                                                                                              
                                                                                                                                                                                                                          
              if (response.getSuccess()) {
                  booking.markSeatsHeld(clock.instant());
                  bookingRepository.save(booking);                                                                                                                                                                        
                  updateSagaStep(booking.getId(), SagaStep.VALIDATE_VOUCHER);
                                                                                                                                                                                                                          
                  // Step 2 ngay l?p t?c
                  executeVoucherValidation(booking);                                                                                                                                                                      
              } else {
                  log.warn("HoldSeats failed for booking {}: {}", booking.getId(), response.getErrorMessage());
                  booking.fail("SEATS_UNAVAILABLE: " + response.getErrorMessage(), clock.instant());                                                                                                                      
                  bookingRepository.save(booking);                                                                                                                                                                        
                  outboxPublisher.publishAll(booking.pullDomainEvents());                                                                                                                                                 
              }                                                                                                                                                                                                           
          } catch (StatusRuntimeException e) {
              log.error("gRPC HoldSeats error for booking {}", booking.getId(), e);
              // Retry logic handled by Resilience4j ― n?u v?n fail sau retry:                                                                                                                                            
              booking.fail("SHOWTIME_SERVICE_UNAVAILABLE", clock.instant());                                                                                                                                              
              bookingRepository.save(booking);                                                                                                                                                                            
              outboxPublisher.publishAll(booking.pullDomainEvents());                                                                                                                                                     
          }                                                                                                                                                                                                               
      }
                                                                                                                                                                                                                          
      // ─── STEP 2: Validate Voucher (optional) ─────────────────────────────────
      private void executeVoucherValidation(Booking booking) {
          if (booking.getAppliedVoucher() == null) {
              // Kh?ng c? voucher → skip sang step 3                                                                                                                                                                      
              executeInitiatePayment(booking);
              return;                                                                                                                                                                                                     
          }       
                                                                                                                                                                                                                          
          try {   
              var response = promotionClient.validateVoucher(ValidateVoucherRequest.newBuilder()
                  .setCode(booking.getAppliedVoucher().value())
                  .setUserId(booking.getCustomerId().value().toString())                                                                                                                                                  
                  .setTotalAmount(booking.getSubtotal().amount().longValue())
                  .setCurrency(booking.getSubtotal().currency().getCurrencyCode())                                                                                                                                        
                  .build());
                                                                                                                                                                                                                          
              if (response.getValid()) {
                  booking.applyVoucher(
                      booking.getAppliedVoucher(),                                                                                                                                                                        
                      Money.of(response.getDiscountAmount(), "VND"),
                      clock.instant()                                                                                                                                                                                     
                  );
                  bookingRepository.save(booking);                                                                                                                                                                        
                  updateSagaStep(booking.getId(), SagaStep.INITIATE_PAYMENT);
                  executeInitiatePayment(booking);                                                                                                                                                                        
              } else {
                  compensate(booking, "INVALID_VOUCHER: " + response.getErrorMessage(),                                                                                                                                   
                      false, false);                                                                                                                                                                                      
              }
          } catch (StatusRuntimeException e) {                                                                                                                                                                            
              log.error("gRPC ValidateVoucher error", e);
              compensate(booking, "PROMOTION_SERVICE_UNAVAILABLE", false, false);                                                                                                                                         
          }
      }                                                                                                                                                                                                                   
                  
      // ─── STEP 3: Initiate Payment ─────────────────────────────────────────────
      private void executeInitiatePayment(Booking booking) {
          try {                                                                                                                                                                                                           
              var response = paymentClient.initiatePayment(InitiatePaymentRequest.newBuilder()
                  .setBookingId(booking.getId().value().toString())                                                                                                                                                       
                  .setAmount(booking.getFinalAmount().amount().longValue())
                  .setCurrency(booking.getFinalAmount().currency().getCurrencyCode())                                                                                                                                     
                  .setUserId(booking.getCustomerId().value().toString())
                  .build());                                                                                                                                                                                              
                                                                                                                                                                                                                          
              booking.markPaymentPending(
                  PaymentId.of(UUID.fromString(response.getPaymentId())),                                                                                                                                                 
                  clock.instant()
              );
              bookingRepository.save(booking);
              updateSagaStep(booking.getId(), SagaStep.AWAIT_PAYMENT);                                                                                                                                                    
   
              // Publish event v?i paymentUrl cho client                                                                                                                                                                  
              outboxPublisher.publishAll(booking.pullDomainEvents());
                                                                                                                                                                                                                          
              // Saga t?m d?ng ― ti?p t?c khi nh?n PaymentCompleted/Failed event                                                                                                                                          
                                                                                                                                                                                                                          
          } catch (StatusRuntimeException e) {                                                                                                                                                                            
              log.error("gRPC InitiatePayment error", e);
              compensate(booking, "PAYMENT_SERVICE_UNAVAILABLE", true, false);                                                                                                                                            
          }
      }                                                                                                                                                                                                                   
                  
      // ─── STEP 4: Resume on PaymentCompleted (event-driven) ───────────────────                                                                                                                                        
      @RabbitListener(queues = "booking-service.payment.completed")
      @Transactional                                                                                                                                                                                                      
      public void onPaymentCompleted(PaymentCompletedEvent event) {
          var booking = bookingRepository.findById(                                                                                                                                                                       
              BookingId.of(UUID.fromString(event.getBookingId()))
          ).orElseThrow(() -> new BookingNotFoundException(event.getBookingId()));                                                                                                                                        
                                                                                                                                                                                                                          
          // Idempotency check
          if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {                                                                                                                                                     
              log.warn("Received PaymentCompleted for booking {} in status {}. Ignoring.",
                  event.getBookingId(), booking.getStatus());                                                                                                                                                             
              return;
          }                                                                                                                                                                                                               
                                                                                                                                                                                                                          
          booking.startConfirming(clock.instant());
          bookingRepository.save(booking);                                                                                                                                                                                
                  
          // Step 4a: Confirm seats                                                                                                                                                                                       
          executeConfirmSeats(booking);
      }                                                                                                                                                                                                                   
                  
      // ─── STEP 4 + 5: Confirm Seats + Redeem Voucher ──────────────────────────                                                                                                                                        
      private void executeConfirmSeats(Booking booking) {
          var seatIds = booking.getItems().stream()                                                                                                                                                                       
              .map(item -> item.getSeatId().value())
              .toList();                                                                                                                                                                                                  
   
          try {                                                                                                                                                                                                           
              showtimeClient.confirmSeats(ConfirmSeatsRequest.newBuilder()
                  .setShowtimeId(booking.getShowtimeId().value())
                  .addAllSeatIds(seatIds)
                  .setBookingId(booking.getId().value().toString())                                                                                                                                                       
                  .build());
                                                                                                                                                                                                                          
              // Step 5: Redeem voucher n?u c?
              if (booking.getAppliedVoucher() != null) {
                  promotionClient.redeemVoucher(RedeemVoucherRequest.newBuilder()                                                                                                                                         
                      .setCode(booking.getAppliedVoucher().value())
                      .setBookingId(booking.getId().value().toString())                                                                                                                                                   
                      .build());                                                                                                                                                                                          
              }
                                                                                                                                                                                                                          
              booking.confirm(clock.instant());
              bookingRepository.save(booking);
              updateSagaStep(booking.getId(), SagaStep.COMPLETED);
              outboxPublisher.publishAll(booking.pullDomainEvents());                                                                                                                                                     
   
          } catch (StatusRuntimeException e) {                                                                                                                                                                            
              log.error("ConfirmSeats failed for booking {} ― initiating compensation with refund",
                  booking.getId(), e);                                                                                                                                                                                    
              // ?? thanh to?n r?i n?n ph?i refund                                                                                                                                                                        
              compensate(booking, "CONFIRM_SEATS_FAILED", true, false);                                                                                                                                                   
          }                                                                                                                                                                                                               
      }           
                                                                                                                                                                                                                          
      // ─── STEP Resume on PaymentFailed ────────────────────────────────────────
      @RabbitListener(queues = "booking-service.payment.failed")
      @Transactional                                                                                                                                                                                                      
      public void onPaymentFailed(PaymentFailedEvent event) {
          var booking = bookingRepository.findById(                                                                                                                                                                       
              BookingId.of(UUID.fromString(event.getBookingId()))
          ).orElseThrow();                                                                                                                                                                                                
   
          if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {                                                                                                                                                     
              return; // idempotent
          }                                                                                                                                                                                                               
          compensate(booking, "PAYMENT_FAILED: " + event.getReason(), false,
              booking.getAppliedVoucher() != null);                                                                                                                                                                       
      }                                                                                                                                                                                                                   
   
      // ─── COMPENSATION LOGIC ───────────────────────────────────────────────────                                                                                                                                       
      private void compensate(Booking booking, String reason,
                              boolean shouldRefund, boolean shouldVoidVoucher) {                                                                                                                                          
          booking.startCompensating(reason, clock.instant());
          bookingRepository.save(booking);                                                                                                                                                                                
                  
          // Compensation l? sequential (theo th? t? ng??c l?i)                                                                                                                                                           
                  
          // C1: Refund payment (n?u ?? thanh to?n)                                                                                                                                                                       
          if (shouldRefund && booking.getPaymentId() != null) {
              try {                                                                                                                                                                                                       
                  paymentClient.refundPayment(RefundPaymentRequest.newBuilder()
                      .setPaymentId(booking.getPaymentId().value().toString())                                                                                                                                            
                      .setReason("BOOKING_COMPENSATION: " + reason)
                      .build());                                                                                                                                                                                          
              } catch (StatusRuntimeException e) {
                  // Refund fail → log + alert + manual intervention                                                                                                                                                      
                  // KH?NG abort compensation ― ti?p t?c release seats                                                                                                                                                    
                  log.error("CRITICAL: Refund failed for booking {}, manual intervention needed",                                                                                                                         
                      booking.getId(), e);                                                                                                                                                                                
                  // Alert qua PagerDuty/Grafana                                                                                                                                                                          
              }   
          }                                                                                                                                                                                                               
                  
          // C2: Release seats                                                                                                                                                                                            
          var seatIds = booking.getItems().stream()
              .map(item -> item.getSeatId().value())                                                                                                                                                                      
              .toList();
          try {
              showtimeClient.releaseSeats(ReleaseSeatsRequest.newBuilder()
                  .setShowtimeId(booking.getShowtimeId().value())                                                                                                                                                         
                  .addAllSeatIds(seatIds)
                  .setBookingId(booking.getId().value().toString())                                                                                                                                                       
                  .build());                                                                                                                                                                                              
          } catch (StatusRuntimeException e) {
              // Release seats fail → Redis TTL s? t? expire sau 10 ph?t                                                                                                                                                  
              log.warn("ReleaseSeats failed for booking {} ― TTL will auto-expire", booking.getId(), e);                                                                                                                  
          }                                                                                                                                                                                                               
                                                                                                                                                                                                                          
          // C3: Void voucher (n?u ?? validate nh?ng ch?a/?ang redeem)                                                                                                                                                    
          if (shouldVoidVoucher && booking.getAppliedVoucher() != null) {
              try {                                                                                                                                                                                                       
                  promotionClient.voidVoucher(VoidVoucherRequest.newBuilder()
                      .setCode(booking.getAppliedVoucher().value())                                                                                                                                                       
                      .setBookingId(booking.getId().value().toString())
                      .build());                                                                                                                                                                                          
              } catch (StatusRuntimeException e) {
                  log.warn("VoidVoucher failed for booking {}", booking.getId(), e);
              }                                                                                                                                                                                                           
          }
                                                                                                                                                                                                                          
          booking.cancel(reason, clock.instant());
          bookingRepository.save(booking);
          updateSagaStep(booking.getId(), SagaStep.COMPENSATED);
          outboxPublisher.publishAll(booking.pullDomainEvents());                                                                                                                                                         
      }
                                                                                                                                                                                                                          
      private void updateSagaStep(BookingId bookingId, SagaStep step) {                                                                                                                                                   
          sagaStateRepository.updateStep(bookingId, step, clock.instant());
      }                                                                                                                                                                                                                   
}

  ---
3.3 Transactional Outbox ― Full Implementation

Schema b?ng Outbox:

-- V1__create_outbox_table.sql (trong booking-service/src/main/resources/db/migration/)
CREATE TABLE outbox_events (                                                                                                                                                                                            
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
aggregate_type  VARCHAR(100)    NOT NULL,  -- 'Booking', 'Payment', etc.                                                                                                                                            
aggregate_id    VARCHAR(255)    NOT NULL,  -- BookingId value
event_type      VARCHAR(200)    NOT NULL,  -- 'booking.events.confirmed.v1'                                                                                                                                         
payload         JSONB           NOT NULL,  -- event body                                                                                                                                                            
metadata        JSONB           NOT NULL DEFAULT '{}',                                                                                                                                                              
status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',                                                                                                                                                         
-- PENDING | PROCESSED | FAILED
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
processed_at    TIMESTAMPTZ,
retry_count     INT             NOT NULL DEFAULT 0,                                                                                                                                                                 
error_message   TEXT,

      CONSTRAINT outbox_events_pkey PRIMARY KEY (id)                                                                                                                                                                      
);

-- Index cho Debezium WAL capture (ch? PENDING records)                                                                                                                                                                 
CREATE INDEX idx_outbox_status_created
ON outbox_events (status, created_at)                                                                                                                                                                               
WHERE status = 'PENDING';

-- Partition by month n?u volume l?n (optional cho MVP)                                                                                                                                                                 
-- ALTER TABLE outbox_events PARTITION BY RANGE (created_at);

-- Saga state tracking
CREATE TABLE saga_states (                                                                                                                                                                                              
booking_id      UUID            NOT NULL,
current_step    VARCHAR(50)     NOT NULL,
started_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
completed_at    TIMESTAMPTZ,                                                                                                                                                                                        
compensation_reason TEXT,

      CONSTRAINT saga_states_pkey PRIMARY KEY (booking_id)
);

-- Idempotency table cho event consumers                                                                                                                                                                                
CREATE TABLE processed_events (
event_id        UUID            NOT NULL,                                                                                                                                                                           
consumer_group  VARCHAR(100)    NOT NULL,  -- 'booking-service.payment.consumer'
processed_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
CONSTRAINT processed_events_pkey PRIMARY KEY (event_id, consumer_group)
);

-- Auto-cleanup processed events sau 7 ng?y (pg_cron ho?c Quartz)                                                                                                                                                       
CREATE INDEX idx_processed_events_cleanup ON processed_events (processed_at);

OutboxEventPublisher ― Java code:

// infrastructure/outbox/OutboxEventPublisher.java
@Component                                                                                                                                                                                                              
@RequiredArgsConstructor
@Slf4j                                                                                                                                                                                                                  
public class OutboxEventPublisher {

      private final OutboxRepository outboxRepository;
      private final ObjectMapper objectMapper;

      /**                                                                                                                                                                                                                 
       * Ghi event v?o outbox table trong C?NG transaction v?i business operation.
       * Debezium s? ??c WAL v? push sang RabbitMQ ― ??m b?o at-least-once delivery.                                                                                                                                      
       */                                                                                                                                                                                                                 
      @Transactional(propagation = Propagation.MANDATORY)  // PH?I trong existing transaction                                                                                                                             
      public void publishAll(List<DomainEvent> events) {                                                                                                                                                                  
          events.forEach(this::publish);                                                                                                                                                                                  
      }                                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      @Transactional(propagation = Propagation.MANDATORY)
      public void publish(DomainEvent event) {
          try {                                                                                                                                                                                                           
              var outboxEvent = OutboxEvent.builder()
                  .id(UUID.randomUUID())                                                                                                                                                                                  
                  .aggregateType(event.getAggregateType())
                  .aggregateId(event.getAggregateId().toString())                                                                                                                                                         
                  .eventType(event.getEventType())        // e.g. "booking.events.confirmed.v1"
                  .payload(objectMapper.valueToTree(event))                                                                                                                                                               
                  .metadata(buildMetadata(event))
                  .status(OutboxStatus.PENDING)                                                                                                                                                                           
                  .createdAt(Instant.now())
                  .build();                                                                                                                                                                                               
                  
              outboxRepository.save(outboxEvent);
              log.debug("Outbox event written: type={}, aggregateId={}",
                  event.getEventType(), event.getAggregateId());                                                                                                                                                          
                                                                                                                                                                                                                          
          } catch (Exception e) {                                                                                                                                                                                         
              // N?u write outbox fail → throw ?? rollback to?n b? transaction                                                                                                                                            
              throw new OutboxWriteException("Failed to write outbox event: " + event.getEventType(), e);                                                                                                                 
          }                                                                                                                                                                                                               
      }                                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      private ObjectNode buildMetadata(DomainEvent event) {
          var meta = objectMapper.createObjectNode();
          meta.put("traceId", MDC.get("traceId"));        // OpenTelemetry trace correlation
          meta.put("spanId", MDC.get("spanId"));                                                                                                                                                                          
          meta.put("occurredAt", event.getOccurredAt().toString());
          meta.put("version", event.getVersion());                                                                                                                                                                        
          return meta;
      }                                                                                                                                                                                                                   
}

C?u h?nh Debezium Connector:

// debezium/connectors/booking-outbox-connector.json                                                                                                                                                                    
{                                                                                                                                                                                                                       
"name": "booking-outbox-connector",
"config": {                                                                                                                                                                                                           
"connector.class": "io.debezium.connector.postgresql.PostgresConnector",
"database.hostname": "booking-postgres",
"database.port": "5432",                                                                                                                                                                                            
"database.user": "debezium",
"database.password": "${DEBEZIUM_DB_PASSWORD}",                                                                                                                                                                     
"database.dbname": "booking_db",                                                                                                                                                                                    
"database.server.name": "booking",
"plugin.name": "pgoutput",

      "table.include.list": "public.outbox_events",                                                                                                                                                                       
      "slot.name": "booking_outbox_slot",
                                                                                                                                                                                                                          
      "transforms": "outbox",                                                                                                                                                                                             
      "transforms.outbox.type": "io.debezium.transforms.outbox.EventRouter",
      "transforms.outbox.table.field.event.id": "id",                                                                                                                                                                     
      "transforms.outbox.table.field.event.key": "aggregate_id",                                                                                                                                                          
      "transforms.outbox.table.field.event.type": "event_type",                                                                                                                                                           
      "transforms.outbox.table.field.event.payload": "payload",                                                                                                                                                           
      "transforms.outbox.route.by.field": "event_type",                                                                                                                                                                   
                                                                                                                                                                                                                          
      "transforms.outbox.route.topic.replacement":                                                                                                                                                                        
        "cinetix.${routedByValue}",                                                                                                                                                                                       
                                                                                                                                                                                                                          
      "heartbeat.interval.ms": "5000",
      "snapshot.mode": "never",                                                                                                                                                                                           
                  
      "key.converter": "org.apache.kafka.connect.storage.StringConverter",                                                                                                                                                
      "value.converter": "org.apache.kafka.connect.json.JsonConverter",
      "value.converter.schemas.enable": "false"                                                                                                                                                                           
    }                                                                                                                                                                                                                     
}

? L?u ?: Debezium d?ng Kafka Connect API nh?ng sink ra RabbitMQ qua RabbitMQ Sink Connector ho?c d?ng Kafka Connect → Kafka → RabbitMQ bridge. Th?c t? ??n gi?n h?n n?u d?ng Debezium Embedded Engine trong Spring Boot:

// infrastructure/outbox/DebeziumOutboxRelayer.java (alternative ― embedded)                                                                                                                                            
@Component                                                                                                                                                                                                              
@Slf4j
public class DebeziumOutboxRelayer {

      // Thay v? external Debezium, d?ng polling fallback approach:                                                                                                                                                       
      // Debezium CDC l? best practice, nh?ng embedded polling ??n gi?n h?n cho start
                                                                                                                                                                                                                          
      private final OutboxRepository outboxRepository;
      private final RabbitTemplate rabbitTemplate;                                                                                                                                                                        
      private final ObjectMapper objectMapper;

      /**                                                                                                                                                                                                                 
       * Polling fallback: ??c PENDING outbox records v? publish sang RabbitMQ.
       * Ch?y m?i 500ms. D?ng distributed lock ?? ch? 1 instance x? l?.                                                                                                                                                   
       * Production n?n migrate sang Debezium CDC ?? gi?m DB polling load.                                                                                                                                                
       */                                                                                                                                                                                                                 
      @Scheduled(fixedDelay = 500)                                                                                                                                                                                        
      @DistributedLock(key = "outbox-relayer:booking", ttl = 5000)                                                                                                                                                        
      @Transactional                                                                                                                                                                                                      
      public void relay() {                                                                                                                                                                                               
          var pendingEvents = outboxRepository.findPendingBatch(100);  // batch 100                                                                                                                                       
                                                                                                                                                                                                                          
          pendingEvents.forEach(event -> {                                                                                                                                                                                
              try {                                                                                                                                                                                                       
                  // Routing key = event_type, e.g. "booking.events.confirmed.v1"                                                                                                                                         
                  rabbitTemplate.convertAndSend(                                                                                                                                                                          
                      "cinetix.events",           // exchange
                      event.getEventType(),        // routing key                                                                                                                                                         
                      event.getPayload().toString(),
                      msg -> {                                                                                                                                                                                            
                          msg.getMessageProperties().setMessageId(event.getId().toString());
                          msg.getMessageProperties().setContentType("application/json");                                                                                                                                  
                          msg.getMessageProperties().setHeader("traceId",
                              event.getMetadata().get("traceId").asText());                                                                                                                                               
                          return msg;
                      }                                                                                                                                                                                                   
                  );
                                                                                                                                                                                                                          
                  outboxRepository.markProcessed(event.getId(), Instant.now());
                                                                                                                                                                                                                          
              } catch (Exception e) {
                  log.error("Failed to relay outbox event {}", event.getId(), e);
                  outboxRepository.incrementRetry(event.getId(), e.getMessage());                                                                                                                                         
                  // Sau 5 l?n retry → mark FAILED → alert
              }                                                                                                                                                                                                           
          });     
      }                                                                                                                                                                                                                   
}

Routing Convention & Exchange Setup:

graph LR                                                                                                                                                                                                                
subgraph EXCHANGES["RabbitMQ ― Topic Exchange: cinetix.events"]
EX["cinetix.events\n(topic exchange)"]                                                                                                                                                                          
end

      subgraph ROUTING["Routing Keys (event_type)"]                                                                                                                                                                       
          R1["booking.events.initiated.v1"]
          R2["booking.events.confirmed.v1"]                                                                                                                                                                               
          R3["booking.events.cancelled.v1"]
          R4["booking.events.expired.v1"]                                                                                                                                                                                 
          R5["payment.events.completed.v1"]
          R6["payment.events.failed.v1"]                                                                                                                                                                                  
          R7["showtime.events.seats_held.v1"]
          R8["showtime.events.seats_released.v1"]                                                                                                                                                                         
          R9["ticket.events.issued.v1"]
          R10["notification.events.requested.v1"]                                                                                                                                                                         
      end                                                                                                                                                                                                                 
   
      subgraph QUEUES["Queues (per consumer)"]                                                                                                                                                                            
          Q1["booking-svc.payment.events\nbinding: payment.events.#"]
          Q2["ticket-svc.booking.events\nbinding: booking.events.confirmed.v1"]                                                                                                                                           
          Q3["notif-svc.booking.events\nbinding: booking.events.#"]                                                                                                                                                       
          Q4["notif-svc.ticket.events\nbinding: ticket.events.issued.v1"]                                                                                                                                                 
          Q5["notif-svc.payment.events\nbinding: payment.events.#"]                                                                                                                                                       
          Q6["showtime-svc.ws.events\nbinding: showtime.events.#"]                                                                                                                                                        
      end                                                                                                                                                                                                                 
                                                                                                                                                                                                                          
      subgraph DLQ["Dead Letter Queues"]                                                                                                                                                                                  
          D1["booking-svc.payment.events.dlq"]
          D2["ticket-svc.booking.events.dlq"]                                                                                                                                                                             
          D3["notif-svc.dlq"]
      end                                                                                                                                                                                                                 
   
      EX --> Q1 & Q2 & Q3 & Q4 & Q5 & Q6                                                                                                                                                                                  
      Q1 -.->|"x-dead-letter after 3 retries"| D1
      Q2 -.->|"x-dead-letter after 3 retries"| D2                                                                                                                                                                         
      Q3 & Q4 & Q5 -.->|"x-dead-letter after 3 retries"| D3                                                                                                                                                               

Idempotency Consumer Pattern:

// application/consumer/PaymentEventConsumer.java                                                                                                                                                                       
@Component      
@RequiredArgsConstructor
@Slf4j                                                                                                                                                                                                                  
public class PaymentEventConsumer {

      private final BookingSagaOrchestrator sagaOrchestrator;
      private final ProcessedEventRepository processedEventRepository;
                                                                                                                                                                                                                          
      @RabbitListener(
          queues = "booking-svc.payment.events",                                                                                                                                                                          
          containerFactory = "rabbitListenerContainerFactory"
      )
      public void handlePaymentEvent(
              @Payload String rawPayload,                                                                                                                                                                                 
              @Header(AmqpHeaders.MESSAGE_ID) String messageId,
              @Header("eventType") String eventType,                                                                                                                                                                      
              Channel channel,
              @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {                                                                                                                                    
                                                                                                                                                                                                                          
          // ── IDEMPOTENCY CHECK ──────────────────────────────────────────────────                                                                                                                                      
          var eventId = UUID.fromString(messageId);                                                                                                                                                                       
          var consumerGroup = "booking-svc.payment.consumer";                                                                                                                                                             
                  
          if (processedEventRepository.existsByEventIdAndConsumerGroup(eventId, consumerGroup)) {                                                                                                                         
              log.info("Duplicate event {} received, skipping (idempotent)", eventId);
              channel.basicAck(deliveryTag, false);                                                                                                                                                                       
              return;
          }                                                                                                                                                                                                               
                  
          try {                                                                                                                                                                                                           
              // ── PROCESS EVENT ────────────────────────────────────────────────
              switch (eventType) {                                                                                                                                                                                        
                  case "payment.events.completed.v1" -> {
                      var event = objectMapper.readValue(rawPayload, PaymentCompletedEvent.class);                                                                                                                        
                      sagaOrchestrator.onPaymentCompleted(event);
                  }                                                                                                                                                                                                       
                  case "payment.events.failed.v1" -> {                                                                                                                                                                    
                      var event = objectMapper.readValue(rawPayload, PaymentFailedEvent.class);
                      sagaOrchestrator.onPaymentFailed(event);                                                                                                                                                            
                  }
                  default -> log.warn("Unknown event type: {}", eventType);
              }                                                                                                                                                                                                           
   
              // ── MARK PROCESSED (same transaction via @Transactional in saga) ─                                                                                                                                        
              processedEventRepository.save(
                  ProcessedEvent.of(eventId, consumerGroup, Instant.now())                                                                                                                                                
              );                                                                                                                                                                                                          
   
              channel.basicAck(deliveryTag, false);                                                                                                                                                                       
                  
          } catch (NonRetryableException e) {
              // Business exception → NACK kh?ng requeue → v?o DLQ
              log.error("Non-retryable error processing event {}", eventId, e);                                                                                                                                           
              channel.basicNack(deliveryTag, false, false);
                                                                                                                                                                                                                          
          } catch (Exception e) {
              // Transient error → NACK + requeue (s? retry)                                                                                                                                                              
              log.warn("Transient error processing event {}, will retry", eventId, e);
              channel.basicNack(deliveryTag, false, true);                                                                                                                                                                
          }
      }                                                                                                                                                                                                                   
}
   
---                                                                                                                                                                                                                     
3.4 Timeout & Retry Strategy

Ai ch?u tr?ch nhi?m timeout?

graph TD
subgraph TIMEOUT_OWNERS["Timeout Ownership"]                                                                                                                                                                        
T1["Seat Hold TTL\n─────────────\nOwner: Redis\nMechanism: TTL t? expire\nValue: 600 seconds\nAction: Key t? x?a\n→ Showtime service detect\n   qua ZADD score monitoring"]

          T2["Payment Window TTL\n─────────────\nOwner: Quartz Scheduler\n(booking-service)\nMechanism: Job ch?y m?i 30s\nQuery: status=PAYMENT_PENDING\n        AND expires_at < NOW()\nAction: trigger compensation"]   
                                                                                                                                                                                                                          
          T3["gRPC Call Timeout\n─────────────\nOwner: gRPC client config\nMechanism: deadline per call\nValue:\n  HoldSeats: 3s\n  InitiatePayment: 5s\n  ConfirmSeats: 3s\nAction: StatusRuntimeException\n→            
compensation"]

          T4["RabbitMQ Message TTL\n─────────────\nOwner: Queue policy\nMechanism: x-message-ttl\nValue: 30 minutes\n(cho payment.events queue)\nAction: → DLQ khi expire\nNote: kh?ng d?ng cho\nseat-hold timeout"]      
      end

Quartz Scheduler ― Booking Expiry Job:

// infrastructure/scheduler/BookingExpiryJob.java                                                                                                                                                                       
@Component      
@RequiredArgsConstructor
@Slf4j                                                                                                                                                                                                                  
public class BookingExpiryJob implements Job {

      private final BookingRepository bookingRepository;
      private final BookingSagaOrchestrator sagaOrchestrator;
      private final Clock clock;                                                                                                                                                                                          
   
      @Override                                                                                                                                                                                                           
      public void execute(JobExecutionContext context) {
          var expiredBookings = bookingRepository.findExpiredPendingBookings(
              clock.instant(),                                                                                                                                                                                            
              BookingStatus.PAYMENT_PENDING,
              PageRequest.of(0, 50)   // batch 50 per run                                                                                                                                                                 
          );                                                                                                                                                                                                              
   
          log.info("Found {} expired bookings to process", expiredBookings.size());                                                                                                                                       
                  
          expiredBookings.forEach(booking -> {                                                                                                                                                                            
              try {
                  sagaOrchestrator.handleBookingExpiry(booking.getId());
              } catch (Exception e) {                                                                                                                                                                                     
                  log.error("Failed to expire booking {}", booking.getId(), e);
                  // Kh?ng throw ― ti?p t?c x? l? bookings kh?c                                                                                                                                                           
              }                                                                                                                                                                                                           
          });
      }                                                                                                                                                                                                                   
}

// Quartz config                                                                                                                                                                                                        
@Configuration
public class QuartzConfig {

      @Bean
      public JobDetail bookingExpiryJobDetail() {
          return JobBuilder.newJob(BookingExpiryJob.class)
              .withIdentity("bookingExpiryJob")                                                                                                                                                                           
              .storeDurably()
              .build();                                                                                                                                                                                                   
      }           

      @Bean
      public Trigger bookingExpiryTrigger() {
          return TriggerBuilder.newTrigger()                                                                                                                                                                              
              .forJob(bookingExpiryJobDetail())
              .withIdentity("bookingExpiryTrigger")                                                                                                                                                                       
              .withSchedule(
                  SimpleScheduleBuilder.simpleSchedule()                                                                                                                                                                  
                      .withIntervalInSeconds(30)   // m?i 30 gi?y
                      .repeatForever()                                                                                                                                                                                    
              )   
              .build();                                                                                                                                                                                                   
      }           
}

Retry Strategy Matrix:

┌─────────────────────────────┬──────────────┬────────────────┬─────────────────────────┐
│ Operation                   │ Retry        │ Backoff        │ Circuit Breaker         │                                                                                                                               
├─────────────────────────────┼──────────────┼────────────────┼─────────────────────────┤                                                                                                                               
│ gRPC HoldSeats              │ 2 attempts   │ Exponential    │ OPEN after 5 fails/10s  │                                                                                                                               
│                             │              │ 100ms, 200ms   │ HALF-OPEN after 30s     │                                                                                                                               
├─────────────────────────────┼──────────────┼────────────────┼─────────────────────────┤                                                                                                                               
│ gRPC InitiatePayment        │ 1 attempt    │ No retry       │ OPEN after 3 fails/10s  │                                                                                                                               
│ (PSP side effect ― risky    │ (idempotency │ (PSP idempoten │                         │                                                                                                                               
│  to retry without key)      │  key in req) │  cy key helps) │                         │                                                                                                                               
├─────────────────────────────┼──────────────┼────────────────┼─────────────────────────┤                                                                                                                               
│ gRPC ConfirmSeats           │ 3 attempts   │ 200ms, 400ms,  │ OPEN after 5 fails      │                                                                                                                               
│                             │              │ 800ms          │                         │                                                                                                                               
├─────────────────────────────┼──────────────┼────────────────┼─────────────────────────┤                                                                                                                               
│ gRPC RefundPayment          │ 5 attempts   │ Exponential    │ OPEN after 3 fails      │                                                                                                                               
│ (money involved!)           │              │ 1s max         │ Alert on OPEN           │
├─────────────────────────────┼──────────────┼────────────────┼─────────────────────────┤                                                                                                                               
│ Outbox Relay (RabbitMQ pub) │ 5 attempts   │ 500ms, 1s, 2s, │ N/A (schedule-based)    │
│                             │              │ 4s, 8s         │ Alert after 5 fails     │                                                                                                                               
├─────────────────────────────┼──────────────┼────────────────┼─────────────────────────┤                                                                                                                               
│ RabbitMQ consumer           │ 3 attempts   │ NackAndRequeue │ → DLQ after 3 nacks     │                                                                                                                               
│ (booking.payment.completed) │              │ (RabbitMQ)     │ Alert on DLQ message    │                                                                                                                               
└─────────────────────────────┴──────────────┴────────────────┴─────────────────────────┘

Resilience4j Config:

# booking-service/src/main/resources/application.yml
resilience4j:                                                                                                                                                                                                           
circuitbreaker:
instances:                                                                                                                                                                                                          
showtime-service:
slidingWindowSize: 10
failureRateThreshold: 50
waitDurationInOpenState: 30s
permittedNumberOfCallsInHalfOpenState: 3                                                                                                                                                                        
recordExceptions:
- io.grpc.StatusRuntimeException                                                                                                                                                                              
payment-service:                                                                                                                                                                                                  
slidingWindowSize: 5
failureRateThreshold: 40    # Strict h?n v? li?n quan ti?n                                                                                                                                                      
waitDurationInOpenState: 60s

    retry:                                                                                                                                                                                                                
      instances:                                                                                                                                                                                                          
        showtime-hold:
          maxAttempts: 3
          waitDuration: 100ms
          enableExponentialBackoff: true
          exponentialBackoffMultiplier: 2                                                                                                                                                                                 
          retryExceptions:
            - io.grpc.StatusRuntimeException                                                                                                                                                                              
          ignoreExceptions:
            - com.cinetix.booking.domain.exception.SeatsUnavailableException                                                                                                                                              
                                                                                                                                                                                                                          
        payment-initiate:                                                                                                                                                                                                 
          maxAttempts: 2                                                                                                                                                                                                  
          waitDuration: 500ms
          # Idempotency key trong request → safe to retry
                                                                                                                                                                                                                          
    bulkhead:
      instances:                                                                                                                                                                                                          
        showtime-service:
          maxConcurrentCalls: 20   # Max 20 concurrent gRPC calls
          maxWaitDuration: 100ms                                                                                                                                                                                          

?? Nh?ng ?i?m quan tr?ng b?n c?n l?u ?:

1. Saga State Recovery sau crash:                                                                                                                                                                                       
   N?u booking-service pod crash gi?a CONFIRMING state:
- Quartz job detect booking ? CONFIRMING qu? 2 ph?t → trigger re-check
- Query payment-service: payment ?? COMPLETED ch?a?
    - N?u COMPLETED → resume t? ConfirmSeats
    - N?u v?n PROCESSING → wait th?m
    - N?u FAILED → compensate

2. Distributed Lock cho Seat Hold:                                                                                                                                                                                      
   // D?ng Redisson ?? tr?nh 2 booking c?ng hold 1 gh?                                                                                                                                                                     
   // (gRPC HoldSeats ph?i atomic ― check + set trong 1 Lua script)                                                                                                                                                        
   String script = """                                                                                                                                                                                                     
   local current = redis.call('HGET', KEYS[1], 'status')                                                                                                                                                               
   if current == false or current == 'AVAILABLE' then                                                                                                                                                                  
   redis.call('HSET', KEYS[1], 'status', 'HELD',                                                                                                                                                                   
   'bookingId', ARGV[1], 'expiresAt', ARGV[2])
   return 1                                                                                                                                                                                                        
   end                                                                                                                                                                                                                 
   return 0                                                                                                                                                                                                            
   """;                                                                                                                                                                                                                
   // Lua script ??m b?o atomicity trong Redis single-threaded model

3. Two-Phase Commit kh?ng d?ng:                                                                                                                                                                                         
   Ch?ng ta c? t?nh KH?NG d?ng 2PC.
   Thay v?o ??: Outbox pattern + Saga compensation                                                                                                                                                                         
   Trade-off: eventual consistency thay v? strong consistency                                                                                                                                                              
   Benefit: services ho?n to?n independent, kh?ng b? block nhau

  ---                                                                                                                                                                                                                     
Phase 3 ho?n th?nh ― ??y l? ph?n ph?c t?p nh?t c?a to?n b? h? th?ng. G? "phase 4" ?? t?i thi?t k? Database Schema ??y ?? cho t?t c? services v?i DDL, index strategy, v? partitioning.

? Churned for 2m 38s

? phase 4

● PHASE 4 ― DATABASE DESIGN

Quy ??c chung tr??c khi b?t ??u

-- ── CONVENTIONS ──────────────────────────────────────────────────────────────                                                                                                                                        
-- 1. Primary Key: UUID v4 (gen_random_uuid()) ― distributed-safe, no sequence leak                                                                                                                                     
-- 2. Timestamps: TIMESTAMPTZ (UTC) ― kh?ng bao gi? d?ng TIMESTAMP                                                                                                                                                      
-- 3. Soft delete: deleted_at TIMESTAMPTZ (NULL = active)                                                                                                                                                               
-- 4. Optimistic locking: version BIGINT NOT NULL DEFAULT 0                                                                                                                                                             
-- 5. Audit: created_at, updated_at tr?n m?i b?ng quan tr?ng                                                                                                                                                            
-- 6. T?n b?ng: snake_case, s? nhi?u                                                                                                                                                                                    
-- 7. FK kh?ng d?ng CASCADE DELETE ― explicit business logic                                                                                                                                                            
-- 8. JSONB cho flexible metadata, kh?ng abuse                                                                                                                                                                          
-- ─────────────────────────────────────────────────────────────────────────────
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
4.1 Schema Chi Ti?t T?ng Service

? identity-service ― identity_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_identity_schema.sql                                                                                                                                                                                       
-- Aggregate Root: UserProfile                                                                                                                                                                                          
-- ??????????????????????????????????????????????????????????????????

-- Extension    
CREATE EXTENSION IF NOT EXISTS "pgcrypto";                                                                                                                                                                              
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- cho full-text search tr?n username/email

-- ── user_profiles ─────────────────────────────────────────────────────────────                                                                                                                                       
-- L?u th?ng tin m? r?ng c?a user (Keycloak gi? auth, ta gi? domain data)                                                                                                                                               
-- Aggregate Root: UserProfile                                                                                                                                                                                          
CREATE TABLE user_profiles (                                                                                                                                                                                            
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
keycloak_id     UUID            NOT NULL,           -- FK → Keycloak user ID (external ref)                                                                                                                         
email           VARCHAR(255)    NOT NULL,                                                                                                                                                                           
full_name       VARCHAR(200)    NOT NULL,                                                                                                                                                                           
phone           VARCHAR(20),                                                                                                                                                                                        
date_of_birth   DATE,                                                                                                                                                                                               
avatar_url      TEXT,
gender          VARCHAR(10),                        -- MALE | FEMALE | OTHER                                                                                                                                        
status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
-- ACTIVE | SUSPENDED | DELETED                                                                                                                                 
preferred_language  VARCHAR(10) NOT NULL DEFAULT 'vi',
marketing_consent   BOOLEAN     NOT NULL DEFAULT FALSE,                                                                                                                                                             
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
deleted_at      TIMESTAMPTZ,                        -- soft delete                                                                                                                                                  
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT user_profiles_pkey PRIMARY KEY (id),
      CONSTRAINT user_profiles_keycloak_id_key UNIQUE (keycloak_id),                                                                                                                                                      
      CONSTRAINT user_profiles_email_key UNIQUE (email),                                                                                                                                                                  
      CONSTRAINT user_profiles_status_check
          CHECK (status IN ('ACTIVE', 'SUSPENDED', 'DELETED')),                                                                                                                                                           
      CONSTRAINT user_profiles_gender_check
          CHECK (gender IN ('MALE', 'FEMALE', 'OTHER') OR gender IS NULL)                                                                                                                                                 
);

-- ── roles ────────────────────────────────────────────────────────────────────                                                                                                                                        
-- Entity (b?n trong aggregate, nh?ng c? b?ng ri?ng v? nhi?u-nhi?u)
CREATE TABLE roles (                                                                                                                                                                                                    
id          UUID        NOT NULL DEFAULT gen_random_uuid(),
name        VARCHAR(50) NOT NULL,                   -- CUSTOMER | STAFF | ADMIN                                                                                                                                     
description TEXT,
created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

      CONSTRAINT roles_pkey PRIMARY KEY (id),
      CONSTRAINT roles_name_key UNIQUE (name)                                                                                                                                                                             
);

-- Seed data                                                                                                                                                                                                            
INSERT INTO roles (name, description) VALUES
('CUSTOMER', 'Regular customer who books tickets'),                                                                                                                                                                 
('STAFF',    'Cinema staff who validates tickets and handles support'),
('ADMIN',    'Administrator with full system access');

-- ── user_roles ────────────────────────────────────────────────────────────────                                                                                                                                       
CREATE TABLE user_roles (                                                                                                                                                                                               
user_id     UUID        NOT NULL REFERENCES user_profiles(id),
role_id     UUID        NOT NULL REFERENCES roles(id),                                                                                                                                                              
assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
assigned_by UUID        REFERENCES user_profiles(id),

      CONSTRAINT user_roles_pkey PRIMARY KEY (user_id, role_id)                                                                                                                                                           
);

-- ── user_sessions ─────────────────────────────────────────────────────────────                                                                                                                                       
-- Track active sessions (complement Keycloak ― d?ng cho force-logout feature)
CREATE TABLE user_sessions (                                                                                                                                                                                            
id              UUID        NOT NULL DEFAULT gen_random_uuid(),
user_id         UUID        NOT NULL REFERENCES user_profiles(id),                                                                                                                                                  
keycloak_session_id VARCHAR(100) NOT NULL,                                                                                                                                                                          
device_info     JSONB,                              -- {os, browser, ip}
last_active_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),                                                                                                                                                                 
created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
expired_at      TIMESTAMPTZ,

      CONSTRAINT user_sessions_pkey PRIMARY KEY (id)
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────
CREATE INDEX idx_user_profiles_keycloak_id ON user_profiles (keycloak_id);
CREATE INDEX idx_user_profiles_email       ON user_profiles (email)                                                                                                                                                     
WHERE deleted_at IS NULL;
CREATE INDEX idx_user_profiles_phone       ON user_profiles (phone)                                                                                                                                                     
WHERE phone IS NOT NULL AND deleted_at IS NULL;
-- GIN index cho full-text search                                                                                                                                                                                       
CREATE INDEX idx_user_profiles_fullname_trgm                                                                                                                                                                            
ON user_profiles USING GIN (full_name gin_trgm_ops);
CREATE INDEX idx_user_roles_user_id     ON user_roles (user_id);                                                                                                                                                        
CREATE INDEX idx_user_sessions_user_id  ON user_sessions (user_id)                                                                                                                                                      
WHERE expired_at IS NULL;
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
? movie-service ― movie_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_movie_schema.sql                                                                                                                                                                                          
-- Aggregate Root: Movie
-- ??????????????????????????????????????????????????????????????????

CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "unaccent";  -- t?m ki?m kh?ng d?u ti?ng Vi?t

-- ── movies ───────────────────────────────────────────────────────────────────                                                                                                                                        
-- Aggregate Root                                                                                                                                                                                                       
CREATE TABLE movies (                                                                                                                                                                                                   
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
title           VARCHAR(500)    NOT NULL,
original_title  VARCHAR(500),                       -- t?n g?c (n?u phim n??c ngo?i)                                                                                                                                
slug            VARCHAR(600)    NOT NULL,           -- URL-friendly                                                                                                                                                 
description     TEXT,                                                                                                                                                                                               
duration_minutes INT            NOT NULL,                                                                                                                                                                           
release_date    DATE,                                                                                                                                                                                               
end_date        DATE,                               -- ng?y k?t th?c chi?u
language        VARCHAR(50)     NOT NULL,           -- 'vi', 'en', etc.                                                                                                                                             
subtitles       VARCHAR(50)[],                      -- ['vi', 'en']                                                                                                                                                 
rating          VARCHAR(10)     NOT NULL,           -- P | K | T13 | T16 | T18                                                                                                                                      
status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',                                                                                                                                                           
-- DRAFT|COMING_SOON|NOW_SHOWING|ENDED                                                                                                                          
poster_url      TEXT,                                                                                                                                                                                               
backdrop_url    TEXT,
trailer_url     TEXT,                                                                                                                                                                                               
imdb_rating     DECIMAL(3,1),
director        VARCHAR(200),                                                                                                                                                                                       
country         VARCHAR(100),
metadata        JSONB           NOT NULL DEFAULT '{}',  -- flexible extras                                                                                                                                          
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
deleted_at      TIMESTAMPTZ,                                                                                                                                                                                        
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT movies_pkey PRIMARY KEY (id),
      CONSTRAINT movies_slug_key UNIQUE (slug),                                                                                                                                                                           
      CONSTRAINT movies_duration_check CHECK (duration_minutes > 0),
      CONSTRAINT movies_rating_check                                                                                                                                                                                      
          CHECK (rating IN ('P', 'K', 'T13', 'T16', 'T18')),
      CONSTRAINT movies_status_check                                                                                                                                                                                      
          CHECK (status IN ('DRAFT', 'COMING_SOON', 'NOW_SHOWING', 'ENDED'))
);

-- ── genres ───────────────────────────────────────────────────────────────────                                                                                                                                        
-- Value Object trong Movie aggregate, nh?ng t?ch b?ng v? reuse
CREATE TABLE genres (                                                                                                                                                                                                   
id      UUID        NOT NULL DEFAULT gen_random_uuid(),
name    VARCHAR(100) NOT NULL,                                                                                                                                                                                      
slug    VARCHAR(120) NOT NULL,

      CONSTRAINT genres_pkey PRIMARY KEY (id),
      CONSTRAINT genres_slug_key UNIQUE (slug)                                                                                                                                                                            
);

INSERT INTO genres (name, slug) VALUES                                                                                                                                                                                  
('H?nh ??ng', 'hanh-dong'), ('H?i H??c', 'hai-huoc'),
('Kinh D?', 'kinh-di'), ('T?nh C?m', 'tinh-cam'),                                                                                                                                                                   
('Ho?t H?nh', 'hoat-hinh'), ('Khoa H?c Vi?n T??ng', 'khoa-hoc-vien-tuong'),                                                                                                                                         
('T?m L?', 'tam-ly'), ('Phi?u L?u', 'phieu-luu');

-- ── movie_genres ──────────────────────────────────────────────────────────────                                                                                                                                       
CREATE TABLE movie_genres (                                                                                                                                                                                             
movie_id    UUID NOT NULL REFERENCES movies(id),
genre_id    UUID NOT NULL REFERENCES genres(id),                                                                                                                                                                    
CONSTRAINT movie_genres_pkey PRIMARY KEY (movie_id, genre_id)
);

-- ── casts ────────────────────────────────────────────────────────────────────                                                                                                                                        
-- Entity b?n trong Movie aggregate
CREATE TABLE casts (                                                                                                                                                                                                    
id          UUID            NOT NULL DEFAULT gen_random_uuid(),
movie_id    UUID            NOT NULL REFERENCES movies(id),                                                                                                                                                         
person_name VARCHAR(200)    NOT NULL,
role_name   VARCHAR(200),                           -- t?n nh?n v?t                                                                                                                                                 
role_type   VARCHAR(20)     NOT NULL,               -- ACTOR | DIRECTOR | PRODUCER                                                                                                                                  
display_order INT           NOT NULL DEFAULT 0,                                                                                                                                                                     
avatar_url  TEXT,

      CONSTRAINT casts_pkey PRIMARY KEY (id),
      CONSTRAINT casts_role_type_check                                                                                                                                                                                    
          CHECK (role_type IN ('ACTOR', 'DIRECTOR', 'PRODUCER', 'WRITER'))
);

-- ── movie_formats ─────────────────────────────────────────────────────────────                                                                                                                                       
-- Value Object: phim c? th? chi?u 2D, 3D, IMAX
CREATE TABLE movie_formats (                                                                                                                                                                                            
id          UUID        NOT NULL DEFAULT gen_random_uuid(),
movie_id    UUID        NOT NULL REFERENCES movies(id),                                                                                                                                                             
format      VARCHAR(20) NOT NULL,                   -- 2D | 3D | IMAX | 4DX
audio_type  VARCHAR(20) NOT NULL DEFAULT 'DUBBED',  -- DUBBED | SUBTITLED | ORIGINAL

      CONSTRAINT movie_formats_pkey PRIMARY KEY (id),                                                                                                                                                                     
      CONSTRAINT movie_formats_unique UNIQUE (movie_id, format, audio_type),                                                                                                                                              
      CONSTRAINT movie_formats_format_check                                                                                                                                                                               
          CHECK (format IN ('2D', '3D', 'IMAX', '4DX', 'ScreenX'))
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
CREATE INDEX idx_movies_status ON movies (status) WHERE deleted_at IS NULL;
CREATE INDEX idx_movies_release_date ON movies (release_date DESC) WHERE deleted_at IS NULL;                                                                                                                            
CREATE INDEX idx_movies_slug ON movies (slug);                                                                                                                                                                          
-- Full-text search cho ti?u ?? phim (c? th? k?t h?p unaccent)
CREATE INDEX idx_movies_title_trgm                                                                                                                                                                                      
ON movies USING GIN (title gin_trgm_ops)
WHERE deleted_at IS NULL;                                                                                                                                                                                           
CREATE INDEX idx_casts_movie_id ON casts (movie_id);
CREATE INDEX idx_movie_genres_genre_id ON movie_genres (genre_id);
                  
---                                                                                                                                                                                                                     
? cinema-service ― cinema_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_cinema_schema.sql
-- Aggregate Root: Cinema, Screen
-- ??????????????????????????????????????????????????????????????????

-- ── cinemas ───────────────────────────────────────────────────────────────────                                                                                                                                       
-- Aggregate Root
CREATE TABLE cinemas (                                                                                                                                                                                                  
id          UUID            NOT NULL DEFAULT gen_random_uuid(),
name        VARCHAR(300)    NOT NULL,
slug        VARCHAR(350)    NOT NULL,
chain       VARCHAR(100),                           -- CGV, Lotte, BHD...                                                                                                                                           
city        VARCHAR(100)    NOT NULL,                                                                                                                                                                               
district    VARCHAR(100),                                                                                                                                                                                           
address     TEXT            NOT NULL,                                                                                                                                                                               
latitude    DECIMAL(9,6),                                                                                                                                                                                           
longitude   DECIMAL(9,6),
phone       VARCHAR(20),                                                                                                                                                                                            
email       VARCHAR(255),
status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
amenities   VARCHAR(50)[],                          -- ['parking', 'food', 'atm']                                                                                                                                   
images      TEXT[],
created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                                 
updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
deleted_at  TIMESTAMPTZ,                                                                                                                                                                                            
version     BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT cinemas_pkey PRIMARY KEY (id),
      CONSTRAINT cinemas_slug_key UNIQUE (slug),
      CONSTRAINT cinemas_status_check CHECK (status IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE'))
);

-- ── screens ───────────────────────────────────────────────────────────────────                                                                                                                                       
-- Aggregate Root (owns seat templates)
CREATE TABLE screens (                                                                                                                                                                                                  
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
cinema_id       UUID            NOT NULL REFERENCES cinemas(id),                                                                                                                                                    
name            VARCHAR(100)    NOT NULL,           -- "Ph?ng 1", "Hall A"                                                                                                                                          
screen_type     VARCHAR(20)     NOT NULL,           -- STANDARD | IMAX | 4DX | VIP
total_rows      INT             NOT NULL,                                                                                                                                                                           
total_cols      INT             NOT NULL,
total_seats     INT             NOT NULL,           -- denormalized cho query nhanh                                                                                                                                 
status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',                                                                                                                                                          
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

      CONSTRAINT screens_pkey PRIMARY KEY (id),                                                                                                                                                                           
      CONSTRAINT screens_cinema_name_unique UNIQUE (cinema_id, name),
      CONSTRAINT screens_type_check                                                                                                                                                                                       
          CHECK (screen_type IN ('STANDARD', 'IMAX', '4DX', 'SCREENX', 'VIP'))
);

-- ── seat_templates ────────────────────────────────────────────────────────────                                                                                                                                       
-- Entity b?n trong Screen aggregate
-- ??y l? LAYOUT T?NH ― ??nh ngh?a v? tr? gh? v?t l? trong ph?ng
-- KH?NG ph?i seat availability (c?i ?? ? showtime-service)                                                                                                                                                             
CREATE TABLE seat_templates (                                                                                                                                                                                           
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
screen_id       UUID            NOT NULL REFERENCES screens(id),                                                                                                                                                    
row_label       VARCHAR(5)      NOT NULL,           -- A, B, C... ho?c 1, 2, 3...
seat_number     INT             NOT NULL,                                                                                                                                                                           
seat_code       VARCHAR(20)     NOT NULL,           -- A1, A2, B1... (denormalized)
category        VARCHAR(20)     NOT NULL DEFAULT 'STANDARD',                                                                                                                                                        
-- STANDARD | VIP | COUPLE | WHEELCHAIR                                                                                                                         
position_x      INT             NOT NULL,           -- grid column (0-based)                                                                                                                                        
position_y      INT             NOT NULL,           -- grid row (0-based)                                                                                                                                           
is_available    BOOLEAN         NOT NULL DEFAULT TRUE,  -- FALSE = gh? b? h?ng/blocked

      CONSTRAINT seat_templates_pkey PRIMARY KEY (id),                                                                                                                                                                    
      CONSTRAINT seat_templates_unique UNIQUE (screen_id, row_label, seat_number),                                                                                                                                        
      CONSTRAINT seat_templates_category_check                                                                                                                                                                            
          CHECK (category IN ('STANDARD', 'VIP', 'COUPLE', 'WHEELCHAIR'))
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
CREATE INDEX idx_cinemas_city     ON cinemas (city) WHERE deleted_at IS NULL;
CREATE INDEX idx_cinemas_location ON cinemas USING GIST (                                                                                                                                                               
ll_to_earth(latitude::float8, longitude::float8)
) WHERE latitude IS NOT NULL;  -- geo search "r?p g?n t?i"                                                                                                                                                              
CREATE INDEX idx_screens_cinema_id        ON screens (cinema_id);                                                                                                                                                       
CREATE INDEX idx_seat_templates_screen_id ON seat_templates (screen_id);                                                                                                                                                
CREATE INDEX idx_seat_templates_category                                                                                                                                                                                
ON seat_templates (screen_id, category)                                                                                                                                                                             
WHERE is_available = TRUE;
   
---                                                                                                                                                                                                                     
? showtime-service ― showtime_db + Redis

-- ??????????????????????????????????????????????????????????????????
-- V1__create_showtime_schema.sql                                                                                                                                                                                       
-- Aggregate Root: Showtime
-- Redis: SeatAvailability (hot data)
-- ??????????????????????????????????????????????????????????????????

-- ── showtimes ─────────────────────────────────────────────────────────────────                                                                                                                                       
-- Aggregate Root
CREATE TABLE showtimes (                                                                                                                                                                                                
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
movie_id        UUID            NOT NULL,           -- ref → movie-service (NO FK ― cross-service)
screen_id       UUID            NOT NULL,           -- ref → cinema-service (NO FK)                                                                                                                                 
cinema_id       UUID            NOT NULL,           -- denormalized cho query nhanh
start_time      TIMESTAMPTZ     NOT NULL,                                                                                                                                                                           
end_time        TIMESTAMPTZ     NOT NULL,
format          VARCHAR(20)     NOT NULL,           -- 2D | 3D | IMAX                                                                                                                                               
audio_type      VARCHAR(20)     NOT NULL DEFAULT 'DUBBED',                                                                                                                                                          
base_price      BIGINT          NOT NULL,           -- VND, stored as integer (cents kh?ng d?ng ? VN)
vip_price       BIGINT          NOT NULL,                                                                                                                                                                           
couple_price    BIGINT          NOT NULL,
status          VARCHAR(20)     NOT NULL DEFAULT 'SCHEDULED',                                                                                                                                                       
-- SCHEDULED|OPEN|FULL|CANCELLED|ENDED
total_seats     INT             NOT NULL,           -- snapshot t? screen                                                                                                                                           
available_seats INT             NOT NULL,           -- denormalized counter
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT showtimes_pkey PRIMARY KEY (id),
      CONSTRAINT showtimes_time_check CHECK (end_time > start_time),                                                                                                                                                      
      CONSTRAINT showtimes_price_check CHECK (base_price > 0),                                                                                                                                                            
      CONSTRAINT showtimes_available_check
          CHECK (available_seats >= 0 AND available_seats <= total_seats),                                                                                                                                                
      CONSTRAINT showtimes_status_check                                                                                                                                                                                   
          CHECK (status IN ('SCHEDULED', 'OPEN', 'FULL', 'CANCELLED', 'ENDED'))
);

-- ── seat_snapshots ────────────────────────────────────────────────────────────                                                                                                                                       
-- Postgres snapshot c?a seat status (Redis l? source of truth khi showtime OPEN)
-- D?ng ??: restore Redis sau crash, historical reporting, audit                                                                                                                                                        
CREATE TABLE seat_snapshots (                                                                                                                                                                                           
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
showtime_id     UUID            NOT NULL REFERENCES showtimes(id),                                                                                                                                                  
seat_id         UUID            NOT NULL,           -- ref → seat_templates (NO FK)
seat_code       VARCHAR(20)     NOT NULL,           -- denormalized                                                                                                                                                 
category        VARCHAR(20)     NOT NULL,
price           BIGINT          NOT NULL,                                                                                                                                                                           
status          VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',                                                                                                                                                       
-- AVAILABLE|HELD|CONFIRMED|UNAVAILABLE
booking_id      UUID,                               -- NULL n?u AVAILABLE                                                                                                                                           
held_until      TIMESTAMPTZ,                        -- NULL n?u kh?ng HELD                                                                                                                                          
confirmed_at    TIMESTAMPTZ,                                                                                                                                                                                        
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

      CONSTRAINT seat_snapshots_pkey PRIMARY KEY (id),
      CONSTRAINT seat_snapshots_unique UNIQUE (showtime_id, seat_id),                                                                                                                                                     
      CONSTRAINT seat_snapshots_status_check
          CHECK (status IN ('AVAILABLE', 'HELD', 'CONFIRMED', 'UNAVAILABLE'))                                                                                                                                             
);

-- ── showtime_outbox ───────────────────────────────────────────────────────────                                                                                                                                       
CREATE TABLE outbox_events (
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
aggregate_type  VARCHAR(100)    NOT NULL,
aggregate_id    VARCHAR(255)    NOT NULL,                                                                                                                                                                           
event_type      VARCHAR(200)    NOT NULL,
payload         JSONB           NOT NULL,                                                                                                                                                                           
metadata        JSONB           NOT NULL DEFAULT '{}',
status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',                                                                                                                                                         
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
processed_at    TIMESTAMPTZ,                                                                                                                                                                                        
retry_count     INT             NOT NULL DEFAULT 0,
error_message   TEXT,                                                                                                                                                                                               
CONSTRAINT outbox_events_pkey PRIMARY KEY (id)
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
-- Query ch?nh: t?m su?t chi?u theo phim + ng?y + r?p
CREATE INDEX idx_showtimes_movie_date                                                                                                                                                                                   
ON showtimes (movie_id, start_time)
WHERE status IN ('SCHEDULED', 'OPEN');

CREATE INDEX idx_showtimes_cinema_date                                                                                                                                                                                  
ON showtimes (cinema_id, start_time)
WHERE status IN ('SCHEDULED', 'OPEN');

CREATE INDEX idx_showtimes_screen_time                                                                                                                                                                                  
ON showtimes (screen_id, start_time, end_time);

-- Partial index: ch? c?c gh? HELD (cho expire job)                                                                                                                                                                     
CREATE INDEX idx_seat_snapshots_held                                                                                                                                                                                    
ON seat_snapshots (showtime_id, held_until)                                                                                                                                                                         
WHERE status = 'HELD';

CREATE INDEX idx_seat_snapshots_showtime
ON seat_snapshots (showtime_id, status);

CREATE INDEX idx_outbox_pending                                                                                                                                                                                         
ON outbox_events (status, created_at)
WHERE status = 'PENDING';

── Redis Data Structures cho Seat Availability ─────────────────────────────────

KEY PATTERNS:
seat:{showtimeId}:{seatId}                                                                                                                                                                                            
Type: HASH                                                                                                                                                                                                          
Fields: status, bookingId, expiresAt, category, price
TTL: none (permanent while showtime active)

    seat_holds:{showtimeId}                                                                                                                                                                                               
      Type: SORTED SET                                                                                                                                                                                                    
      Score: expiresAt (Unix timestamp)
      Member: "{bookingId}:{seatId}"
      D?ng: ZRANGEBYSCORE seat_holds:{id} 0 {now} → l?y expired holds                                                                                                                                                     
                                                                                                                                                                                                                          
    showtime:available_count:{showtimeId}                                                                                                                                                                                 
      Type: STRING (counter)                                                                                                                                                                                              
      D?ng: DECR khi hold, INCR khi release/expire                                                                                                                                                                        
      Cho ph?p query nhanh "c?n bao nhi?u gh?" m? kh?ng scan t?t c? seats                                                                                                                                                 
                                                                                                                                                                                                                          
    showtime:seat_map:{showtimeId}                                                                                                                                                                                        
      Type: HASH (compressed)                                                                                                                                                                                             
      Key: seatCode (A1, A2...)
      Value: "AVAILABLE" | "HELD" | "CONFIRMED"                                                                                                                                                                           
      D?ng: broadcast full seat map cho WebSocket clients khi connect                                                                                                                                                     

EXAMPLE:                                                                                                                                                                                                                
HSET seat:showtime-uuid-123:seat-uuid-456
status HELD                                                                                                                                                                                                      
bookingId booking-uuid-789
expiresAt 1714000200                                                                                                                                                                                             
category VIP
price 120000

    ZADD seat_holds:showtime-uuid-123
         1714000200 "booking-uuid-789:seat-uuid-456"                                                                                                                                                                      

LUA SCRIPT ― Atomic Hold (tr?nh race condition):                                                                                                                                                                        
local key = KEYS[1]  -- seat:showtime:seat
local holdKey = KEYS[2]  -- seat_holds:showtime                                                                                                                                                                       
local status = redis.call('HGET', key, 'status')
if status == 'AVAILABLE' or status == false then                                                                                                                                                                      
redis.call('HSET', key, 'status', 'HELD',
'bookingId', ARGV[1],                                                                                                                                                                                  
'expiresAt', ARGV[2])
redis.call('ZADD', holdKey, ARGV[2], ARGV[1]..':'..ARGV[3])                                                                                                                                                       
redis.call('DECR', 'showtime:available_count:'..ARGV[4])                                                                                                                                                          
return 1  -- success                                                                                                                                                                                              
end                                                                                                                                                                                                                   
return 0  -- already held/confirmed
                                                                                                                                                                                                                          
---
? booking-service ― booking_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_booking_schema.sql
-- Aggregate Root: Booking                                                                                                                                                                                              
-- Patterns: CQRS (write + read model), Outbox, Saga State
-- ??????????????????????????????????????????????????????????????????

-- ── bookings ─────────────────────────────────────────────────────────────────                                                                                                                                        
-- Aggregate Root (Write Model)
CREATE TABLE bookings (                                                                                                                                                                                                 
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
customer_id     UUID            NOT NULL,           -- ref → identity-service
showtime_id     UUID            NOT NULL,           -- ref → showtime-service                                                                                                                                       
cinema_id       UUID            NOT NULL,           -- denormalized                                                                                                                                                 
movie_id        UUID            NOT NULL,           -- denormalized
status          VARCHAR(30)     NOT NULL DEFAULT 'INITIATED',                                                                                                                                                       
subtotal        BIGINT          NOT NULL,           -- VND
discount_amount BIGINT          NOT NULL DEFAULT 0,                                                                                                                                                                 
final_amount    BIGINT          NOT NULL,
currency        VARCHAR(3)      NOT NULL DEFAULT 'VND',                                                                                                                                                             
voucher_code    VARCHAR(100),
payment_id      UUID,                               -- set sau InitiatePayment                                                                                                                                      
expires_at      TIMESTAMPTZ     NOT NULL,           -- created_at + 10 min
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT bookings_pkey PRIMARY KEY (id),                                                                                                                                                                          
      CONSTRAINT bookings_status_check CHECK (status IN (
          'INITIATED', 'SEATS_HELD', 'VOUCHER_APPLIED',                                                                                                                                                                   
          'PAYMENT_PENDING', 'CONFIRMING', 'CONFIRMED',
          'COMPENSATING', 'CANCELLED', 'FAILED'                                                                                                                                                                           
      )),         
      CONSTRAINT bookings_amount_check CHECK (final_amount >= 0 AND subtotal > 0),                                                                                                                                        
      CONSTRAINT bookings_discount_check CHECK (discount_amount >= 0)                                                                                                                                                     
);

-- ── booking_items ─────────────────────────────────────────────────────────────                                                                                                                                       
-- Entity b?n trong Booking aggregate
CREATE TABLE booking_items (                                                                                                                                                                                            
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
booking_id      UUID            NOT NULL REFERENCES bookings(id),                                                                                                                                                   
seat_id         UUID            NOT NULL,           -- ref → showtime-service seat
seat_code       VARCHAR(20)     NOT NULL,           -- denormalized (A1, B3...)                                                                                                                                     
seat_category   VARCHAR(20)     NOT NULL,
unit_price      BIGINT          NOT NULL,                                                                                                                                                                           
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

      CONSTRAINT booking_items_pkey PRIMARY KEY (id),                                                                                                                                                                     
      CONSTRAINT booking_items_booking_seat_unique UNIQUE (booking_id, seat_id)
);

-- ── saga_states ───────────────────────────────────────────────────────────────                                                                                                                                       
-- Track tr?ng th?i c?a Saga Orchestrator
CREATE TABLE saga_states (                                                                                                                                                                                              
booking_id          UUID        NOT NULL,
current_step        VARCHAR(50) NOT NULL,                                                                                                                                                                           
-- HOLD_SEATS|VALIDATE_VOUCHER|INITIATE_PAYMENT|AWAIT_PAYMENT
-- |CONFIRM_SEATS|COMPLETED|COMPENSATING|COMPENSATED                                                                                                                                                                
started_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),                                                                                                                                                             
completed_at        TIMESTAMPTZ,                                                                                                                                                                                    
compensation_reason TEXT,                                                                                                                                                                                           
step_history        JSONB       NOT NULL DEFAULT '[]',
-- [{step, timestamp, result}] ?? debug

      CONSTRAINT saga_states_pkey PRIMARY KEY (booking_id)                                                                                                                                                                
);

-- ── booking_read_model ────────────────────────────────────────────────────────                                                                                                                                       
-- CQRS Read Model ― denormalized view, updated b?i event handler
-- Cho ph?p query nhanh "l?ch s? ??t v? c?a t?i" kh?ng JOIN                                                                                                                                                             
CREATE TABLE booking_read_model (                                                                                                                                                                                       
id                  UUID        NOT NULL,                                                                                                                                                                           
customer_id         UUID        NOT NULL,                                                                                                                                                                           
movie_title         VARCHAR(500),
movie_poster_url    TEXT,                                                                                                                                                                                           
cinema_name         VARCHAR(300),                                                                                                                                                                                   
screen_name         VARCHAR(100),
showtime_start      TIMESTAMPTZ,                                                                                                                                                                                    
seat_codes          VARCHAR(20)[],                  -- ['A1', 'A2']                                                                                                                                                 
total_amount        BIGINT,
status              VARCHAR(30),                                                                                                                                                                                    
voucher_code        VARCHAR(100),
payment_method      VARCHAR(50),                                                                                                                                                                                    
qr_codes            TEXT[],                         -- set sau khi ticket issued
created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

      CONSTRAINT booking_read_model_pkey PRIMARY KEY (id)                                                                                                                                                                 
);

-- ── outbox_events ─────────────────────────────────────────────────────────────                                                                                                                                       
CREATE TABLE outbox_events (
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
aggregate_type  VARCHAR(100)    NOT NULL DEFAULT 'Booking',
aggregate_id    VARCHAR(255)    NOT NULL,
event_type      VARCHAR(200)    NOT NULL,                                                                                                                                                                           
payload         JSONB           NOT NULL,
metadata        JSONB           NOT NULL DEFAULT '{}',                                                                                                                                                              
status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
processed_at    TIMESTAMPTZ,
retry_count     INT             NOT NULL DEFAULT 0,                                                                                                                                                                 
error_message   TEXT,
CONSTRAINT outbox_events_pkey PRIMARY KEY (id)
);

-- ── processed_events ─────────────────────────────────────────────────────────                                                                                                                                        
-- Idempotency tracking cho event consumers
CREATE TABLE processed_events (                                                                                                                                                                                         
event_id        UUID            NOT NULL,
consumer_group  VARCHAR(100)    NOT NULL,                                                                                                                                                                           
processed_at    TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
CONSTRAINT processed_events_pkey PRIMARY KEY (event_id, consumer_group)
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
-- Query ch?nh: booking c?a user, sort theo th?i gian m?i nh?t
CREATE INDEX idx_bookings_customer_status                                                                                                                                                                               
ON bookings (customer_id, created_at DESC)
WHERE status NOT IN ('FAILED');

-- Timeout job query: t?m bookings PAYMENT_PENDING ?? h?t h?n                                                                                                                                                           
CREATE INDEX idx_bookings_expiry                                                                                                                                                                                        
ON bookings (expires_at)                                                                                                                                                                                            
WHERE status = 'PAYMENT_PENDING';

-- CQRS read model: query theo customer                                                                                                                                                                                 
CREATE INDEX idx_booking_read_model_customer
ON booking_read_model (customer_id, created_at DESC);

CREATE INDEX idx_booking_items_booking_id                                                                                                                                                                               
ON booking_items (booking_id);

CREATE INDEX idx_outbox_pending
ON outbox_events (status, created_at)
WHERE status = 'PENDING';

-- Cleanup index cho processed_events                                                                                                                                                                                   
CREATE INDEX idx_processed_events_age
ON processed_events (processed_at);
   
---                                                                                                                                                                                                                     
? payment-service ― payment_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_payment_schema.sql                                                                                                                                                                                        
-- Aggregate Root: Payment, Refund
-- ??????????????????????????????????????????????????????????????????

-- ── payments ─────────────────────────────────────────────────────────────────
-- Aggregate Root                                                                                                                                                                                                       
CREATE TABLE payments (
id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
booking_id          UUID            NOT NULL,
customer_id         UUID            NOT NULL,                                                                                                                                                                       
idempotency_key     VARCHAR(255)    NOT NULL,       -- bookingId + userId hash
amount              BIGINT          NOT NULL,                                                                                                                                                                       
currency            VARCHAR(3)      NOT NULL DEFAULT 'VND',
method              VARCHAR(30)     NOT NULL,       -- VNPAY|MOMO|CREDIT_CARD                                                                                                                                       
status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',                                                                                                                                                     
-- PENDING|PROCESSING|COMPLETED|FAILED|REFUNDED
-- PSP data (Value Object ― ACL boundary)                                                                                                                                                                           
psp_provider        VARCHAR(30),                    -- VNPAY|MOMO                                                                                                                                                   
psp_transaction_id  VARCHAR(200),                   -- ID t? PSP                                                                                                                                                    
psp_payment_url     TEXT,                           -- redirect URL                                                                                                                                                 
psp_raw_response    JSONB,                          -- raw PSP response (audit)                                                                                                                                     
-- Timestamps                                                                                                                                                                                                       
initiated_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                         
completed_at        TIMESTAMPTZ,                                                                                                                                                                                    
failed_at           TIMESTAMPTZ,
expires_at          TIMESTAMPTZ,                    -- payment window
created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                         
updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
version             BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT payments_pkey PRIMARY KEY (id),                                                                                                                                                                          
      CONSTRAINT payments_idempotency_key UNIQUE (idempotency_key),
      CONSTRAINT payments_booking_id_key UNIQUE (booking_id),  -- 1 booking = 1 payment                                                                                                                                   
      CONSTRAINT payments_status_check
          CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELLED')),                                                                                                                    
      CONSTRAINT payments_method_check
          CHECK (method IN ('VNPAY', 'MOMO', 'CREDIT_CARD', 'BANK_TRANSFER', 'WALLET')),                                                                                                                                  
      CONSTRAINT payments_amount_check CHECK (amount > 0)
);

-- ── refunds ───────────────────────────────────────────────────────────────────                                                                                                                                       
-- Aggregate Root (separate lifecycle t? Payment)
CREATE TABLE refunds (                                                                                                                                                                                                  
id                  UUID            NOT NULL DEFAULT gen_random_uuid(),
payment_id          UUID            NOT NULL REFERENCES payments(id),                                                                                                                                               
booking_id          UUID            NOT NULL,                                                                                                                                                                       
amount              BIGINT          NOT NULL,
reason              TEXT            NOT NULL,                                                                                                                                                                       
type                VARCHAR(20)     NOT NULL DEFAULT 'FULL',  -- FULL | PARTIAL
status              VARCHAR(20)     NOT NULL DEFAULT 'REQUESTED',                                                                                                                                                   
-- REQUESTED|PROCESSING|COMPLETED|FAILED
psp_refund_id       VARCHAR(200),                                                                                                                                                                                   
psp_raw_response    JSONB,
requested_at        TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                         
processed_at        TIMESTAMPTZ,
created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                         
updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
version             BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT refunds_pkey PRIMARY KEY (id),                                                                                                                                                                           
      CONSTRAINT refunds_status_check
          CHECK (status IN ('REQUESTED', 'PROCESSING', 'COMPLETED', 'FAILED')),                                                                                                                                           
      CONSTRAINT refunds_type_check CHECK (type IN ('FULL', 'PARTIAL'))
);

-- ── outbox_events ─────────────────────────────────────────────────────────────                                                                                                                                       
-- (identical structure to booking-service outbox)
CREATE TABLE outbox_events (                                                                                                                                                                                            
id              UUID        NOT NULL DEFAULT gen_random_uuid(),
aggregate_type  VARCHAR(100) NOT NULL,                                                                                                                                                                              
aggregate_id    VARCHAR(255) NOT NULL,
event_type      VARCHAR(200) NOT NULL,
payload         JSONB        NOT NULL,                                                                                                                                                                              
metadata        JSONB        NOT NULL DEFAULT '{}',
status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',                                                                                                                                                            
created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
processed_at    TIMESTAMPTZ,                                                                                                                                                                                        
retry_count     INT          NOT NULL DEFAULT 0,
error_message   TEXT,                                                                                                                                                                                               
CONSTRAINT outbox_events_pkey PRIMARY KEY (id)
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
CREATE INDEX idx_payments_booking_id    ON payments (booking_id);
CREATE INDEX idx_payments_customer_id   ON payments (customer_id, created_at DESC);                                                                                                                                     
CREATE INDEX idx_payments_status        ON payments (status, created_at)                                                                                                                                                
WHERE status IN ('PENDING', 'PROCESSING');
-- PSP webhook lookup: t?m payment theo psp_transaction_id                                                                                                                                                              
CREATE INDEX idx_payments_psp_txn       ON payments (psp_transaction_id)                                                                                                                                                
WHERE psp_transaction_id IS NOT NULL;                                                                                                                                                                               
CREATE INDEX idx_refunds_payment_id     ON refunds (payment_id);                                                                                                                                                        
CREATE INDEX idx_outbox_pending         ON outbox_events (status, created_at)                                                                                                                                           
WHERE status = 'PENDING';
   
---                                                                                                                                                                                                                     
? promotion-service ― promotion_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_promotion_schema.sql                                                                                                                                                                                      
-- Aggregate Root: Promotion, Voucher
-- ??????????????????????????????????????????????????????????????????

-- ── promotions ────────────────────────────────────────????????????────────────                                                                                                                                       
-- Aggregate Root
CREATE TABLE promotions (                                                                                                                                                                                               
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
name            VARCHAR(300)    NOT NULL,
description     TEXT,
type            VARCHAR(20)     NOT NULL,           -- PERCENTAGE | FIXED_AMOUNT
discount_value  BIGINT          NOT NULL,           -- % ho?c VND                                                                                                                                                   
max_discount    BIGINT,                             -- cap cho PERCENTAGE type                                                                                                                                      
min_order_amount BIGINT         NOT NULL DEFAULT 0,                                                                                                                                                                 
applicable_days VARCHAR(3)[],                       -- ['MON','TUE'...] NULL = all days                                                                                                                             
applicable_times JSONB,                             -- [{from:'08:00',to:'12:00'}]                                                                                                                                  
applicable_movie_ids UUID[],                        -- NULL = all movies                                                                                                                                            
applicable_cinema_ids UUID[],                       -- NULL = all cinemas                                                                                                                                           
max_uses_total  INT,                                -- NULL = unlimited                                                                                                                                             
max_uses_per_user INT           NOT NULL DEFAULT 1,                                                                                                                                                                 
current_uses    INT             NOT NULL DEFAULT 0,
status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',                                                                                                                                                           
-- DRAFT|ACTIVE|PAUSED|ENDED                                                                                                                                    
starts_at       TIMESTAMPTZ     NOT NULL,
ends_at         TIMESTAMPTZ     NOT NULL,                                                                                                                                                                           
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT promotions_pkey PRIMARY KEY (id),
      CONSTRAINT promotions_type_check CHECK (type IN ('PERCENTAGE', 'FIXED_AMOUNT')),                                                                                                                                    
      CONSTRAINT promotions_status_check
          CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'ENDED')),
      CONSTRAINT promotions_date_check CHECK (ends_at > starts_at),                                                                                                                                                       
      CONSTRAINT promotions_discount_check CHECK (discount_value > 0)                                                                                                                                                     
);

-- ── vouchers ──────────────────────────────────────────────────────────────────
-- Entity b?n trong Promotion aggregate
CREATE TABLE vouchers (
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
promotion_id    UUID            NOT NULL REFERENCES promotions(id),                                                                                                                                                 
code            VARCHAR(50)     NOT NULL,
status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',                                                                                                                                                          
-- ACTIVE|REDEEMED|VOIDED|EXPIRED
owner_id        UUID,                               -- NULL = public voucher                                                                                                                                        
redeemed_by     UUID,                               -- customer_id khi d?ng                                                                                                                                         
booking_id      UUID,                                                                                                                                                                                               
redeemed_at     TIMESTAMPTZ,                                                                                                                                                                                        
voided_at       TIMESTAMPTZ,
void_reason     TEXT,                                                                                                                                                                                               
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT vouchers_pkey PRIMARY KEY (id),
      CONSTRAINT vouchers_code_key UNIQUE (code),                                                                                                                                                                         
      CONSTRAINT vouchers_status_check
          CHECK (status IN ('ACTIVE', 'REDEEMED', 'VOIDED', 'EXPIRED'))                                                                                                                                                   
);

-- ── voucher_usage_log ─────────────────────────────────────────────────────────                                                                                                                                       
-- Immutable audit log ― KH?NG update, ch? INSERT
-- D?ng ??: idempotency check, analytics, dispute resolution                                                                                                                                                            
CREATE TABLE voucher_usage_log (                                                                                                                                                                                        
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
voucher_id      UUID            NOT NULL REFERENCES vouchers(id),                                                                                                                                                   
promotion_id    UUID            NOT NULL,
customer_id     UUID            NOT NULL,                                                                                                                                                                           
booking_id      UUID            NOT NULL,
action          VARCHAR(20)     NOT NULL,           -- VALIDATED|REDEEMED|VOIDED                                                                                                                                    
discount_applied BIGINT         NOT NULL DEFAULT 0,                                                                                                                                                                 
occurred_at     TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
idempotency_key VARCHAR(255)    NOT NULL,           -- bookingId + action

      CONSTRAINT voucher_usage_log_pkey PRIMARY KEY (id),
      CONSTRAINT voucher_usage_log_idempotency UNIQUE (idempotency_key)                                                                                                                                                   
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
CREATE INDEX idx_promotions_status_dates
ON promotions (status, starts_at, ends_at)                                                                                                                                                                          
WHERE status = 'ACTIVE';
-- Lookup code → voucher (hot path trong booking flow)                                                                                                                                                                  
CREATE INDEX idx_vouchers_code     ON vouchers (code);                                                                                                                                                                  
CREATE INDEX idx_vouchers_owner    ON vouchers (owner_id) WHERE owner_id IS NOT NULL;
CREATE INDEX idx_vouchers_status   ON vouchers (promotion_id, status);                                                                                                                                                  
-- Idempotency: ?? user n?y d?ng promotion n?y ch?a?
CREATE INDEX idx_usage_log_customer_promotion                                                                                                                                                                           
ON voucher_usage_log (customer_id, promotion_id, action);
                                                                                                                                                                                                                          
---             
? ticket-service ― ticket_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_ticket_schema.sql
-- Aggregate Root: Ticket                                                                                                                                                                                               
-- ??????????????????????????????????????????????????????????????????

CREATE TABLE tickets (
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
booking_id      UUID            NOT NULL,
booking_item_id UUID            NOT NULL,           -- 1 ticket = 1 seat                                                                                                                                            
customer_id     UUID            NOT NULL,
showtime_id     UUID            NOT NULL,                                                                                                                                                                           
-- Denormalized data (snapshot t?i th?i ?i?m issue ― b?t bi?n)
movie_title     VARCHAR(500)    NOT NULL,                                                                                                                                                                           
cinema_name     VARCHAR(300)    NOT NULL,
screen_name     VARCHAR(100)    NOT NULL,                                                                                                                                                                           
showtime_start  TIMESTAMPTZ     NOT NULL,
seat_code       VARCHAR(20)     NOT NULL,                                                                                                                                                                           
seat_category   VARCHAR(20)     NOT NULL,                                                                                                                                                                           
price           BIGINT          NOT NULL,
-- QR data                                                                                                                                                                                                          
qr_token        VARCHAR(500)    NOT NULL,           -- signed JWT ch?a ticketId
qr_url          TEXT,                               -- URL to QR image in S3                                                                                                                                        
status          VARCHAR(20)     NOT NULL DEFAULT 'ISSUED',                                                                                                                                                          
-- ISSUED|USED|EXPIRED|VOIDED                                                                                                                                   
issued_at       TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
used_at         TIMESTAMPTZ,                                                                                                                                                                                        
used_by_staff   UUID,                               -- staff_id validate
expires_at      TIMESTAMPTZ     NOT NULL,           -- showtime_start + 30 min                                                                                                                                      
voided_at       TIMESTAMPTZ,                                                                                                                                                                                        
void_reason     TEXT,                                                                                                                                                                                               
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT tickets_pkey PRIMARY KEY (id),                                                                                                                                                                           
      CONSTRAINT tickets_booking_item_unique UNIQUE (booking_item_id),  -- 1-1
      CONSTRAINT tickets_qr_token_unique UNIQUE (qr_token),                                                                                                                                                               
      CONSTRAINT tickets_status_check                                                                                                                                                                                     
          CHECK (status IN ('ISSUED', 'USED', 'EXPIRED', 'VOIDED'))
);

-- ── INDEXES ───────────────────────────────────────────────────────────────────                                                                                                                                       
CREATE INDEX idx_tickets_booking_id    ON tickets (booking_id);
CREATE INDEX idx_tickets_customer_id   ON tickets (customer_id, issued_at DESC);                                                                                                                                        
-- QR scan lookup ― c?c k? hot path t?i r?p                                                                                                                                                                             
CREATE INDEX idx_tickets_qr_token      ON tickets (qr_token);                                                                                                                                                           
-- Staff query: tickets c?a showtime X ch?a d?ng                                                                                                                                                                        
CREATE INDEX idx_tickets_showtime_status                                                                                                                                                                                
ON tickets (showtime_id, status)
WHERE status = 'ISSUED';
                  
---                                                                                                                                                                                                                     
? notification-service ― notif_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_notification_schema.sql                                                                                                                                                                                   
-- ??????????????????????????????????????????????????????????????????

CREATE TABLE notification_templates (
id          UUID        NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                         
name        VARCHAR(100) NOT NULL,                  -- BOOKING_CONFIRMED, TICKET_ISSUED...
channel     VARCHAR(20) NOT NULL,                   -- EMAIL | PUSH | SMS                                                                                                                                           
subject     VARCHAR(500),                                                                                                                                                                                           
body_html   TEXT,                                                                                                                                                                                                   
body_text   TEXT,                                                                                                                                                                                                   
variables   TEXT[],                                 -- ['customerName', 'movieTitle'...]
is_active   BOOLEAN     NOT NULL DEFAULT TRUE,                                                                                                                                                                      
created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
CONSTRAINT notification_templates_pkey PRIMARY KEY (id),                                                                                                                                                            
CONSTRAINT notification_templates_name_channel UNIQUE (name, channel)
);

CREATE TABLE notification_logs (
id              UUID            NOT NULL DEFAULT gen_random_uuid(),                                                                                                                                                 
idempotency_key VARCHAR(255)    NOT NULL,           -- eventId + channel
recipient_id    UUID            NOT NULL,                                                                                                                                                                           
recipient_email VARCHAR(255),
recipient_token TEXT,                               -- FCM token                                                                                                                                                    
channel         VARCHAR(20)     NOT NULL,                                                                                                                                                                           
template_name   VARCHAR(100)    NOT NULL,
subject         VARCHAR(500),                                                                                                                                                                                       
status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
-- PENDING|SENT|FAILED|SKIPPED                                                                                                                                  
provider        VARCHAR(50),                        -- SENDGRID | FCM                                                                                                                                               
provider_message_id VARCHAR(200),                                                                                                                                                                                   
error_message   TEXT,                                                                                                                                                                                               
sent_at         TIMESTAMPTZ,                                                                                                                                                                                        
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

      CONSTRAINT notification_logs_pkey PRIMARY KEY (id),
      CONSTRAINT notification_logs_idempotency UNIQUE (idempotency_key)                                                                                                                                                   
);

CREATE INDEX idx_notif_logs_recipient   ON notification_logs (recipient_id, created_at DESC);                                                                                                                           
CREATE INDEX idx_notif_logs_status      ON notification_logs (status, created_at)
WHERE status = 'PENDING';
                                                                                                                                                                                                                          
---
? chat-service ― chat_db

-- ??????????????????????????????????????????????????????????????????
-- V1__create_chat_schema.sql
-- Aggregate Root: Conversation                                                                                                                                                                                         
-- ??????????????????????????????????????????????????????????????????

CREATE TABLE conversations (
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
customer_id     UUID            NOT NULL,
agent_id        UUID,                               -- NULL n?u ch?a assign                                                                                                                                         
booking_id      UUID,                               -- context (optional)
subject         VARCHAR(500),                                                                                                                                                                                       
status          VARCHAR(20)     NOT NULL DEFAULT 'OPEN',
-- OPEN|ASSIGNED|RESOLVED|CLOSED                                                                                                                                
priority        VARCHAR(10)     NOT NULL DEFAULT 'NORMAL',  -- LOW|NORMAL|HIGH                                                                                                                                      
last_message_at TIMESTAMPTZ,                                                                                                                                                                                        
created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),                                                                                                                                                             
resolved_at     TIMESTAMPTZ,
version         BIGINT          NOT NULL DEFAULT 0,

      CONSTRAINT conversations_pkey PRIMARY KEY (id),                                                                                                                                                                     
      CONSTRAINT conversations_status_check
          CHECK (status IN ('OPEN', 'ASSIGNED', 'RESOLVED', 'CLOSED'))
);

CREATE TABLE messages (                                                                                                                                                                                                 
id              UUID            NOT NULL DEFAULT gen_random_uuid(),
conversation_id UUID            NOT NULL REFERENCES conversations(id),
sender_id       UUID            NOT NULL,
sender_type     VARCHAR(20)     NOT NULL,           -- CUSTOMER | AGENT | SYSTEM                                                                                                                                    
content         TEXT            NOT NULL,
content_type    VARCHAR(20)     NOT NULL DEFAULT 'TEXT',  -- TEXT|IMAGE|FILE                                                                                                                                        
attachment_url  TEXT,                                                                                                                                                                                               
is_read         BOOLEAN         NOT NULL DEFAULT FALSE,
sent_at         TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

      CONSTRAINT messages_pkey PRIMARY KEY (id),                                                                                                                                                                          
      CONSTRAINT messages_sender_type_check
          CHECK (sender_type IN ('CUSTOMER', 'AGENT', 'SYSTEM'))
);

-- PARTITION messages by month (messages table s? l?n r?t nhanh)                                                                                                                                                        
-- Th?c hi?n sau khi ??t >1M records
-- ALTER TABLE messages PARTITION BY RANGE (sent_at);                                                                                                                                                                   
-- CREATE TABLE messages_2024_04 PARTITION OF messages                                                                                                                                                                  
--     FOR VALUES FROM ('2024-04-01') TO ('2024-05-01');

CREATE INDEX idx_conversations_customer ON conversations (customer_id, created_at DESC);
CREATE INDEX idx_conversations_agent    ON conversations (agent_id, status)                                                                                                                                             
WHERE status IN ('OPEN', 'ASSIGNED');                                                                                                                                                                               
CREATE INDEX idx_messages_conversation  ON messages (conversation_id, sent_at ASC);
CREATE INDEX idx_messages_unread                                                                                                                                                                                        
ON messages (conversation_id, is_read)
WHERE is_read = FALSE;
                  
---                                                                                                                                                                                                                     
4.2 Aggregate Root / Entity / Value Object ― Code vs DB Mapping

┌─────────────────────────────────────────────────────────────────────────────┐
│                    DDD TACTICAL PATTERNS → DATABASE MAPPING                 │                                                                                                                                         
├──────────────────┬──────────────────────────┬──────────────────────────────┤
│ DDD Concept      │ Java Code                │ Database                      │                                                                                                                                         
├──────────────────┼──────────────────────────┼──────────────────────────────┤                                                                                                                                          
│ Aggregate Root   │ @Entity + @Version       │ B?ng ri?ng + version column  │                                                                                                                                          
│                  │ Booking, Payment...       │ bookings, payments...         │                                                                                                                                        
├──────────────────┼──────────────────────────┼──────────────────────────────┤                                                                                                                                          
│ Entity           │ @Entity (no @Version)    │ B?ng ri?ng, FK → parent      │                                                                                                                                          
│                  │ BookingItem, Cast...      │ booking_items, casts...       │                                                                                                                                        
├──────────────────┼──────────────────────────┼──────────────────────────────┤
│ Value Object     │ @Embeddable ho?c record  │ Embedded columns HO?C        │                                                                                                                                          
│ (single table)   │ Money, SeatCode,         │ columns trong parent table   │                                                                                                                                          
│                  │ VoucherCode              │ amount + currency columns     │                                                                                                                                         
├──────────────────┼──────────────────────────┼──────────────────────────────┤                                                                                                                                          
│ Value Object     │ @Embeddable              │ Prefix columns trong b?ng    │                                                                                                                                          
│ (complex)        │ Address {street,city...} │ address_street, address_city  │                                                                                                                                         
├──────────────────┼──────────────────────────┼──────────────────────────────┤                                                                                                                                          
│ Domain Event     │ record implements        │ outbox_events table          │                                                                                                                                          
│                  │ DomainEvent              │ (payload as JSONB)            │                                                                                                                                         
├──────────────────┼──────────────────────────┼──────────────────────────────┤                                                                                                                                          
│ Repository       │ interface extends        │ Spring Data JPA              │                                                                                                                                          
│                  │ JpaRepository<AR, ID>    │ @Repository impl              │                                                                                                                                         
├──────────────────┼──────────────────────────┼──────────────────────────────┤                                                                                                                                          
│ Domain Service   │ @DomainService           │ N/A (pure logic)             │                                                                                                                                          
│                  │ SeatPricingService       │                               │                                                                                                                                         
└──────────────────┴──────────────────────────┴──────────────────────────────┘

// Value Objects ― kh?ng c? identity, so s?nh b?ng value                                                                                                                                                                
// booking-service/domain/model/Money.java                                                                                                                                                                              
public record Money(BigDecimal amount, Currency currency) {

      public static final Money ZERO = new Money(BigDecimal.ZERO, Currency.getInstance("VND"));
                                                                                                                                                                                                                          
      public Money {                                                                                                                                                                                                      
          Objects.requireNonNull(amount);
          Objects.requireNonNull(currency);                                                                                                                                                                               
          if (amount.compareTo(BigDecimal.ZERO) < 0) {
              throw new IllegalArgumentException("Money amount cannot be negative");
          }                                                                                                                                                                                                               
      }
                                                                                                                                                                                                                          
      public static Money of(long amountInMinorUnit, String currencyCode) {                                                                                                                                               
          return new Money(
              BigDecimal.valueOf(amountInMinorUnit),                                                                                                                                                                      
              Currency.getInstance(currencyCode)
          );                                                                                                                                                                                                              
      }
                                                                                                                                                                                                                          
      public Money add(Money other) {
          assertSameCurrency(other);
          return new Money(amount.add(other.amount), currency);
      }                                                                                                                                                                                                                   
   
      public Money minus(Money other) {                                                                                                                                                                                   
          assertSameCurrency(other);
          var result = amount.subtract(other.amount);
          if (result.compareTo(BigDecimal.ZERO) < 0) {
              throw new IllegalArgumentException("Result cannot be negative");                                                                                                                                            
          }
          return new Money(result, currency);                                                                                                                                                                             
      }           

      private void assertSameCurrency(Money other) {                                                                                                                                                                      
          if (!currency.equals(other.currency)) {
              throw new IllegalArgumentException(                                                                                                                                                                         
                  "Currency mismatch: " + currency + " vs " + other.currency);
          }
      }
}

// booking-service/domain/model/BookingId.java
public record BookingId(UUID value) {

      public BookingId {
          Objects.requireNonNull(value, "BookingId cannot be null");
      }                                                                                                                                                                                                                   
   
      public static BookingId generate() {                                                                                                                                                                                
          return new BookingId(UUID.randomUUID());
      }

      public static BookingId of(UUID value) {                                                                                                                                                                            
          return new BookingId(value);
      }                                                                                                                                                                                                                   
                  
      public static BookingId of(String value) {
          return new BookingId(UUID.fromString(value));
      }
}

// JPA converter cho Value Object ? DB column                                                                                                                                                                           
// booking-service/infrastructure/persistence/converter/BookingIdConverter.java
@Converter(autoApply = true)                                                                                                                                                                                            
public class BookingIdConverter implements AttributeConverter<BookingId, UUID> {

      @Override   
      public UUID convertToDatabaseColumn(BookingId attribute) {
          return attribute != null ? attribute.value() : null;                                                                                                                                                            
      }
                                                                                                                                                                                                                          
      @Override   
      public BookingId convertToEntityAttribute(UUID dbData) {
          return dbData != null ? BookingId.of(dbData) : null;
      }                                                                                                                                                                                                                   
}
                                                                                                                                                                                                                          
---             
4.3 Index Strategy & Partitioning

graph TD
subgraph INDEX_STRATEGY["Index Strategy ― Quy?t ??nh theo query pattern"]
I1["B-Tree Index\n──────────────\nD?ng cho: equality, range,\nORDER BY, FK lookups\nV? d?: idx_bookings_customer_status\nidx_payments_booking_id"]

          I2["Partial Index\n──────────────\nD?ng cho: ch? index subset rows\nTi?t ki?m 60-80% space\nV? d?: WHERE status = 'PENDING'\nWHERE deleted_at IS NULL"]                                                         
                                                                                                                                                                                                                          
          I3["GIN Index\n──────────────\nD?ng cho: JSONB, arrays,\nfull-text search\nV? d?: movie title trigram search\nidx_movies_title_trgm"]                                                                           
                  
          I4["GiST Index\n──────────────\nD?ng cho: geographic queries\n'r?p g?n t?i'\nidx_cinemas_location\nll_to_earth()"]                                                                                              
                  
          I5["Composite Index\n──────────────\nD?ng cho: multi-column WHERE\nTh? t?: equality tr??c,\nrange sau\n(customer_id, created_at DESC)"]                                                                         
      end         
                                                                                                                                                                                                                          
      subgraph PARTITION["Partitioning Strategy"]
          P1["seat_snapshots\n──────────────\nPartition by: showtime_id HASH\n(16 partitions)\nL? do: m?i query lu?n c?\nshowtimeId, tr?nh full scan\nSize: ~500 rows/showtime\n→ 16 partitions = manageable"]
                                                                                                                                                                                                                          
          P2["messages (chat)\n──────────────\nPartition by: RANGE (sent_at)\nM?i th?ng 1 partition\nL? do: query lu?n c?\ndate range, old data archive\nDROP partition thay DELETE"]                                     
                                                                                                                                                                                                                          
          P3["outbox_events\n──────────────\nPartition by: RANGE (created_at)\nM?i tu?n 1 partition\nL? do: processed events\nc? th? archive/drop theo week\nTr?nh table bloat"]                                          
                  
          P4["notification_logs\n──────────────\nPartition by: RANGE (created_at)\nM?i th?ng 1 partition\nL? do: volume cao nh?t\n~10 notifs per booking"]                                                                
      end         

V? d? Partitioning setup:

-- Partition seat_snapshots by HASH(showtime_id) ― cho showtime-service
CREATE TABLE seat_snapshots (                                                                                                                                                                                           
id              UUID        NOT NULL,
showtime_id     UUID        NOT NULL,                                                                                                                                                                               
seat_id         UUID        NOT NULL,
-- ... other columns                                                                                                                                                                                                
) PARTITION BY HASH (showtime_id);

-- 16 partitions ― ?? cho scale-out                                                                                                                                                                                     
CREATE TABLE seat_snapshots_p0  PARTITION OF seat_snapshots
FOR VALUES WITH (MODULUS 16, REMAINDER 0);                                                                                                                                                                          
CREATE TABLE seat_snapshots_p1  PARTITION OF seat_snapshots
FOR VALUES WITH (MODULUS 16, REMAINDER 1);                                                                                                                                                                          
-- ... p2 ??n p15

-- Index tr?n t?ng partition (auto-inherited t? parent)                                                                                                                                                                 
CREATE INDEX ON seat_snapshots (showtime_id, seat_id);
CREATE INDEX ON seat_snapshots (showtime_id, status) WHERE status = 'HELD';

-- ─────────────────────────────────────────────────────────────────────────────                                                                                                                                        
-- Partition messages by month (chat-service) ― range partitioning
CREATE TABLE messages (                                                                                                                                                                                                 
id              UUID        NOT NULL,
conversation_id UUID        NOT NULL,                                                                                                                                                                               
sent_at         TIMESTAMPTZ NOT NULL,
-- ...                                                                                                                                                                                                              
) PARTITION BY RANGE (sent_at);

-- T?o partitions t??ng lai b?ng pg_cron job                                                                                                                                                                            
CREATE TABLE messages_2026_04 PARTITION OF messages
FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');

CREATE TABLE messages_2026_05 PARTITION OF messages                                                                                                                                                                     
FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');

-- T? ??ng t?o partition th?ng t?i (ch?y ??u m?i th?ng)                                                                                                                                                                 
-- pg_cron: SELECT cron.schedule('0 0 25 * *', $$                                                                                                                                                                       
--   SELECT create_monthly_partition('messages', NOW() + INTERVAL '1 month');                                                                                                                                           
-- $$);

B?ng "nguy hi?m" c?n quan t?m:

┌─────────────────────┬──────────────┬────────────────────────────────────────┐                                                                                                                                         
│ B?ng                │ Est. rows/yr │ Chi?n l??c                             │                                                                                                                                         
├─────────────────────┼──────────────┼────────────────────────────────────────┤
│ seat_snapshots      │ ~36M         │ HASH partition by showtime_id (16)      │                                                                                                                                        
│ (500 gh? × 200      │              │ Purge sau showtime k?t th?c 90 ng?y     │                                                                                                                                        
│  showtimes/r?p/ng?y)│              │                                        │                                                                                                                                         
├─────────────────────┼──────────────┼────────────────────────────────────────┤                                                                                                                                         
│ outbox_events       │ ~5M          │ RANGE partition by week                │                                                                                                                                         
│ (booking+payment)   │              │ Drop partition sau 30 ng?y             │                                                                                                                                         
├─────────────────────┼──────────────┼────────────────────────────────────────┤                                                                                                                                         
│ notification_logs   │ ~10M         │ RANGE partition by month               │                                                                                                                                         
│                     │              │ Archive cold data sang S3 sau 6 th?ng  │                                                                                                                                         
├─────────────────────┼──────────────┼────────────────────────────────────────┤                                                                                                                                         
│ messages (chat)     │ ~20M         │ RANGE partition by month               │                                                                                                                                         
│                     │              │ Hot: 3 th?ng g?n nh?t                  │                                                                                                                                         
├─────────────────────┼──────────────┼────────────────────────────────────────┤                                                                                                                                         
│ processed_events    │ ~5M          │ TTL cleanup job: DELETE WHERE           │                                                                                                                                        
│                     │              │ processed_at < NOW() - 7 days          │                                                                                                                                         
└─────────────────────┴──────────────┴────────────────────────────────────────┘

?? Nh?ng ?i?m quan tr?ng b?n c?n l?u ?:

1. Flyway Naming Convention:                                                                                                                                                                                            
   V{version}__{description}.sql
   V1__create_booking_schema.sql                                                                                                                                                                                           
   V2__add_booking_read_model.sql
   V3__add_processed_events_table.sql
   R__seed_roles.sql   (Repeatable migration ― ch?y l?i khi checksum thay ??i)

2. Zero-downtime Migration v?i Flyway:                                                                                                                                                                                  
   KH?NG bao gi?:  
   - DROP COLUMN trong c?ng release v?i code remove                                                                                                                                                                      
   - RENAME COLUMN tr?c ti?p                       
   - ADD NOT NULL column kh?ng c? DEFAULT

THAY V?O ?? ― Expand/Contract pattern:                                                                                                                                                                                  
Sprint N:   ADD COLUMN new_col (nullable)                                                                                                                                                                             
Sprint N+1: Code vi?t v?o c? old_col + new_col
Sprint N+2: Backfill old_col → new_col                                                                                                                                                                                
Sprint N+3: DROP COLUMN old_col

3. Row-level Security (Optional nh?ng n?n c? cho payment_db):                                                                                                                                                           
   -- Ch? application user ??c ???c payment c?a m?nh                                                                                                                                                                       
   ALTER TABLE payments ENABLE ROW LEVEL SECURITY;  
   CREATE POLICY payment_isolation ON payments                                                                                                                                                                             
   USING (customer_id = current_setting('app.current_user_id')::UUID);

4. AUTOVACUUM tuning cho high-write tables:                                                                                                                                                                             
   -- outbox_events b? INSERT/UPDATE li?n t?c → c?n aggressive vacuum                                                                                                                                                      
   ALTER TABLE outbox_events SET (                                                                                                                                                                                         
   autovacuum_vacuum_scale_factor = 0.01,  -- vacuum khi 1% rows dead                                                                                                                                                  
   autovacuum_analyze_scale_factor = 0.005
   );

  ---                                                                                                                                                                                                                     
Phase 4 ho?n th?nh ― to?n b? 9 databases ?? c? DDL production-ready. G? "phase 5" ?? t?i thi?t k? Contract Design: .proto files cho gRPC, Event Schema cho RabbitMQ, OpenAPI spec cho REST Gateway, v? WebSocket topics.

PHASE 5 ― CONTRACT DESIGN

5.1 gRPC ― .proto Files

Shared Types (d?ng chung gi?a c?c proto):

// proto/common/v1/common.proto                                                                                                                                                                                         
syntax = "proto3";

package cinetix.common.v1;

option java_package = "com.cinetix.grpc.common.v1";                                                                                                                                                                     
option java_multiple_files = true;

import "google/protobuf/timestamp.proto";

// ── Money ─────────────────────────────────────────────────────────────────────                                                                                                                                       
message Money {
int64  amount_minor = 1;   // VND stored as integer (kh?ng d?ng float!)                                                                                                                                               
string currency     = 2;   // "VND"                                                                                                                                                                                   
}

// ── Error ─────────────────────────────────────────────────────────────────────                                                                                                                                       
message Error {
string code    = 1;   // SEATS_UNAVAILABLE, VOUCHER_EXPIRED, INSUFFICIENT_FUNDS...
string message = 2;   // human-readable                                                                                                                                                                               
map<string, string> details = 3;  // extra context
}

// ── PageRequest / PageResponse ────────────────────────────────────────────────                                                                                                                                       
message PageRequest {
int32 page = 1;   // 0-based
int32 size = 2;   // default 20                                                                                                                                                                                       
}

message PageInfo {
int32 total_elements = 1;
int32 total_pages    = 2;                                                                                                                                                                                             
int32 current_page   = 3;
}
                  
---                                                                                                                                                                                                                     
showtime.proto

// proto/showtime/v1/showtime.proto
syntax = "proto3";

package cinetix.showtime.v1;

option java_package = "com.cinetix.grpc.showtime.v1";                                                                                                                                                                   
option java_multiple_files = true;
option java_outer_classname = "ShowtimeProto";

import "google/protobuf/timestamp.proto";
import "common/v1/common.proto";

// ?????????????????????????????????????????????????????????????????????????????
// ShowtimeService ― called by booking-service (Saga Orchestrator)                                                                                                                                                      
// ?????????????????????????????????????????????????????????????????????????????                                                                                                                                        
service ShowtimeService {

    // L?y th?ng tin showtime + gi? gh? (d?ng ?? validate tr??c khi t?o booking)                                                                                                                                          
    rpc GetShowtime (GetShowtimeRequest) returns (GetShowtimeResponse);
                                                                                                                                                                                                                          
    // L?y danh s?ch showtime theo phim + ng?y (cho browse UI, optional qua gRPC)                                                                                                                                         
    rpc ListShowtimes (ListShowtimesRequest) returns (ListShowtimesResponse);
                                                                                                                                                                                                                          
    // [SAGA STEP 1] Atomic hold seats v?i TTL ― d?ng Lua script tr?n Redis                                                                                                                                               
    rpc HoldSeats (HoldSeatsRequest) returns (HoldSeatsResponse);                                                                                                                                                         
                                                                                                                                                                                                                          
    // [SAGA COMPENSATION] Release held seats (khi payment fail / cancel)                                                                                                                                                 
    rpc ReleaseSeats (ReleaseSeatsRequest) returns (ReleaseSeatsResponse);                                                                                                                                                
                                                                                                                                                                                                                          
    // [SAGA STEP 4] Convert HELD → CONFIRMED sau payment success                                                                                                                                                         
    rpc ConfirmSeats (ConfirmSeatsRequest) returns (ConfirmSeatsResponse);
                                                                                                                                                                                                                          
    // [SAGA COMPENSATION] Force release khi booking timeout                                                                                                                                                              
    rpc ForceReleaseByBooking (ForceReleaseByBookingRequest) returns (ReleaseSeatsResponse);
}

// ── GetShowtime ───────────────────────────────────────────────────────────────                                                                                                                                       
message GetShowtimeRequest {
string showtime_id = 1;                                                                                                                                                                                               
}

message GetShowtimeResponse {
oneof result {
ShowtimeDetail showtime = 1;
cinetix.common.v1.Error error = 2;
}                                                                                                                                                                                                                     
}

message ShowtimeDetail {
string   showtime_id      = 1;
string   movie_id         = 2;
string   movie_title      = 3;                                                                                                                                                                                        
string   cinema_id        = 4;
string   cinema_name      = 5;                                                                                                                                                                                        
string   screen_id        = 6;
string   screen_name      = 7;                                                                                                                                                                                        
string   format           = 8;   // 2D | 3D | IMAX
string   audio_type       = 9;   // DUBBED | SUBTITLED                                                                                                                                                                
google.protobuf.Timestamp start_time  = 10;
google.protobuf.Timestamp end_time    = 11;                                                                                                                                                                           
string   status           = 12;  // SCHEDULED | OPEN | FULL | CANCELLED
int32    total_seats      = 13;                                                                                                                                                                                       
int32    available_seats  = 14;
cinetix.common.v1.Money base_price   = 15;                                                                                                                                                                            
cinetix.common.v1.Money vip_price    = 16;
cinetix.common.v1.Money couple_price = 17;                                                                                                                                                                            
}

// ── ListShowtimes ─────────────────────────────────────────────────────────────                                                                                                                                       
message ListShowtimesRequest {
string movie_id   = 1;                                                                                                                                                                                                
string cinema_id  = 2;          // optional
string date       = 3;          // "2026-04-22" ISO format
string format     = 4;          // optional filter                                                                                                                                                                    
cinetix.common.v1.PageRequest page = 5;                                                                                                                                                                               
}

message ListShowtimesResponse {
repeated ShowtimeDetail showtimes = 1;
cinetix.common.v1.PageInfo        page_info = 2;
}

// ── HoldSeats ─────────────────────────────────────────────────────────────────                                                                                                                                       
message HoldSeatsRequest {
string   showtime_id     = 1;                                                                                                                                                                                         
repeated string seat_ids = 2;   // max 8 seats per booking
string   booking_id      = 3;   // correlation ID ― d?ng cho Redis value                                                                                                                                              
int32    ttl_seconds     = 4;   // 600 (10 ph?t)                                                                                                                                                                      
string   idempotency_key = 5;   // bookingId ― tr?nh double-hold n?u retry                                                                                                                                            
}

message HoldSeatsResponse {                                                                                                                                                                                             
oneof result {
HoldSeatsSuccess success = 1;
HoldSeatsFailure failure = 2;                                                                                                                                                                                       
}
}

message HoldSeatsSuccess {
google.protobuf.Timestamp expires_at        = 1;
repeated SeatHoldDetail   held_seats        = 2;
int32                     remaining_seats   = 3;  // seats available sau hold                                                                                                                                         
}

message HoldSeatsFailure {                                                                                                                                                                                              
cinetix.common.v1.Error   error             = 1;
repeated string           unavailable_seats = 2;  // seatIds ?? b? taken                                                                                                                                              
}

message SeatHoldDetail {                                                                                                                                                                                                
string seat_id       = 1;
string seat_code     = 2;  // A1, B3...
string category      = 3;  // STANDARD | VIP | COUPLE                                                                                                                                                                 
cinetix.common.v1.Money price = 4;                                                                                                                                                                                    
}

// ── ReleaseSeats ──────────────────────────────────────────────────────────────                                                                                                                                       
message ReleaseSeatsRequest {
string   showtime_id          = 1;                                                                                                                                                                                    
repeated string seat_ids      = 2;
string   booking_id           = 3;  // validate ownership tr??c khi release
string   reason               = 4;  // PAYMENT_FAILED | BOOKING_CANCELLED | TIMEOUT                                                                                                                                   
}

message ReleaseSeatsResponse {                                                                                                                                                                                          
bool    success          = 1;
int32   released_count   = 2;
cinetix.common.v1.Error error = 3;  // populated n?u partial failure                                                                                                                                                  
}

// ── ConfirmSeats ──────────────────────────────────────────────────────────────                                                                                                                                       
message ConfirmSeatsRequest {
string   showtime_id     = 1;
repeated string seat_ids = 2;
string   booking_id      = 3;                                                                                                                                                                                         
string   idempotency_key = 4;  // bookingId ― safe to retry
}

message ConfirmSeatsResponse {                                                                                                                                                                                          
oneof result {
ConfirmSeatsSuccess success = 1;                                                                                                                                                                                    
cinetix.common.v1.Error error   = 2;
}
}

message ConfirmSeatsSuccess {                                                                                                                                                                                           
google.protobuf.Timestamp confirmed_at = 1;
repeated string confirmed_seat_ids     = 2;                                                                                                                                                                           
}

// ── ForceReleaseByBooking ─────────────────────────────────────────────────────                                                                                                                                       
message ForceReleaseByBookingRequest {
string booking_id  = 1;                                                                                                                                                                                               
string showtime_id = 2;
string reason      = 3;
}
   
---                                                                                                                                                                                                                     
payment.proto

// proto/payment/v1/payment.proto
syntax = "proto3";

package cinetix.payment.v1;

option java_package = "com.cinetix.grpc.payment.v1";                                                                                                                                                                    
option java_multiple_files = true;

import "google/protobuf/timestamp.proto";
import "common/v1/common.proto";

service PaymentService {                                                                                                                                                                                                
// [SAGA STEP 3] T?o payment record + call PSP → l?y paymentUrl
rpc InitiatePayment (InitiatePaymentRequest) returns (InitiatePaymentResponse);

    // [SAGA COMPENSATION] Request refund khi booking cancelled sau payment                                                                                                                                               
    rpc RefundPayment   (RefundPaymentRequest)   returns (RefundPaymentResponse);                                                                                                                                         
                                                                                                                                                                                                                          
    // Query payment status (d?ng khi saga c?n reconcile sau crash)                                                                                                                                                       
    rpc GetPaymentStatus (GetPaymentStatusRequest) returns (GetPaymentStatusResponse);                                                                                                                                    
                                                                                                                                                                                                                          
    // Cancel pending payment (tr??c khi user pay) ― d?ng khi booking timeout                                                                                                                                             
    rpc CancelPayment   (CancelPaymentRequest)   returns (CancelPaymentResponse);
}

// ── InitiatePayment ───────────────────────────────────────────────────────────                                                                                                                                       
message InitiatePaymentRequest {
string   booking_id      = 1;
string   customer_id     = 2;                                                                                                                                                                                         
cinetix.common.v1.Money amount = 3;
string   method          = 4;  // VNPAY | MOMO | CREDIT_CARD                                                                                                                                                          
string   idempotency_key = 5;  // bookingId + customerId hash ― CRITICAL                                                                                                                                              
string   return_url      = 6;  // redirect sau khi PSP x? l?                                                                                                                                                          
string   ip_address      = 7;  // c?n cho VNPay signature                                                                                                                                                             
string   description     = 8;  // "Dat ve xem phim - BookingXXX"                                                                                                                                                      
}

message InitiatePaymentResponse {                                                                                                                                                                                       
oneof result {
InitiatePaymentSuccess success = 1;                                                                                                                                                                                 
cinetix.common.v1.Error error  = 2;
}                                                                                                                                                                                                                     
}

message InitiatePaymentSuccess {
string   payment_id     = 1;
string   payment_url    = 2;   // redirect user sang ??y                                                                                                                                                              
string   psp_provider   = 3;   // VNPAY | MOMO                                                                                                                                                                        
google.protobuf.Timestamp expires_at = 4;  // payment window (15 ph?t PSP)                                                                                                                                            
}

// ── RefundPayment ─────────────────────────────────────────────────────────────                                                                                                                                       
message RefundPaymentRequest {
string   payment_id      = 1;                                                                                                                                                                                         
string   booking_id      = 2;
cinetix.common.v1.Money refund_amount = 3;  // null = full refund
string   reason          = 4;  // BOOKING_CANCELLED | SHOWTIME_CANCELLED | DISPUTE                                                                                                                                    
string   idempotency_key = 5;  // paymentId + reason hash                                                                                                                                                             
string   requested_by    = 6;  // customerId ho?c "SYSTEM" (saga compensation)                                                                                                                                        
}

message RefundPaymentResponse {                                                                                                                                                                                         
oneof result {
RefundPaymentSuccess success = 1;
cinetix.common.v1.Error error = 2;
}
}

message RefundPaymentSuccess {                                                                                                                                                                                          
string   refund_id           = 1;
string   status              = 2;  // REQUESTED | PROCESSING (PSP async)                                                                                                                                              
cinetix.common.v1.Money refund_amount = 3;                                                                                                                                                                            
google.protobuf.Timestamp estimated_completion = 4;  // PSP SLA
}

// ── GetPaymentStatus ──────────────────────────────────────────────────────────                                                                                                                                       
message GetPaymentStatusRequest {
string payment_id  = 1;
string booking_id  = 2;  // alternative lookup                                                                                                                                                                        
}

message GetPaymentStatusResponse {
string   payment_id   = 1;
string   booking_id   = 2;                                                                                                                                                                                            
string   status       = 3;  // PENDING|PROCESSING|COMPLETED|FAILED|REFUNDED
cinetix.common.v1.Money amount = 4;                                                                                                                                                                                   
string   method       = 5;
google.protobuf.Timestamp completed_at = 6;                                                                                                                                                                           
google.protobuf.Timestamp failed_at    = 7;
string   failure_reason = 8;                                                                                                                                                                                          
}

// ── CancelPayment ─────────────────────────────────────────────────────────────                                                                                                                                       
message CancelPaymentRequest {
string payment_id = 1;                                                                                                                                                                                                
string reason     = 2;
}

message CancelPaymentResponse {
bool   success = 1;
string status  = 2;  // CANCELLED                                                                                                                                                                                     
cinetix.common.v1.Error error = 3;
}
                                                                                                                                                                                                                          
---
promotion.proto

// proto/promotion/v1/promotion.proto
syntax = "proto3";

package cinetix.promotion.v1;

option java_package = "com.cinetix.grpc.promotion.v1";                                                                                                                                                                  
option java_multiple_files = true;

import "google/protobuf/timestamp.proto";
import "common/v1/common.proto";

service PromotionService {
// [SAGA STEP 2] Validate voucher + t?nh discount (ch?a mark used)
rpc ValidateVoucher (ValidateVoucherRequest) returns (ValidateVoucherResponse);

    // [SAGA STEP 5] Mark voucher as redeemed (sau payment + seat confirm)                                                                                                                                                
    rpc RedeemVoucher   (RedeemVoucherRequest)   returns (RedeemVoucherResponse);
                                                                                                                                                                                                                          
    // [SAGA COMPENSATION] Undo redeem n?u saga fail sau b??c n?y                                                                                                                                                         
    rpc VoidVoucher     (VoidVoucherRequest)     returns (VoidVoucherResponse);
                                                                                                                                                                                                                          
    // Query public promotions (d?ng cho browse UI)
    rpc ListActivePromotions (ListActivePromotionsRequest) returns (ListActivePromotionsResponse);                                                                                                                        
}

// ── ValidateVoucher ───────────────────────────────────────────────────────────                                                                                                                                       
message ValidateVoucherRequest {
string   code          = 1;
string   customer_id   = 2;                                                                                                                                                                                           
cinetix.common.v1.Money order_amount = 3;
string   showtime_id   = 4;  // ki?m tra applicable_movie / cinema                                                                                                                                                    
string   movie_id      = 5;                                                                                                                                                                                           
string   cinema_id     = 6;
google.protobuf.Timestamp showtime_date = 7;  // ki?m tra applicable_days                                                                                                                                             
}

message ValidateVoucherResponse {                                                                                                                                                                                       
oneof result {
ValidateVoucherSuccess success = 1;
cinetix.common.v1.Error error  = 2;
}                                                                                                                                                                                                                     
}

message ValidateVoucherSuccess {
string   voucher_id      = 1;
string   promotion_id    = 2;
string   promotion_name  = 3;                                                                                                                                                                                         
string   discount_type   = 4;  // PERCENTAGE | FIXED_AMOUNT
int32    discount_percent = 5;  // n?u PERCENTAGE                                                                                                                                                                     
cinetix.common.v1.Money discount_amount = 6;  // s? ti?n ???c gi?m                                                                                                                                                    
cinetix.common.v1.Money final_amount    = 7;  // sau khi ?p d?ng
google.protobuf.Timestamp valid_until   = 8;  // voucher h?t h?n khi n?o                                                                                                                                              
}

// ── RedeemVoucher ─────────────────────────────────────────────────────────────                                                                                                                                       
message RedeemVoucherRequest {
string   code            = 1;                                                                                                                                                                                         
string   voucher_id      = 2;   // t? ValidateVoucherResponse
string   customer_id     = 3;                                                                                                                                                                                         
string   booking_id      = 4;
cinetix.common.v1.Money discount_applied = 5;                                                                                                                                                                         
string   idempotency_key = 6;   // bookingId + voucherId                                                                                                                                                              
}

message RedeemVoucherResponse {                                                                                                                                                                                         
oneof result {
RedeemVoucherSuccess success = 1;
cinetix.common.v1.Error error = 2;
}
}

message RedeemVoucherSuccess {                                                                                                                                                                                          
string   redemption_id  = 1;
google.protobuf.Timestamp redeemed_at = 2;                                                                                                                                                                            
}

// ── VoidVoucher ───────────────────────────────────────────────────────────────                                                                                                                                       
message VoidVoucherRequest {
string   voucher_id      = 1;                                                                                                                                                                                         
string   booking_id      = 2;
string   reason          = 3;  // BOOKING_CANCELLED | PAYMENT_FAILED
string   idempotency_key = 4;                                                                                                                                                                                         
}

message VoidVoucherResponse {                                                                                                                                                                                           
bool    success   = 1;
string  status    = 2;   // VOIDED | NOT_FOUND | ALREADY_VOIDED                                                                                                                                                       
cinetix.common.v1.Error error = 3;                                                                                                                                                                                    
}

// ── ListActivePromotions ──────────────────────────────────────────────────────
message ListActivePromotionsRequest {
string   cinema_id  = 1;  // optional
string   movie_id   = 2;  // optional                                                                                                                                                                                 
string   date       = 3;  // "2026-04-22"
}

message ListActivePromotionsResponse {
repeated PromotionSummary promotions = 1;                                                                                                                                                                             
}

message PromotionSummary {
string   id              = 1;
string   name            = 2;                                                                                                                                                                                         
string   description     = 3;
string   discount_type   = 4;                                                                                                                                                                                         
int32    discount_percent = 5;
cinetix.common.v1.Money max_discount = 6;
cinetix.common.v1.Money min_order    = 7;                                                                                                                                                                             
google.protobuf.Timestamp ends_at    = 8;
}
                  
---                                                                                                                                                                                                                     
5.2 Event Schema ― RabbitMQ (JSON Schema)

Exchange & Routing Key Convention:

Exchange   : cinetix.events (topic)
Routing Key: {service}.events.{event_name}.{version}

Examples:                                                                                                                                                                                                               
booking.events.initiated.v1                                                                                                                                                                                           
booking.events.confirmed.v1                                                                                                                                                                                           
booking.events.cancelled.v1
booking.events.expired.v1                                                                                                                                                                                             
payment.events.initiated.v1
payment.events.completed.v1
payment.events.failed.v1
showtime.events.seats_held.v1                                                                                                                                                                                         
showtime.events.seats_released.v1
showtime.events.seats_confirmed.v1                                                                                                                                                                                    
ticket.events.issued.v1                                                                                                                                                                                               
notification.events.requested.v1

Base Event Envelope (t?t c? events ??u c?):

// Envelope ?p d?ng cho M?I event ― kh?ng thay ??i gi?a versions                                                                                                                                                        
{                                                                                                                                                                                                                       
"$schema": "https://json-schema.org/draft/2020-12",
"title": "BaseEvent",                                                                                                                                                                                                 
"type": "object",
"required": ["eventId", "eventType", "version", "occurredAt",                                                                                                                                                         
"aggregateType", "aggregateId", "payload"],                                                                                                                                                              
"properties": {
"eventId":       { "type": "string", "format": "uuid" },                                                                                                                                                            
"eventType":     { "type": "string" },                                                                                                                                                                              
"version":       { "type": "string", "enum": ["v1", "v2"] },
"occurredAt":    { "type": "string", "format": "date-time" },                                                                                                                                                       
"aggregateType": { "type": "string" },
"aggregateId":   { "type": "string", "format": "uuid" },                                                                                                                                                            
"correlationId": { "type": "string", "format": "uuid" },                                                                                                                                                            
"causationId":   { "type": "string", "format": "uuid" },
"traceId":       { "type": "string" },                                                                                                                                                                              
"spanId":        { "type": "string" },
"payload":       { "type": "object" }                                                                                                                                                                               
}                                                                                                                                                                                                                     
}

Booking Events:

// ── BookingInitiatedEvent ― booking.events.initiated.v1
{                                                                                                                                                                                                                       
"eventId": "550e8400-e29b-41d4-a716-446655440000",
"eventType": "booking.events.initiated.v1",                                                                                                                                                                           
"version": "v1",                                                                                                                                                                                                      
"occurredAt": "2026-04-22T10:30:00Z",
"aggregateType": "Booking",                                                                                                                                                                                           
"aggregateId": "booking-uuid",                                                                                                                                                                                        
"correlationId": "booking-uuid",
"traceId": "abc123",                                                                                                                                                                                                  
"payload": {  
"bookingId":   "booking-uuid",                                                                                                                                                                                      
"customerId":  "customer-uuid",
"showtimeId":  "showtime-uuid",                                                                                                                                                                                     
"cinemaId":    "cinema-uuid",                                                                                                                                                                                       
"movieId":     "movie-uuid",
"seats": [                                                                                                                                                                                                          
{ "seatId": "seat-uuid-1", "seatCode": "A1", "category": "VIP",      "price": 120000 },
{ "seatId": "seat-uuid-2", "seatCode": "A2", "category": "VIP",      "price": 120000 }                                                                                                                            
],                                                                                                                                                                                                                  
"subtotal":    240000,                                                                                                                                                                                              
"currency":    "VND",                                                                                                                                                                                               
"expiresAt":   "2026-04-22T10:40:00Z"
}                                                                                                                                                                                                                     
}

// ── BookingConfirmedEvent ― booking.events.confirmed.v1
{                                                                                                                                                                                                                       
"eventId": "...",
"eventType": "booking.events.confirmed.v1",
"version": "v1",
"occurredAt": "2026-04-22T10:33:00Z",
"aggregateType": "Booking",                                                                                                                                                                                           
"aggregateId": "booking-uuid",
"payload": {                                                                                                                                                                                                          
"bookingId":      "booking-uuid",
"customerId":     "customer-uuid",                                                                                                                                                                                  
"showtimeId":     "showtime-uuid",
"cinemaId":       "cinema-uuid",                                                                                                                                                                                    
"movieId":        "movie-uuid",
"movieTitle":     "Avengers: Endgame",
"cinemaName":     "CGV Vincom Center",                                                                                                                                                                              
"screenName":     "Ph?ng 3",                                                                                                                                                                                        
"showtimeStart":  "2026-04-23T19:30:00Z",                                                                                                                                                                           
"seats": [                                                                                                                                                                                                          
{ "seatId": "seat-uuid-1", "seatCode": "A1", "category": "VIP", "price": 120000 },
{ "seatId": "seat-uuid-2", "seatCode": "A2", "category": "VIP", "price": 120000 }                                                                                                                                 
],          
"subtotal":       240000,                                                                                                                                                                                           
"discountAmount": 24000,                                                                                                                                                                                            
"finalAmount":    216000,
"currency":       "VND",                                                                                                                                                                                            
"voucherCode":    "SAVE10",
"paymentMethod":  "VNPAY",                                                                                                                                                                                          
"confirmedAt":    "2026-04-22T10:33:00Z",
"bookingItems": [                                                                                                                                                                                                   
{ "bookingItemId": "item-uuid-1", "seatId": "seat-uuid-1" },                                                                                                                                                      
{ "bookingItemId": "item-uuid-2", "seatId": "seat-uuid-2" }
]                                                                                                                                                                                                                   
}             
}

// ── BookingCancelledEvent ― booking.events.cancelled.v1
{
"eventId": "...",
"eventType": "booking.events.cancelled.v1",
"version": "v1",                                                                                                                                                                                                      
"occurredAt": "2026-04-22T10:40:00Z",
"aggregateType": "Booking",                                                                                                                                                                                           
"aggregateId": "booking-uuid",
"payload": {
"bookingId":     "booking-uuid",                                                                                                                                                                                    
"customerId":    "customer-uuid",
"showtimeId":    "showtime-uuid",                                                                                                                                                                                   
"reason":        "PAYMENT_FAILED",
"cancelledAt":   "2026-04-22T10:40:00Z",                                                                                                                                                                            
"refundAmount":  0,
"refundStatus":  "NOT_REQUIRED"                                                                                                                                                                                     
}                                                                                                                                                                                                                     
}

// ── BookingExpiredEvent ― booking.events.expired.v1
{
"eventId": "...",
"eventType": "booking.events.expired.v1",
"version": "v1",                                                                                                                                                                                                      
"occurredAt": "2026-04-22T10:40:00Z",
"aggregateType": "Booking",

──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
?? 
──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
esc to interrupt
"aggregateType": "Booking",
"aggregateId": "booking-uuid",
"payload": {
"bookingId":     "booking-uuid",
"customerId":    "customer-uuid",
"showtimeId":    "showtime-uuid",
"reason":        "PAYMENT_FAILED",
"cancelledAt":   "2026-04-22T10:40:00Z",
"refundAmount":  0,
"refundStatus":  "NOT_REQUIRED"
}
}

// ── BookingExpiredEvent ― booking.events.expired.v1
{
"eventId": "...",
"eventType": "booking.events.expired.v1",
"version": "v1",
"occurredAt": "2026-04-22T10:40:00Z",
"aggregateType": "Booking",
"aggregateId": "booking-uuid",
"payload": {                                                                                                                                                                                                          
"bookingId":   "booking-uuid",
"customerId":  "customer-uuid",                                                                                                                                                                                     
"showtimeId":  "showtime-uuid",
"expiredAt":   "2026-04-22T10:40:00Z"
}                                                                                                                                                                                                                     
}

Payment Events:

// ── PaymentInitiatedEvent ― payment.events.initiated.v1
{
"eventType": "payment.events.initiated.v1",
"aggregateType": "Payment",                                                                                                                                                                                           
"aggregateId": "payment-uuid",
"payload": {                                                                                                                                                                                                          
"paymentId":   "payment-uuid",
"bookingId":   "booking-uuid",
"customerId":  "customer-uuid",                                                                                                                                                                                     
"amount":      216000,
"currency":    "VND",                                                                                                                                                                                               
"method":      "VNPAY",
"paymentUrl":  "https://sandbox.vnpayment.vn/paymentv2/...",
"pspProvider": "VNPAY",                                                                                                                                                                                             
"expiresAt":   "2026-04-22T10:45:00Z",
"initiatedAt": "2026-04-22T10:30:00Z"                                                                                                                                                                               
}                                                                                                                                                                                                                     
}

// ── PaymentCompletedEvent ― payment.events.completed.v1
{
"eventType": "payment.events.completed.v1",
"aggregateType": "Payment",
"aggregateId": "payment-uuid",                                                                                                                                                                                        
"payload": {
"paymentId":        "payment-uuid",                                                                                                                                                                                 
"bookingId":        "booking-uuid",
"customerId":       "customer-uuid",
"amount":           216000,
"currency":         "VND",                                                                                                                                                                                          
"method":           "VNPAY",
"pspTransactionId": "VNP_TXN_123456789",                                                                                                                                                                            
"pspProvider":      "VNPAY",                                                                                                                                                                                        
"completedAt":      "2026-04-22T10:33:00Z"                                                                                                                                                                          
}                                                                                                                                                                                                                     
}

// ── PaymentFailedEvent ― payment.events.failed.v1
{
"eventType": "payment.events.failed.v1",
"aggregateType": "Payment",                                                                                                                                                                                           
"aggregateId": "payment-uuid",
"payload": {                                                                                                                                                                                                          
"paymentId":   "payment-uuid",
"bookingId":   "booking-uuid",
"customerId":  "customer-uuid",                                                                                                                                                                                     
"amount":      216000,
"method":      "VNPAY",                                                                                                                                                                                             
"reason":      "INSUFFICIENT_FUNDS",
"pspErrorCode": "24",
"failedAt":    "2026-04-22T10:35:00Z"                                                                                                                                                                               
}
}

Showtime Events:

// ── SeatsHeldEvent ― showtime.events.seats_held.v1                                                                                                                                                                    
{               
"eventType": "showtime.events.seats_held.v1",
"aggregateType": "Showtime",
"aggregateId": "showtime-uuid",                                                                                                                                                                                       
"payload": {
"showtimeId":  "showtime-uuid",                                                                                                                                                                                     
"bookingId":   "booking-uuid",
"seats": [
{ "seatId": "seat-uuid-1", "seatCode": "A1", "category": "VIP" },                                                                                                                                                 
{ "seatId": "seat-uuid-2", "seatCode": "A2", "category": "VIP" }
],                                                                                                                                                                                                                  
"expiresAt":   "2026-04-22T10:40:00Z",
"heldAt":      "2026-04-22T10:30:00Z"                                                                                                                                                                               
}                                                                                                                                                                                                                     
}

// ── SeatsReleasedEvent ― showtime.events.seats_released.v1
{
"eventType": "showtime.events.seats_released.v1",
"aggregateType": "Showtime",
"aggregateId": "showtime-uuid",                                                                                                                                                                                       
"payload": {
"showtimeId":  "showtime-uuid",                                                                                                                                                                                     
"bookingId":   "booking-uuid",
"seatIds":     ["seat-uuid-1", "seat-uuid-2"],
"seatCodes":   ["A1", "A2"],                                                                                                                                                                                        
"reason":      "PAYMENT_FAILED",
"releasedAt":  "2026-04-22T10:35:00Z"                                                                                                                                                                               
}                                                                                                                                                                                                                     
}

// ── SeatsConfirmedEvent ― showtime.events.seats_confirmed.v1
{
"eventType": "showtime.events.seats_confirmed.v1",
"aggregateType": "Showtime",                                                                                                                                                                                          
"aggregateId": "showtime-uuid",
"payload": {                                                                                                                                                                                                          
"showtimeId":  "showtime-uuid",
"bookingId":   "booking-uuid",
"seatIds":     ["seat-uuid-1", "seat-uuid-2"],                                                                                                                                                                      
"seatCodes":   ["A1", "A2"],
"confirmedAt": "2026-04-22T10:33:00Z"                                                                                                                                                                               
}             
}

Ticket & Notification Events:

// ── TicketIssuedEvent ― ticket.events.issued.v1
{
"eventType": "ticket.events.issued.v1",
"aggregateType": "Ticket",
"aggregateId": "ticket-uuid-1",                                                                                                                                                                                       
"payload": {
"bookingId":  "booking-uuid",                                                                                                                                                                                       
"customerId": "customer-uuid",
"tickets": [                                                                                                                                                                                                        
{
"ticketId":      "ticket-uuid-1",                                                                                                                                                                               
"bookingItemId": "item-uuid-1",
"seatCode":      "A1",
"category":      "VIP",                                                                                                                                                                                         
"qrToken":       "eyJhbGc...",
"qrUrl":         "https://cdn.cinetix.vn/qr/ticket-uuid-1.png"                                                                                                                                                  
},                                                                                                                                                                                                                
{
"ticketId":      "ticket-uuid-2",                                                                                                                                                                               
"bookingItemId": "item-uuid-2",                                                                                                                                                                                 
"seatCode":      "A2",
"category":      "VIP",                                                                                                                                                                                         
"qrToken":       "eyJhbGc...",
"qrUrl":         "https://cdn.cinetix.vn/qr/ticket-uuid-2.png"                                                                                                                                                  
}
],                                                                                                                                                                                                                  
"movieTitle":    "Avengers: Endgame",
"cinemaName":    "CGV Vincom Center",                                                                                                                                                                               
"screenName":    "Ph?ng 3",
"showtimeStart": "2026-04-23T19:30:00Z",                                                                                                                                                                            
"issuedAt":      "2026-04-22T10:33:30Z"                                                                                                                                                                             
}
}

// ── NotificationRequestedEvent ― notification.events.requested.v1                                                                                                                                                     
{
"eventType": "notification.events.requested.v1",                                                                                                                                                                      
"aggregateType": "Notification",
"aggregateId": "notif-request-uuid",
"payload": {
"recipientId":    "customer-uuid",
"recipientEmail": "user@example.com",                                                                                                                                                                               
"fcmToken":       "fcm-device-token",
"channels":       ["EMAIL", "PUSH"],                                                                                                                                                                                
"templateName":   "TICKET_ISSUED",
"templateVars": {                                                                                                                                                                                                   
"customerName":  "Nguy?n V?n A",                                                                                                                                                                                  
"movieTitle":    "Avengers: Endgame",
"cinemaName":    "CGV Vincom Center",                                                                                                                                                                             
"screenName":    "Ph?ng 3",                                                                                                                                                                                       
"showtimeStart": "19:30 - 23/04/2026",
"seatCodes":     "A1, A2",                                                                                                                                                                                        
"totalAmount":   "216,000 VND",
"qrUrls": [                                                                                                                                                                                                       
"https://cdn.cinetix.vn/qr/ticket-uuid-1.png",
"https://cdn.cinetix.vn/qr/ticket-uuid-2.png"                                                                                                                                                                   
]         
},                                                                                                                                                                                                                  
"idempotencyKey": "booking-uuid-TICKET_ISSUED-EMAIL"
}                                                                                                                                                                                                                     
}
                                                                                                                                                                                                                          
---             
5.3 REST API ― OpenAPI 3.1 (API Gateway)

# openapi/cinetix-api.yaml
openapi: "3.1.0"                                                                                                                                                                                                        
info:                                                                                                                                                                                                                   
title: CinemaTix API
version: "1.0.0"                                                                                                                                                                                                      
description: |
Public REST API exposed qua Spring Cloud Gateway.
Authentication: Bearer JWT (RS256) t? Keycloak.                                                                                                                                                                     
Rate Limit: 100 req/min per user, 10 req/min cho booking endpoints.

servers:                                                                                                                                                                                                                
- url: https://api.cinetix.vn/v1                                                                                                                                                                                      
description: Production                                                                                                                                                                                             
- url: http://localhost:8080/v1
description: Local development

security:
- BearerAuth: []

# ???????????????????????????????????????????????????????????????????????????????
paths:

    # ── MOVIES ─────────────────────────────────────────────────────────────────
    /movies:                                                                                                                                                                                                              
      get:        
        summary: L?y danh s?ch phim
        operationId: listMovies                                                                                                                                                                                           
        tags: [Movies]
        security: []  # public endpoint                                                                                                                                                                                   
        parameters:                                                                                                                                                                                                       
          - name: status
            in: query                                                                                                                                                                                                     
            schema:
              type: string
              enum: [COMING_SOON, NOW_SHOWING]
          - name: genreId                                                                                                                                                                                                 
            in: query
            schema: { type: string, format: uuid }                                                                                                                                                                        
          - name: search
            in: query                                                                                                                                                                                                     
            schema: { type: string, maxLength: 100 }
          - $ref: '#/components/parameters/PageParam'                                                                                                                                                                     
          - $ref: '#/components/parameters/SizeParam'                                                                                                                                                                     
        responses:
          '200':                                                                                                                                                                                                          
            description: Danh s?ch phim
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/MovieListResponse'
                                                                                                                                                                                                                          
    /movies/{movieId}:
      get:                                                                                                                                                                                                                
        summary: Chi ti?t phim
        operationId: getMovie
        tags: [Movies]
        security: []
        parameters:                                                                                                                                                                                                       
          - $ref: '#/components/parameters/MovieId'
        responses:                                                                                                                                                                                                        
          '200':  
            content:
              application/json:
                schema: { $ref: '#/components/schemas/MovieDetail' }
          '404':                                                                                                                                                                                                          
            $ref: '#/components/responses/NotFound'
                                                                                                                                                                                                                          
    /movies/{movieId}/showtimes:
      get:
        summary: L?ch chi?u c?a phim                                                                                                                                                                                      
        operationId: getMovieShowtimes
        tags: [Movies, Showtimes]                                                                                                                                                                                         
        security: []
        parameters:                                                                                                                                                                                                       
          - $ref: '#/components/parameters/MovieId'
          - name: date                                                                                                                                                                                                    
            in: query
            required: true
            schema: { type: string, format: date, example: "2026-04-22" }
          - name: cinemaId                                                                                                                                                                                                
            in: query
            schema: { type: string, format: uuid }                                                                                                                                                                        
          - name: city
            in: query                                                                                                                                                                                                     
            schema: { type: string, example: "H? N?i" }
        responses:                                                                                                                                                                                                        
          '200':  
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/ShowtimeListResponse'
                                                                                                                                                                                                                          
    # ── CINEMAS ─────────────────────────────────────────────────────────────────
    /cinemas:                                                                                                                                                                                                             
      get:        
        summary: Danh s?ch r?p
        operationId: listCinemas                                                                                                                                                                                          
        tags: [Cinemas]
        security: []                                                                                                                                                                                                      
        parameters:
          - name: city
            in: query
            schema: { type: string }
          - name: lat
            in: query                                                                                                                                                                                                     
            schema: { type: number, format: double }
          - name: lng                                                                                                                                                                                                     
            in: query
            schema: { type: number, format: double }
          - name: radius
            in: query
            description: B?n k?nh t?m ki?m (km)
            schema: { type: integer, default: 10 }                                                                                                                                                                        
        responses:                                                                                                                                                                                                        
          '200':                                                                                                                                                                                                          
            content:                                                                                                                                                                                                      
              application/json:
                schema:
                  $ref: '#/components/schemas/CinemaListResponse'

    # ── SHOWTIMES ───────────────────────────────────────────────────────────────                                                                                                                                        
    /showtimes/{showtimeId}:
      get:                                                                                                                                                                                                                
        summary: Chi ti?t su?t chi?u + s? ?? gh?
        operationId: getShowtime                                                                                                                                                                                          
        tags: [Showtimes]                                                                                                                                                                                                 
        security: []                                                                                                                                                                                                      
        parameters:                                                                                                                                                                                                       
          - $ref: '#/components/parameters/ShowtimeId'
        responses:
          '200':
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/ShowtimeWithSeatMap'                                                                                                                                                        
   
    # ── BOOKINGS ────────────────────────────────────────────────────────────────                                                                                                                                        
    /bookings:    
      post:                                                                                                                                                                                                               
        summary: T?o booking m?i (b?t ??u Saga)
        operationId: createBooking
        tags: [Bookings]                                                                                                                                                                                                  
        x-rateLimit: "5/minute per user"
        requestBody:                                                                                                                                                                                                      
          required: true
          content:                                                                                                                                                                                                        
            application/json:
              schema:
                $ref: '#/components/schemas/CreateBookingRequest'
        responses:
          '202':
            description: Booking ?? ???c kh?i t?o, ?ang x? l?                                                                                                                                                             
            content:
              application/json:                                                                                                                                                                                           
                schema:
                  $ref: '#/components/schemas/CreateBookingResponse'                                                                                                                                                      
          '400':
            $ref: '#/components/responses/BadRequest'                                                                                                                                                                     
          '409':  
            description: Gh? ?? b? ??t b?i ng??i kh?c
            content:                                                                                                                                                                                                      
              application/json:
                schema:                                                                                                                                                                                                   
                  $ref: '#/components/schemas/SeatsUnavailableError'
                                                                                                                                                                                                                          
      get:
        summary: L?ch s? ??t v? c?a t?i                                                                                                                                                                                   
        operationId: listMyBookings                                                                                                                                                                                       
        tags: [Bookings]
        parameters:                                                                                                                                                                                                       
          - name: status                                                                                                                                                                                                  
            in: query
            schema:                                                                                                                                                                                                       
              type: string
              enum: [CONFIRMED, CANCELLED, PENDING]
          - $ref: '#/components/parameters/PageParam'
          - $ref: '#/components/parameters/SizeParam'                                                                                                                                                                     
        responses:
          '200':                                                                                                                                                                                                          
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/BookingListResponse'
                                                                                                                                                                                                                          
    /bookings/{bookingId}:
      get:                                                                                                                                                                                                                
        summary: Chi ti?t booking
        operationId: getBooking
        tags: [Bookings]
        parameters:
          - $ref: '#/components/parameters/BookingId'
        responses:                                                                                                                                                                                                        
          '200':
            content:                                                                                                                                                                                                      
              application/json:
                schema:
                  $ref: '#/components/schemas/BookingDetail'
          '403':
            $ref: '#/components/responses/Forbidden'
          '404':                                                                                                                                                                                                          
            $ref: '#/components/responses/NotFound'
                                                                                                                                                                                                                          
      delete:     
        summary: Hu? booking
        operationId: cancelBooking
        tags: [Bookings]
        parameters:                                                                                                                                                                                                       
          - $ref: '#/components/parameters/BookingId'
        requestBody:                                                                                                                                                                                                      
          content:
            application/json:
              schema:
                type: object
                properties:
                  reason: { type: string, maxLength: 500 }
        responses:                                                                                                                                                                                                        
          '200':
            content:                                                                                                                                                                                                      
              application/json:
                schema:
                  $ref: '#/components/schemas/BookingCancelResponse'
          '409':
            description: Kh?ng th? hu? booking ? tr?ng th?i hi?n t?i                                                                                                                                                      
   
    /bookings/{bookingId}/apply-voucher:                                                                                                                                                                                  
      post:       
        summary: ?p d?ng voucher v?o booking                                                                                                                                                                              
        operationId: applyVoucher
        tags: [Bookings]
        parameters:                                                                                                                                                                                                       
          - $ref: '#/components/parameters/BookingId'
        requestBody:                                                                                                                                                                                                      
          required: true
          content:
            application/json:
              schema:
                type: object
                required: [voucherCode]
                properties:                                                                                                                                                                                               
                  voucherCode:
                    type: string                                                                                                                                                                                          
                    pattern: '^[A-Z0-9]{4,20}$'
        responses:                                                                                                                                                                                                        
          '200':
            content:                                                                                                                                                                                                      
              application/json:
                schema:
                  $ref: '#/components/schemas/ApplyVoucherResponse'
          '422':
            description: Voucher kh?ng h?p l? / ?? d?ng / h?t h?n                                                                                                                                                         
   
    # ── TICKETS ─────────────────────────────────────────────────────────────────                                                                                                                                        
    /tickets:     
      get:                                                                                                                                                                                                                
        summary: V? c?a t?i
        operationId: listMyTickets
        tags: [Tickets]                                                                                                                                                                                                   
        parameters:
          - name: upcoming                                                                                                                                                                                                
            in: query
            schema: { type: boolean }
        responses:                                                                                                                                                                                                        
          '200':
            content:                                                                                                                                                                                                      
              application/json:
                schema:
                  $ref: '#/components/schemas/TicketListResponse'

    /tickets/{ticketId}/validate:                                                                                                                                                                                         
      post:
        summary: Validate QR t?i r?p (staff only)                                                                                                                                                                         
        operationId: validateTicket                                                                                                                                                                                       
        tags: [Tickets]
        x-requiredRole: STAFF                                                                                                                                                                                             
        parameters:
          - name: ticketId
            in: path                                                                                                                                                                                                      
            required: true
            schema: { type: string, format: uuid }                                                                                                                                                                        
        requestBody:
          required: true
          content:
            application/json:
              schema:
                type: object
                required: [qrToken]
                properties:                                                                                                                                                                                               
                  qrToken: { type: string }
        responses:                                                                                                                                                                                                        
          '200':  
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/TicketValidationResult'
          '409':                                                                                                                                                                                                          
            description: V? ?? ???c s? d?ng
                                                                                                                                                                                                                          
    # ── PROMOTIONS ───────────────────────────────────────────────────────────────                                                                                                                                       
    /promotions:
      get:                                                                                                                                                                                                                
        summary: Danh s?ch khuy?n m?i ?ang ho?t ??ng
        operationId: listPromotions
        tags: [Promotions]                                                                                                                                                                                                
        security: []
        responses:                                                                                                                                                                                                        
          '200':  
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/PromotionListResponse'

    /promotions/validate:                                                                                                                                                                                                 
      post:
        summary: Ki?m tra voucher code                                                                                                                                                                                    
        operationId: validateVoucherPublic
        tags: [Promotions]                                                                                                                                                                                                
        requestBody:
          required: true                                                                                                                                                                                                  
          content:
            application/json:
              schema:
                type: object
                required: [code, showtimeId, seatCount]
                properties:                                                                                                                                                                                               
                  code:        { type: string }
                  showtimeId:  { type: string, format: uuid }                                                                                                                                                             
                  seatCount:   { type: integer, minimum: 1 }                                                                                                                                                              
        responses:
          '200':                                                                                                                                                                                                          
            content:
              application/json:
                schema:
                  $ref: '#/components/schemas/VoucherValidationResponse'

# ???????????????????????????????????????????????????????????????????????????????
components:                                                                                                                                                                                                             
securitySchemes:
BearerAuth:                                                                                                                                                                                                         
type: http
scheme: bearer                                                                                                                                                                                                    
bearerFormat: JWT

    parameters:
      MovieId:
        name: movieId
        in: path
        required: true
        schema: { type: string, format: uuid }
      ShowtimeId:                                                                                                                                                                                                         
        name: showtimeId
        in: path                                                                                                                                                                                                          
        required: true
        schema: { type: string, format: uuid }
      BookingId:
        name: bookingId
        in: path
        required: true                                                                                                                                                                                                    
        schema: { type: string, format: uuid }
      PageParam:                                                                                                                                                                                                          
        name: page
        in: query
        schema: { type: integer, default: 0, minimum: 0 }
      SizeParam:
        name: size                                                                                                                                                                                                        
        in: query
        schema: { type: integer, default: 20, minimum: 1, maximum: 100 }                                                                                                                                                  
                  
    schemas:
      # ── Booking ───────────────────────────────────────────────────────────────
      CreateBookingRequest:                                                                                                                                                                                               
        type: object
        required: [showtimeId, seatIds, paymentMethod]                                                                                                                                                                    
        properties:                                                                                                                                                                                                       
          showtimeId:
            type: string                                                                                                                                                                                                  
            format: uuid
          seatIds:
            type: array
            items: { type: string, format: uuid }
            minItems: 1
            maxItems: 8
          paymentMethod:                                                                                                                                                                                                  
            type: string
            enum: [VNPAY, MOMO, CREDIT_CARD]                                                                                                                                                                              
          voucherCode:
            type: string
            pattern: '^[A-Z0-9]{4,20}$'
                                                                                                                                                                                                                          
      CreateBookingResponse:
        type: object                                                                                                                                                                                                      
        properties:
          bookingId:    { type: string, format: uuid }
          status:       { type: string, example: "PAYMENT_PENDING" }
          paymentUrl:   { type: string, format: uri }                                                                                                                                                                     
          finalAmount:  { type: integer, description: "VND" }
          expiresAt:    { type: string, format: date-time }                                                                                                                                                               
          seats:  
            type: array                                                                                                                                                                                                   
            items:
              type: object
              properties:
                seatId:   { type: string, format: uuid }
                seatCode: { type: string, example: "A1" }                                                                                                                                                                 
                price:    { type: integer }
                                                                                                                                                                                                                          
      BookingDetail:
        type: object
        properties:
          bookingId:     { type: string, format: uuid }
          status:        { type: string }                                                                                                                                                                                 
          movieTitle:    { type: string }
          moviePoster:   { type: string, format: uri }                                                                                                                                                                    
          cinemaName:    { type: string }                                                                                                                                                                                 
          screenName:    { type: string }
          showtimeStart: { type: string, format: date-time }                                                                                                                                                              
          seats:  
            type: array                                                                                                                                                                                                   
            items:
              $ref: '#/components/schemas/BookedSeat'                                                                                                                                                                     
          subtotal:      { type: integer }
          discountAmount:{ type: integer }
          finalAmount:   { type: integer }
          currency:      { type: string, example: "VND" }                                                                                                                                                                 
          voucherCode:   { type: string }
          paymentMethod: { type: string }                                                                                                                                                                                 
          qrCodes:                                                                                                                                                                                                        
            type: array
            items: { type: string, format: uri }                                                                                                                                                                          
          createdAt:     { type: string, format: date-time }
          confirmedAt:   { type: string, format: date-time }
                                                                                                                                                                                                                          
      BookedSeat:
        type: object                                                                                                                                                                                                      
        properties:
          seatId:    { type: string, format: uuid }
          seatCode:  { type: string }
          category:  { type: string, enum: [STANDARD, VIP, COUPLE] }                                                                                                                                                      
          price:     { type: integer }
                                                                                                                                                                                                                          
      SeatsUnavailableError:
        type: object                                                                                                                                                                                                      
        properties:
          code:           { type: string, example: "SEATS_UNAVAILABLE" }
          message:        { type: string }                                                                                                                                                                                
          unavailableSeats:
            type: array                                                                                                                                                                                                   
            items: { type: string }

      # ── Showtime + SeatMap ────────────────────────────────────────────────────                                                                                                                                        
      ShowtimeWithSeatMap:
        type: object                                                                                                                                                                                                      
        properties:
          showtimeId:     { type: string, format: uuid }
          movieTitle:     { type: string }                                                                                                                                                                                
          cinemaName:     { type: string }
          screenName:     { type: string }                                                                                                                                                                                
          startTime:      { type: string, format: date-time }
          format:         { type: string, enum: [2D, 3D, IMAX, 4DX] }
          availableSeats: { type: integer }                                                                                                                                                                               
          totalSeats:     { type: integer }                                                                                                                                                                               
          seatMap:                                                                                                                                                                                                        
            type: object                                                                                                                                                                                                  
            properties:
              rows:
                type: array
                items:
                  type: object                                                                                                                                                                                            
                  properties:
                    rowLabel: { type: string, example: "A" }                                                                                                                                                              
                    seats:
                      type: array                                                                                                                                                                                         
                      items:
                        $ref: '#/components/schemas/SeatInfo'                                                                                                                                                             
                  
      SeatInfo:
        type: object
        properties:
          seatId:    { type: string, format: uuid }
          seatCode:  { type: string, example: "A1" }                                                                                                                                                                      
          category:  { type: string, enum: [STANDARD, VIP, COUPLE, WHEELCHAIR] }
          status:    { type: string, enum: [AVAILABLE, HELD, CONFIRMED, UNAVAILABLE] }                                                                                                                                    
          price:     { type: integer, description: "VND" }
          positionX: { type: integer }                                                                                                                                                                                    
          positionY: { type: integer }
                                                                                                                                                                                                                          
      # ── Error responses ───────────────────────────────────────────────────────                                                                                                                                        
      ErrorResponse:
        type: object                                                                                                                                                                                                      
        required: [code, message, timestamp, traceId]
        properties:                                                                                                                                                                                                       
          code:      { type: string }
          message:   { type: string }                                                                                                                                                                                     
          details:   { type: object }
          timestamp: { type: string, format: date-time }
          traceId:   { type: string }                                                                                                                                                                                     
          path:      { type: string }
                                                                                                                                                                                                                          
    responses:    
      BadRequest:
        description: Request kh?ng h?p l?
        content:                                                                                                                                                                                                          
          application/json:
            schema: { $ref: '#/components/schemas/ErrorResponse' }                                                                                                                                                        
      Forbidden:  
        description: Kh?ng c? quy?n truy c?p
        content:                                                                                                                                                                                                          
          application/json:
            schema: { $ref: '#/components/schemas/ErrorResponse' }                                                                                                                                                        
      NotFound:   
        description: Kh?ng t?m th?y resource
        content:
          application/json:
            schema: { $ref: '#/components/schemas/ErrorResponse' }
                                                                                                                                                                                                                          
---
5.4 WebSocket Topics ― STOMP over SockJS

Endpoint & Protocol:

WebSocket URL : wss://api.cinetix.vn/ws
Handshake     : SockJS fallback → STOMP protocol                                                                                                                                                                        
Auth          : JWT token qua query param ?token=<jwt>                                                                                                                                                                  
ho?c STOMP CONNECT header: Authorization: Bearer <jwt>

Topic Definitions:

????????????????????????????????????????????????????????????????????????????????                                                                                                                                        
?                        WEBSOCKET TOPIC REFERENCE                           ?
????????????????????????????????????????????????????????????????????????????????                                                                                                                                        
?  PREFIX /topic/  → broadcast (nhi?u subscribers)                           ?
?  PREFIX /user/   → unicast (ch? user ?? nh?n)                              ?                                                                                                                                          
????????????????????????????????????????????????????????????????????????????????

TOPIC 1: Seat Map Real-Time                                                                                                                                                                                             
Destination : /topic/showtime/{showtimeId}/seats
Direction   : Server → All browsers ?ang xem showtime n?y                                                                                                                                                             
Auth        : Public (kh?ng c?n login)                                                                                                                                                                                
Trigger     : Khi c? seat HELD / RELEASED / CONFIRMED
Payload     :                                                                                                                                                                                                         
{           
"eventType": "SEAT_STATUS_CHANGED",                                                                                                                                                                               
"showtimeId": "uuid",
"changes": [                                                                                                                                                                                                      
{
"seatId":   "uuid",                                                                                                                                                                                           
"seatCode": "A1",
"status":   "HELD"       // AVAILABLE | HELD | CONFIRMED
}                                                                                                                                                                                                               
],
"availableCount": 45,                                                                                                                                                                                             
"timestamp": "2026-04-22T10:30:05Z"                                                                                                                                                                               
}

    Rate        : Max 50 msgs/second per showtime (batch multiple seat changes)                                                                                                                                           
    ─────────────────────────────────────────────────────────────────────────────

TOPIC 2: Booking Status Update (user-specific)                                                                                                                                                                          
Destination : /user/queue/booking/{bookingId}
Direction   : Server → Only the booking owner                                                                                                                                                                         
Auth        : Required (JWT validated ― Spring Security)                                                                                                                                                              
Trigger     : Khi booking status thay ??i
Payload     :                                                                                                                                                                                                         
{           
"eventType": "BOOKING_STATUS_CHANGED",                                                                                                                                                                            
"bookingId": "uuid",
"status":    "CONFIRMED",     // CONFIRMED | CANCELLED | PAYMENT_PENDING
"message":   "??t v? th?nh c?ng! V? c?a b?n ?? s?n s?ng.",                                                                                                                                                        
"data": {                                                                                                                                                                                                         
"paymentUrl":  null,        // c? gi? tr? khi status = PAYMENT_PENDING                                                                                                                                          
"ticketUrls":  ["https://cdn.../qr1.png"],  // khi CONFIRMED                                                                                                                                                    
"expiresAt":   null                                                                                                                                                                                             
},                                                                                                                                                                                                                
"timestamp": "2026-04-22T10:33:00Z"                                                                                                                                                                               
}

    ─────────────────────────────────────────────────────────────────────────────

TOPIC 3: Chat Message (user-specific)                                                                                                                                                                                   
Destination : /user/queue/chat/{conversationId}
Direction   : Bidirectional                                                                                                                                                                                           
Auth        : Required                                                                                                                                                                                                
Subscribe   : Server → User (nh?n tin nh?n m?i)
Send To     : /app/chat/{conversationId}/send (client → server)                                                                                                                                                       
Payload (receive):                                                                                                                                                                                                    
{                                                                                                                                                                                                                   
"eventType":      "NEW_MESSAGE",                                                                                                                                                                                  
"conversationId": "uuid",
"message": {                                                                                                                                                                                                      
"messageId":    "uuid",
"senderId":     "uuid",                                                                                                                                                                                         
"senderName":   "Support Agent",
"senderType":   "AGENT",
"content":      "Xin ch?o, t?i c? th? gi?p g? cho b?n?",                                                                                                                                                        
"contentType":  "TEXT",                                                                                                                                                                                         
"sentAt":       "2026-04-22T10:31:00Z"                                                                                                                                                                          
}                                                                                                                                                                                                                 
}

    Payload (send from client):                                                                                                                                                                                           
      {
        "content":     "T?i mu?n h?i v? booking #ABC123",                                                                                                                                                                 
        "contentType": "TEXT"                                                                                                                                                                                             
      }
                                                                                                                                                                                                                          
    ─────────────────────────────────────────────────────────────────────────────                                                                                                                                         

TOPIC 4: Agent Queue (staff-specific)                                                                                                                                                                                   
Destination : /user/queue/agent/conversations
Direction   : Server → Staff agent only                                                                                                                                                                               
Auth        : Required (ROLE_STAFF)
Trigger     : Khi c? conversation m?i / ???c assign                                                                                                                                                                   
Payload     :                                                                                                                                                                                                         
{
"eventType":      "CONVERSATION_ASSIGNED",                                                                                                                                                                        
"conversationId": "uuid",
"customerId":     "uuid",
"customerName":   "Nguy?n V?n A",                                                                                                                                                                                 
"subject":        "H?i v? refund",
"bookingId":      "uuid",                                                                                                                                                                                         
"priority":       "HIGH"                                                                                                                                                                                          
}

Spring STOMP Configuration:

// chat-service/infrastructure/websocket/WebSocketConfig.java
@Configuration                                                                                                                                                                                                          
@EnableWebSocketMessageBroker
@RequiredArgsConstructor                                                                                                                                                                                                
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

      private final JwtChannelInterceptor jwtChannelInterceptor;
                                                                                                                                                                                                                          
      @Override   
      public void configureMessageBroker(MessageBrokerRegistry registry) {
          // In-memory broker cho /topic (broadcast) v? /queue (point-to-point)                                                                                                                                           
          // Production: thay b?ng RabbitMQ native STOMP ?? multi-instance support                                                                                                                                        
          registry.enableSimpleBroker("/topic", "/user/queue");                                                                                                                                                           
          registry.setApplicationDestinationPrefixes("/app");                                                                                                                                                             
          registry.setUserDestinationPrefix("/user");                                                                                                                                                                     
      }                                                                                                                                                                                                                   
                  
      @Override
      public void registerStompEndpoints(StompEndpointRegistry registry) {
          registry.addEndpoint("/ws")
              .setAllowedOriginPatterns("https://*.cinetix.vn", "http://localhost:*")                                                                                                                                     
              .withSockJS()
                  .setHeartbeatTime(25_000)       // 25s heartbeat                                                                                                                                                        
                  .setDisconnectDelay(5_000);                                                                                                                                                                             
      }                                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      @Override   
      public void configureClientInboundChannel(ChannelRegistration registration) {
          // JWT validation tr??c khi message ??n handler
          registration.interceptors(jwtChannelInterceptor);                                                                                                                                                               
      }                                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      @Override                                                                                                                                                                                                           
      public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
          registry.setMessageSizeLimit(64 * 1024);      // 64KB max message                                                                                                                                               
          registry.setSendBufferSizeLimit(512 * 1024);  // 512KB send buffer
          registry.setSendTimeLimit(20_000);             // 20s send timeout                                                                                                                                              
      }                                                                                                                                                                                                                   
}

// JWT Interceptor cho STOMP CONNECT                                                                                                                                                                                    
@Component
@RequiredArgsConstructor                                                                                                                                                                                                
public class JwtChannelInterceptor implements ChannelInterceptor {

      private final JwtDecoder jwtDecoder;

      @Override
      public Message<?> preSend(Message<?> message, MessageChannel channel) {
          StompHeaderAccessor accessor =                                                                                                                                                                                  
              MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                                                                                                                                                                                                                          
          if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
              String authHeader = accessor.getFirstNativeHeader("Authorization");                                                                                                                                         
              if (authHeader == null || !authHeader.startsWith("Bearer ")) {                                                                                                                                              
                  throw new MessagingException("Missing JWT token in STOMP CONNECT");
              }                                                                                                                                                                                                           
                  
              String token = authHeader.substring(7);                                                                                                                                                                     
              try {
                  Jwt jwt = jwtDecoder.decode(token);
                  // Set user principal t? JWT sub claim
                  UsernamePasswordAuthenticationToken auth =                                                                                                                                                              
                      new UsernamePasswordAuthenticationToken(
                          jwt.getSubject(), null,                                                                                                                                                                         
                          extractAuthorities(jwt)
                      );                                                                                                                                                                                                  
                  accessor.setUser(auth);                                                                                                                                                                                 
              } catch (JwtException e) {
                  throw new MessagingException("Invalid JWT token");                                                                                                                                                      
              }   
          }
          return message;
      }

      private List<GrantedAuthority> extractAuthorities(Jwt jwt) {                                                                                                                                                        
          List<String> roles = jwt.getClaimAsStringList("roles");
          if (roles == null) return List.of();                                                                                                                                                                            
          return roles.stream()                                                                                                                                                                                           
              .map(r -> (GrantedAuthority) () -> "ROLE_" + r)
              .toList();                                                                                                                                                                                                  
      }           
}

// showtime-service: broadcast seat changes l?n STOMP
@Service                                                                                                                                                                                                                
@RequiredArgsConstructor
public class SeatMapBroadcaster {

      private final SimpMessagingTemplate messagingTemplate;                                                                                                                                                              
                  
      public void broadcastSeatChanges(String showtimeId, List<SeatChange> changes, int availableCount) {                                                                                                                 
          var payload = Map.of(
              "eventType",      "SEAT_STATUS_CHANGED",                                                                                                                                                                    
              "showtimeId",     showtimeId,                                                                                                                                                                               
              "changes",        changes,
              "availableCount", availableCount,                                                                                                                                                                           
              "timestamp",      Instant.now().toString()
          );
          messagingTemplate.convertAndSend(                                                                                                                                                                               
              "/topic/showtime/" + showtimeId + "/seats",
              payload                                                                                                                                                                                                     
          );      
      }

      public void notifyBookingStatusChange(String userId, String bookingId,                                                                                                                                              
                                             String status, Map<String, Object> data) {
          var payload = Map.of(                                                                                                                                                                                           
              "eventType", "BOOKING_STATUS_CHANGED",
              "bookingId", bookingId,                                                                                                                                                                                     
              "status",    status,
              "data",      data,                                                                                                                                                                                          
              "timestamp", Instant.now().toString()
          );
          // /user/{userId}/queue/booking/{bookingId}
          messagingTemplate.convertAndSendToUser(                                                                                                                                                                         
              userId,
              "/queue/booking/" + bookingId,                                                                                                                                                                              
              payload
          );
      }
}

?? Production WebSocket ― Multi-Instance Problem:

V?n ??: 2 instance c?a showtime-service, user A k?t n?i instance 1,                                                                                                                                                     
user B k?t n?i instance 2. Instance 2 broadcast → user A kh?ng nh?n.

Gi?i ph?p: D?ng RabbitMQ STOMP plugin l?m message broker thay v? in-memory.

Spring config:                                                                                                                                                                                                          
registry.enableStompBrokerRelay("/topic", "/user/queue")
.setRelayHost("rabbitmq")                                                                                                                                                                                         
.setRelayPort(61613)          // STOMP port c?a RabbitMQ                                                                                                                                                          
.setClientLogin("stomp-user")
.setClientPasscode("password")                                                                                                                                                                                    
.setSystemHeartbeatReceiveInterval(10_000)
.setSystemHeartbeatSendInterval(10_000);

K?t qu?: T?t c? instances ??u nh?n broadcast qua RabbitMQ STOMP broker.
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
Phase 5 ho?n th?nh ― to?n b? contracts ?? ???c ??nh ngh?a production-ready. G? "phase 6" ?? t?i thi?t k? Code Structure: monorepo layout, Hexagonal Architecture cho booking-service, v? code m?u ??y ??.

? Worked for 2m 51s

? phase 6

● PHASE 6 ― CODE STRUCTURE

6.1 Monorepo Structure ― Maven Multi-Module

Maven vs Gradle ― Quy?t ??nh:

┌─────────────────────┬──────────────────────────┬──────────────────────────┐                                                                                                                                           
│                     │ Maven Multi-Module        │ Gradle Composite Build   │                                                                                                                                          
├─────────────────────┼──────────────────────────┼──────────────────────────┤                                                                                                                                           
│ Spring Boot support │ ? First-class, official  │ ? T?t                   │                                                                                                                                            
│ Dependency mgmt     │ ? BOM + dependencyMgmt   │ ? Platform dependencies │                                                                                                                                            
│ IDE support         │ ? IntelliJ native        │ ? IntelliJ native        │                                                                                                                                           
│ Build speed         │ ?? Ch?m h?n (sequential) │ ? Parallel by default    │                                                                                                                                           
│ Learning curve      │ ? Quen thu?c Java team   │  ?? Groovy/Kotlin DSL     │                                                                                                                                          
│ CI/CD tooling       │ ? mvn -pl :service       │ ? ./gradlew :service     │                                                                                                                                           
│ Incremental build   │ ?? Limited                │ ? Excellent              │                                                                                                                                          
└─────────────────────┴──────────────────────────┴──────────────────────────┘

Khuy?n ngh?: Maven Multi-Module                                                                                                                                                                                         
L? do: Team Java th??ng quen Maven h?n, Spring Initializr generate Maven,                                                                                                                                               
tooling (SonarQube, Jacoco, Spotbugs) support t?t h?n.                                                                                                                                                           
Incremental build kh?ng quan tr?ng b?ng khi d?ng Docker layer cache.

C?u tr?c th? m?c Monorepo:

cinetix/        
├── .github/                                                                                                                                                                                                            
│   └── workflows/
│       ├── ci.yml                                                                                                                                                                                                      
│       └── release.yml
├── .mvn/
│   └── wrapper/                                                                                                                                                                                                        
│       └── maven-wrapper.properties
│                                                                                                                                                                                                                       
├── infrastructure/                          # Shared infra configs
│   ├── docker/                                                                                                                                                                                                         
│   │   ├── docker-compose.yml
│   │   └── docker-compose.observability.yml                                                                                                                                                                            
│   ├── k8s/    
│   │   ├── namespaces/                                                                                                                                                                                                 
│   │   └── infra/
│   │       ├── postgres/                                                                                                                                                                                               
│   │       ├── rabbitmq/
│   │       ├── redis/                                                                                                                                                                                                  
│   │       └── keycloak/
│   └── helm/                                                                                                                                                                                                           
│       ├── _common/                         # Shared Helm templates
│       └── charts/                                                                                                                                                                                                     
│           ├── booking-service/
│           ├── showtime-service/                                                                                                                                                                                       
│           └── ...                                                                                                                                                                                                     
│
├── proto/                                   # Shared .proto files                                                                                                                                                      
│   ├── common/v1/common.proto
│   ├── showtime/v1/showtime.proto                                                                                                                                                                                      
│   ├── payment/v1/payment.proto
│   └── promotion/v1/promotion.proto                                                                                                                                                                                    
│                                                                                                                                                                                                                       
├── libs/                                    # Shared libraries (Maven modules)
│   ├── cinetix-common/                      # Common domain primitives                                                                                                                                                 
│   │   ├── pom.xml                                                                                                                                                                                                     
│   │   └── src/main/java/com/cinetix/common/
│   │       ├── domain/                                                                                                                                                                                                 
│   │       │   ├── DomainEvent.java                                                                                                                                                                                    
│   │       │   ├── AggregateRoot.java
│   │       │   └── valueobject/                                                                                                                                                                                        
│   │       │       ├── Money.java
│   │       │       └── UserId.java                                                                                                                                                                                     
│   │       ├── exception/
│   │       │   ├── BusinessException.java                                                                                                                                                                              
│   │       │   └── ErrorCode.java
│   │       └── web/                                                                                                                                                                                                    
│   │           ├── ApiResponse.java
│   │           └── GlobalExceptionHandler.java                                                                                                                                                                         
│   │           
│   ├── cinetix-security/                    # JWT / Spring Security config                                                                                                                                             
│   │   ├── pom.xml
│   │   └── src/main/java/com/cinetix/security/                                                                                                                                                                         
│   │       ├── JwtSecurityConfig.java                                                                                                                                                                                  
│   │       └── CurrentUserResolver.java
│   │                                                                                                                                                                                                                   
│   ├── cinetix-outbox/                      # Outbox pattern shared impl
│   │   ├── pom.xml                                                                                                                                                                                                     
│   │   └── src/main/java/com/cinetix/outbox/
│   │       ├── OutboxEvent.java                                                                                                                                                                                        
│   │       ├── OutboxEventPublisher.java
│   │       └── OutboxRelayScheduler.java                                                                                                                                                                               
│   │                                                                                                                                                                                                                   
│   └── cinetix-grpc-stubs/                  # Generated gRPC stubs (t? proto)
│       ├── pom.xml                                                                                                                                                                                                     
│       └── src/main/java/com/cinetix/grpc/
│           ├── showtime/v1/                                                                                                                                                                                            
│           ├── payment/v1/                                                                                                                                                                                             
│           └── promotion/v1/
│                                                                                                                                                                                                                       
├── services/   
│   ├── api-gateway/
│   ├── identity-service/                                                                                                                                                                                               
│   ├── movie-service/
│   ├── cinema-service/                                                                                                                                                                                                 
│   ├── showtime-service/
│   ├── booking-service/                     # ← Deep dive b?n d??i                                                                                                                                                     
│   ├── payment-service/                                                                                                                                                                                                
│   ├── ticket-service/                                                                                                                                                                                                 
│   ├── promotion-service/                                                                                                                                                                                              
│   ├── notification-service/
│   └── chat-service/                                                                                                                                                                                                   
│
├── pom.xml                                  # Root POM                                                                                                                                                                 
└── mvnw

Root pom.xml:

  <?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"                                                                                                                                                          
xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
https://maven.apache.org/xsd/maven-4.0.0.xsd">                                                                                                                                                                 
<modelVersion>4.0.0</modelVersion>

      <groupId>com.cinetix</groupId>                                                                                                                                                                                      
      <artifactId>cinetix-platform</artifactId>
      <version>${revision}</version>                                                                                                                                                                                      
      <packaging>pom</packaging>
      <name>CinemaTix Platform</name>                                                                                                                                                                                     
   
      <properties>                                                                                                                                                                                                        
          <revision>1.0.0-SNAPSHOT</revision>
          <java.version>21</java.version>
          <maven.compiler.source>21</maven.compiler.source>                                                                                                                                                               
          <maven.compiler.target>21</maven.compiler.target>
          <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>                                                                                                                                              
                                                                                                                                                                                                                          
          <!-- Spring versions -->
          <spring-boot.version>3.3.4</spring-boot.version>                                                                                                                                                                
          <spring-cloud.version>2023.0.3</spring-cloud.version>                                                                                                                                                           
   
          <!-- Third-party -->                                                                                                                                                                                            
          <grpc.version>1.65.1</grpc.version>
          <protobuf.version>3.25.3</protobuf.version>                                                                                                                                                                     
          <mapstruct.version>1.5.5.Final</mapstruct.version>
          <resilience4j.version>2.2.0</resilience4j.version>                                                                                                                                                              
          <testcontainers.version>1.20.1</testcontainers.version>
          <opentelemetry.version>2.6.0</opentelemetry.version>                                                                                                                                                            
      </properties>
                                                                                                                                                                                                                          
      <modules>                                                                                                                                                                                                           
          <!-- Shared libs ― build tr??c -->
          <module>libs/cinetix-common</module>                                                                                                                                                                            
          <module>libs/cinetix-security</module>
          <module>libs/cinetix-outbox</module>                                                                                                                                                                            
          <module>libs/cinetix-grpc-stubs</module>
                                                                                                                                                                                                                          
          <!-- Services -->
          <module>services/api-gateway</module>
          <module>services/identity-service</module>
          <module>services/movie-service</module>                                                                                                                                                                         
          <module>services/cinema-service</module>
          <module>services/showtime-service</module>                                                                                                                                                                      
          <module>services/booking-service</module>
          <module>services/payment-service</module>
          <module>services/ticket-service</module>                                                                                                                                                                        
          <module>services/promotion-service</module>
          <module>services/notification-service</module>                                                                                                                                                                  
          <module>services/chat-service</module>
      </modules>

      <!-- BOM ― qu?n l? version t?p trung -->                                                                                                                                                                            
      <dependencyManagement>
          <dependencies>                                                                                                                                                                                                  
              <dependency>
                  <groupId>org.springframework.boot</groupId>                                                                                                                                                             
                  <artifactId>spring-boot-dependencies</artifactId>
                  <version>${spring-boot.version}</version>                                                                                                                                                               
                  <type>pom</type>
                  <scope>import</scope>
              </dependency>                                                                                                                                                                                               
              <dependency>
                  <groupId>org.springframework.cloud</groupId>                                                                                                                                                            
                  <artifactId>spring-cloud-dependencies</artifactId>
                  <version>${spring-cloud.version}</version>
                  <type>pom</type>                                                                                                                                                                                        
                  <scope>import</scope>
              </dependency>                                                                                                                                                                                               
              <dependency>
                  <groupId>io.grpc</groupId>
                  <artifactId>grpc-bom</artifactId>
                  <version>${grpc.version}</version>                                                                                                                                                                      
                  <type>pom</type>
                  <scope>import</scope>                                                                                                                                                                                   
              </dependency>
              <!-- Internal libs -->
              <dependency>                                                                                                                                                                                                
                  <groupId>com.cinetix</groupId>
                  <artifactId>cinetix-common</artifactId>                                                                                                                                                                 
                  <version>${revision}</version>
              </dependency>
              <dependency>
                  <groupId>com.cinetix</groupId>                                                                                                                                                                          
                  <artifactId>cinetix-security</artifactId>
                  <version>${revision}</version>                                                                                                                                                                          
              </dependency>
              <dependency>
                  <groupId>com.cinetix</groupId>
                  <artifactId>cinetix-outbox</artifactId>                                                                                                                                                                 
                  <version>${revision}</version>
              </dependency>                                                                                                                                                                                               
              <dependency>
                  <groupId>com.cinetix</groupId>
                  <artifactId>cinetix-grpc-stubs</artifactId>
                  <version>${revision}</version>                                                                                                                                                                          
              </dependency>
          </dependencies>                                                                                                                                                                                                 
      </dependencyManagement>
  </project>

  ---
6.2 Hexagonal Architecture ― booking-service

services/booking-service/
├── pom.xml                                                                                                                                                                                                             
└── src/        
├── main/                                                                                                                                                                                                           
│   ├── java/com/cinetix/booking/
│   │   │                                                                                                                                                                                                           
│   │   ├── domain/                          # ? DOMAIN LAYER ― thu?n Java, 0 framework
│   │   │   ├── model/                                                                                                                                                                                              
│   │   │   │   ├── Booking.java             # Aggregate Root
│   │   │   │   ├── BookingItem.java          # Entity                                                                                                                                                              
│   │   │   │   ├── BookingStatus.java        # Enum + transition rules
│   │   │   │   ├── SagaStep.java             # Enum                                                                                                                                                                
│   │   │   │   └── valueobject/                                                                                                                                                                                    
│   │   │   │       ├── BookingId.java                                                                                                                                                                              
│   │   │   │       ├── CustomerId.java                                                                                                                                                                             
│   │   │   │       ├── ShowtimeId.java                                                                                                                                                                             
│   │   │   │       ├── SeatId.java
│   │   │   │       ├── PaymentId.java                                                                                                                                                                              
│   │   │   │       ├── VoucherCode.java
│   │   │   │       └── SeatSelection.java                                                                                                                                                                          
│   │   │   ├── event/                        # Domain Events (records)
│   │   │   │   ├── BookingInitiatedEvent.java                                                                                                                                                                      
│   │   │   │   ├── BookingConfirmedEvent.java
│   │   │   │   ├── BookingCancelledEvent.java                                                                                                                                                                      
│   │   │   │   └── BookingExpiredEvent.java
│   │   │   ├── port/                         # Ports (interfaces) ― DIP                                                                                                                                            
│   │   │   │   ├── outbound/                                                                                                                                                                                       
│   │   │   │   │   ├── BookingRepository.java
│   │   │   │   │   ├── SagaStateRepository.java                                                                                                                                                                    
│   │   │   │   │   ├── ShowtimePort.java      # gRPC client port
│   │   │   │   │   ├── PaymentPort.java                                                                                                                                                                            
│   │   │   │   │   ├── PromotionPort.java
│   │   │   │   │   ├── EventPublisherPort.java                                                                                                                                                                     
│   │   │   │   │   └── BookingReadModelPort.java
│   │   │   │   └── inbound/                                                                                                                                                                                        
│   │   │   │       ├── CreateBookingUseCase.java
│   │   │   │       ├── CancelBookingUseCase.java                                                                                                                                                                   
│   │   │   │       ├── GetBookingUseCase.java
│   │   │   │       └── HandlePaymentResultUseCase.java                                                                                                                                                             
│   │   │   └── service/                      # Domain Services
│   │   │       └── BookingPricingService.java                                                                                                                                                                      
│   │   │   
│   │   ├── application/                      # ? APPLICATION LAYER ― orchestration                                                                                                                                 
│   │   │   ├── usecase/
│   │   │   │   ├── CreateBookingUseCaseImpl.java                                                                                                                                                                   
│   │   │   │   ├── CancelBookingUseCaseImpl.java                                                                                                                                                                   
│   │   │   │   ├── GetBookingUseCaseImpl.java
│   │   │   │   └── HandlePaymentResultUseCaseImpl.java                                                                                                                                                             
│   │   │   ├── saga/                                                                                                                                                                                               
│   │   │   │   ├── BookingSagaOrchestrator.java
│   │   │   │   └── BookingExpiryHandler.java                                                                                                                                                                       
│   │   │   └── dto/                          # Application-layer DTOs                                                                                                                                              
│   │   │       ├── command/                                                                                                                                                                                        
│   │   │       │   ├── CreateBookingCommand.java                                                                                                                                                                   
│   │   │       │   └── CancelBookingCommand.java                                                                                                                                                                   
│   │   │       └── query/                                                                                                                                                                                          
│   │   │           └── BookingDetailDto.java
│   │   │                                                                                                                                                                                                           
│   │   ├── infrastructure/                   # ? INFRA LAYER ― adapters
│   │   │   ├── persistence/                  # Outbound: DB adapter                                                                                                                                                
│   │   │   │   ├── entity/
│   │   │   │   │   ├── BookingJpaEntity.java                                                                                                                                                                       
│   │   │   │   │   ├── BookingItemJpaEntity.java
│   │   │   │   │   └── SagaStateJpaEntity.java                                                                                                                                                                     
│   │   │   │   ├── repository/                                                                                                                                                                                     
│   │   │   │   │   ├── BookingJpaRepository.java
│   │   │   │   │   └── SagaStateJpaRepository.java                                                                                                                                                                 
│   │   │   │   ├── mapper/
│   │   │   │   │   └── BookingPersistenceMapper.java                                                                                                                                                               
│   │   │   │   ├── BookingRepositoryAdapter.java                                                                                                                                                                   
│   │   │   │   ├── SagaStateRepositoryAdapter.java                                                                                                                                                                 
│   │   │   │   └── BookingReadModelAdapter.java                                                                                                                                                                    
│   │   │   ├── grpc/                         # Outbound: gRPC clients                                                                                                                                              
│   │   │   │   ├── ShowtimeGrpcAdapter.java                                                                                                                                                                        
│   │   │   │   ├── PaymentGrpcAdapter.java                                                                                                                                                                         
│   │   │   │   └── PromotionGrpcAdapter.java                                                                                                                                                                       
│   │   │   ├── messaging/                    # Outbound: RabbitMQ publish                                                                                                                                          
│   │   │   │   ├── OutboxEventPublisherAdapter.java                                                                                                                                                                
│   │   │   │   └── consumer/
│   │   │   │       └── PaymentEventConsumer.java   # Inbound: RabbitMQ consume                                                                                                                                     
│   │   │   ├── websocket/                    # Outbound: WS notify                                                                                                                                                 
│   │   │   │   └── BookingWebSocketNotifier.java                                                                                                                                                                   
│   │   │   └── scheduler/                                                                                                                                                                                          
│   │   │       └── BookingExpiryJob.java
│   │   │                                                                                                                                                                                                           
│   │   ├── adapter/                          # ? INBOUND adapters
│   │   │   └── rest/                                                                                                                                                                                               
│   │   │       ├── BookingController.java
│   │   │       ├── dto/                                                                                                                                                                                            
│   │   │       │   ├── request/
│   │   │       │   │   └── CreateBookingRequest.java                                                                                                                                                               
│   │   │       │   └── response/
│   │   │       │       ├── BookingResponse.java                                                                                                                                                                    
│   │   │       │       └── CreateBookingResponse.java
│   │   │       └── mapper/                                                                                                                                                                                         
│   │   │           └── BookingRestMapper.java                                                                                                                                                                      
│   │   │
│   │   └── config/                           # ? CONFIG                                                                                                                                                            
│   │       ├── GrpcClientConfig.java                                                                                                                                                                               
│   │       ├── RabbitMQConfig.java
│   │       ├── RedisConfig.java                                                                                                                                                                                    
│   │       ├── QuartzConfig.java                                                                                                                                                                                   
│   │       ├── SecurityConfig.java
│   │       └── OpenTelemetryConfig.java                                                                                                                                                                            
│   │                                                                                                                                                                                                               
│   └── resources/
│       ├── application.yml                                                                                                                                                                                         
│       ├── application-local.yml
│       └── db/migration/                                                                                                                                                                                           
│           ├── V1__create_booking_schema.sql
│           └── V2__create_read_model.sql                                                                                                                                                                           
│                                                                                                                                                                                                                   
└── test/
├── java/com/cinetix/booking/                                                                                                                                                                                   
│   ├── domain/
│   │   └── BookingTest.java              # Unit test ― no Spring context                                                                                                                                       
│   ├── application/                                                                                                                                                                                            
│   │   └── BookingSagaOrchestratorTest.java                                                                                                                                                                    
│   └── infrastructure/                                                                                                                                                                                         
│       ├── BookingRepositoryIT.java       # Testcontainers
│       └── PaymentEventConsumerIT.java                                                                                                                                                                         
└── resources/                                                                                                                                                                                                  
└── application-test.yml
                                                                                                                                                                                                                          
---             
6.3 Code M?u ― Production-Ready

? Booking.java ― Aggregate Root ho?n ch?nh:

// domain/model/Booking.java
package com.cinetix.booking.domain.model;

import com.cinetix.booking.domain.event.*;                                                                                                                                                                              
import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.common.domain.AggregateRoot;                                                                                                                                                                         
import jakarta.persistence.*;
import lombok.AccessLevel;                                                                                                                                                                                              
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "bookings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA c?n no-arg constructor
public class Booking extends AggregateRoot<BookingId> {

      @EmbeddedId                                                                                                                                                                                                         
      @AttributeOverride(name = "value", column = @Column(name = "id"))
      private BookingId id;                                                                                                                                                                                               
   
      @Embedded                                                                                                                                                                                                           
      @AttributeOverride(name = "value", column = @Column(name = "customer_id"))
      private CustomerId customerId;                                                                                                                                                                                      
   
      @Embedded                                                                                                                                                                                                           
      @AttributeOverride(name = "value", column = @Column(name = "showtime_id"))
      private ShowtimeId showtimeId;
                                                                                                                                                                                                                          
      @Column(name = "cinema_id", nullable = false)
      private UUID cinemaId;                                                                                                                                                                                              
                  
      @Column(name = "movie_id", nullable = false)                                                                                                                                                                        
      private UUID movieId;
                                                                                                                                                                                                                          
      @OneToMany( 
          mappedBy = "booking",
          cascade = CascadeType.ALL,
          orphanRemoval = true,
          fetch = FetchType.EAGER                                                                                                                                                                                         
      )
      @OrderBy("seatCode ASC")                                                                                                                                                                                            
      private List<BookingItem> items = new ArrayList<>();
                                                                                                                                                                                                                          
      @Enumerated(EnumType.STRING)
      @Column(name = "status", nullable = false, length = 30)                                                                                                                                                             
      private BookingStatus status;

      @Embedded
      @AttributeOverrides({
          @AttributeOverride(name = "amount",   column = @Column(name = "subtotal")),                                                                                                                                     
          @AttributeOverride(name = "currency", column = @Column(name = "currency"))
      })                                                                                                                                                                                                                  
      private Money subtotal;
                                                                                                                                                                                                                          
      @Column(name = "discount_amount", nullable = false)
      private long discountAmountValue = 0L;
                                                                                                                                                                                                                          
      @Column(name = "final_amount", nullable = false)
      private long finalAmountValue;                                                                                                                                                                                      
                                                                                                                                                                                                                          
      @Embedded
      @AttributeOverride(name = "value", column = @Column(name = "voucher_code"))                                                                                                                                         
      private VoucherCode appliedVoucher;
                                                                                                                                                                                                                          
      @Embedded
      @AttributeOverride(name = "value", column = @Column(name = "payment_id"))                                                                                                                                           
      private PaymentId paymentId;

      @Column(name = "expires_at", nullable = false)                                                                                                                                                                      
      private Instant expiresAt;
                                                                                                                                                                                                                          
      @Column(name = "created_at", nullable = false, updatable = false)
      private Instant createdAt;

      @Column(name = "updated_at", nullable = false)                                                                                                                                                                      
      private Instant updatedAt;
                                                                                                                                                                                                                          
      @Version    
      @Column(name = "version", nullable = false)
      private Long version;

      private static final int MAX_SEATS_PER_BOOKING = 8;                                                                                                                                                                 
      private static final Duration BOOKING_TTL = Duration.ofMinutes(10);
                                                                                                                                                                                                                          
      // ─── Factory ─────────────────────────────────────────────────────────────                                                                                                                                        
      public static Booking initiate(
              CustomerId customerId,                                                                                                                                                                                      
              ShowtimeId showtimeId,                                                                                                                                                                                      
              UUID cinemaId,
              UUID movieId,                                                                                                                                                                                               
              List<SeatSelection> selections,
              Clock clock) {
                                                                                                                                                                                                                          
          Objects.requireNonNull(customerId, "customerId required");
          Objects.requireNonNull(showtimeId, "showtimeId required");                                                                                                                                                      
          if (selections == null || selections.isEmpty()) {
              throw new IllegalArgumentException("At least one seat required");                                                                                                                                           
          }
          if (selections.size() > MAX_SEATS_PER_BOOKING) {                                                                                                                                                                
              throw new IllegalArgumentException(
                  "Cannot book more than " + MAX_SEATS_PER_BOOKING + " seats");                                                                                                                                           
          }
                                                                                                                                                                                                                          
          var booking = new Booking();
          booking.id = BookingId.generate();
          booking.customerId = customerId;                                                                                                                                                                                
          booking.showtimeId = showtimeId;
          booking.cinemaId = cinemaId;                                                                                                                                                                                    
          booking.movieId = movieId;
          booking.status = BookingStatus.INITIATED;
          booking.createdAt = clock.instant();                                                                                                                                                                            
          booking.updatedAt = clock.instant();
          booking.expiresAt = booking.createdAt.plus(BOOKING_TTL);                                                                                                                                                        
                  
          selections.forEach(s -> booking.items.add(                                                                                                                                                                      
              BookingItem.of(booking, s.seatId(), s.seatCode(), s.category(), s.price())
          ));                                                                                                                                                                                                             
                  
          booking.subtotal = booking.recalculateSubtotal();                                                                                                                                                               
          booking.finalAmountValue = booking.subtotal.amount().longValue();
                                                                                                                                                                                                                          
          booking.registerEvent(BookingInitiatedEvent.from(booking));
          return booking;                                                                                                                                                                                                 
      }                                                                                                                                                                                                                   
   
      // ─── State transitions ────────────────────────────────────────────────────                                                                                                                                       
      public void markSeatsHeld(Instant at) {
          transitionTo(BookingStatus.SEATS_HELD, at);                                                                                                                                                                     
      }
                                                                                                                                                                                                                          
      public void applyVoucher(VoucherCode code, Money discount, Instant at) {                                                                                                                                            
          transitionTo(BookingStatus.VOUCHER_APPLIED, at);
          this.appliedVoucher = code;                                                                                                                                                                                     
          this.discountAmountValue = discount.amount().longValue();
          this.finalAmountValue = subtotal.amount().longValue() - discountAmountValue;                                                                                                                                    
          if (finalAmountValue < 0) {                                                                                                                                                                                     
              throw new IllegalArgumentException("Discount cannot exceed subtotal");
          }                                                                                                                                                                                                               
      }           

      public void markPaymentPending(PaymentId paymentId, Instant at) {                                                                                                                                                   
          // Cho ph?p c? SEATS_HELD v? VOUCHER_APPLIED → PAYMENT_PENDING
          if (status != BookingStatus.SEATS_HELD && status != BookingStatus.VOUCHER_APPLIED) {                                                                                                                            
              throw new InvalidBookingTransitionException(id, status, BookingStatus.PAYMENT_PENDING);                                                                                                                     
          }                                                                                                                                                                                                               
          this.status = BookingStatus.PAYMENT_PENDING;                                                                                                                                                                    
          this.paymentId = paymentId;                                                                                                                                                                                     
          this.updatedAt = at;
      }                                                                                                                                                                                                                   
                  
      public void startConfirming(Instant at) {                                                                                                                                                                           
          transitionTo(BookingStatus.CONFIRMING, at);
      }                                                                                                                                                                                                                   
                  
      public void confirm(Instant at) {
          transitionTo(BookingStatus.CONFIRMED, at);
          registerEvent(BookingConfirmedEvent.from(this, at));                                                                                                                                                            
      }
                                                                                                                                                                                                                          
      public void startCompensating(String reason, Instant at) {                                                                                                                                                          
          transitionTo(BookingStatus.COMPENSATING, at);
          registerEvent(BookingCompensationStartedEvent.of(id, reason, at));                                                                                                                                              
      }                                                                                                                                                                                                                   
   
      public void cancel(String reason, Instant at) {                                                                                                                                                                     
          if (status != BookingStatus.COMPENSATING && status != BookingStatus.INITIATED
                  && status != BookingStatus.SEATS_HELD) {                                                                                                                                                                
              throw new InvalidBookingTransitionException(id, status, BookingStatus.CANCELLED);
          }                                                                                                                                                                                                               
          this.status = BookingStatus.CANCELLED;
          this.updatedAt = at;                                                                                                                                                                                            
          registerEvent(BookingCancelledEvent.of(id, customerId, reason, at));
      }
                                                                                                                                                                                                                          
      public void fail(String reason, Instant at) {
          this.status = BookingStatus.FAILED;                                                                                                                                                                             
          this.updatedAt = at;
          registerEvent(BookingFailedEvent.of(id, reason, at));
      }                                                                                                                                                                                                                   
   
      // ─── Queries ──────────────────────────────────────────────────────────────                                                                                                                                       
      public boolean isExpired(Clock clock) {
          return Instant.now(clock).isAfter(expiresAt);                                                                                                                                                                   
      }
                                                                                                                                                                                                                          
      public boolean isPaid() {
          return paymentId != null;
      }                                                                                                                                                                                                                   
   
      public boolean hasVoucher() {                                                                                                                                                                                       
          return appliedVoucher != null;
      }

      public List<SeatId> getSeatIds() {                                                                                                                                                                                  
          return items.stream().map(BookingItem::getSeatId).toList();
      }                                                                                                                                                                                                                   
                  
      public Money getFinalAmount() {
          return Money.of(finalAmountValue, subtotal.currency().getCurrencyCode());
      }                                                                                                                                                                                                                   
   
      public Money getDiscountAmount() {                                                                                                                                                                                  
          return Money.of(discountAmountValue, subtotal.currency().getCurrencyCode());
      }                                                                                                                                                                                                                   
   
      // ─── Private helpers ──────────────────────────────────────────────────────                                                                                                                                       
      private void transitionTo(BookingStatus next, Instant at) {
          if (!status.canTransitionTo(next)) {                                                                                                                                                                            
              throw new InvalidBookingTransitionException(id, status, next);
          }                                                                                                                                                                                                               
          this.status = next;
          this.updatedAt = at;                                                                                                                                                                                            
      }           

      private Money recalculateSubtotal() {
          return items.stream()
              .map(BookingItem::getPrice)
              .reduce(Money.ZERO_VND, Money::add);
      }                                                                                                                                                                                                                   
   
      @Override                                                                                                                                                                                                           
      public BookingId getId() { return id; }
}

? BookingSagaOrchestrator.java ― Full Implementation:

// application/saga/BookingSagaOrchestrator.java
package com.cinetix.booking.application.saga;

import com.cinetix.booking.domain.model.*;                                                                                                                                                                              
import com.cinetix.booking.domain.model.valueobject.*;
import com.cinetix.booking.domain.port.outbound.*;                                                                                                                                                                      
import com.cinetix.booking.application.dto.command.CreateBookingCommand;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;                                                                                                                                                 
import io.github.resilience4j.retry.annotation.Retry;                                                                                                                                                                   
import io.micrometer.observation.annotation.Observed;                                                                                                                                                                   
import lombok.RequiredArgsConstructor;                                                                                                                                                                                  
import lombok.extern.slf4j.Slf4j;                                                                                                                                                                                       
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.UUID;

@Service
@RequiredArgsConstructor                                                                                                                                                                                                
@Slf4j          
@Observed(name = "booking.saga")
public class BookingSagaOrchestrator {

      private final BookingRepository       bookingRepo;
      private final SagaStateRepository     sagaStateRepo;                                                                                                                                                                
      private final ShowtimePort            showtimePort;
      private final PaymentPort             paymentPort;                                                                                                                                                                  
      private final PromotionPort           promotionPort;
      private final EventPublisherPort      eventPublisher;                                                                                                                                                               
      private final Clock                   clock;
                                                                                                                                                                                                                          
      // ─── ENTRY POINT ─────────────────────────────────────────────────────────
      @Transactional                                                                                                                                                                                                      
      public BookingId start(CreateBookingCommand cmd) {                                                                                                                                                                  
          log.info("Starting booking saga for customer={} showtime={}",
              cmd.customerId(), cmd.showtimeId());                                                                                                                                                                        
                  
          // Step 0: T?o Booking aggregate                                                                                                                                                                                
          var booking = Booking.initiate(
              CustomerId.of(cmd.customerId()),                                                                                                                                                                            
              ShowtimeId.of(cmd.showtimeId()),
              cmd.cinemaId(),                                                                                                                                                                                             
              cmd.movieId(),
              cmd.selections(),
              clock
          );                                                                                                                                                                                                              
          bookingRepo.save(booking);
          sagaStateRepo.save(SagaState.create(booking.getId(), SagaStep.HOLD_SEATS));                                                                                                                                     
          publishDomainEvents(booking);                                                                                                                                                                                   
   
          // Step 1: Hold seats (sync, trong c?ng transaction)                                                                                                                                                            
          executeHoldSeats(booking);
                                                                                                                                                                                                                          
          return booking.getId();                                                                                                                                                                                         
      }
                                                                                                                                                                                                                          
      // ─── STEP 1: Hold Seats ───────────────────────────────────────────────────
      @CircuitBreaker(name = "showtime-service", fallbackMethod = "holdSeatsFallback")
      @Retry(name = "showtime-hold")                                                                                                                                                                                      
      private void executeHoldSeats(Booking booking) {
          log.debug("Saga[{}] STEP 1: HoldSeats for {} seats",                                                                                                                                                            
              booking.getId(), booking.getSeatIds().size());                                                                                                                                                              
                                                                                                                                                                                                                          
          var result = showtimePort.holdSeats(                                                                                                                                                                            
              booking.getShowtimeId(),
              booking.getSeatIds(),                                                                                                                                                                                       
              booking.getId(),
              600                                                                                                                                                                                                         
          );                                                                                                                                                                                                              
   
          result.ifSuccessOrElse(                                                                                                                                                                                         
              success -> {
                  booking.markSeatsHeld(clock.instant());
                  bookingRepo.save(booking);
                  sagaStateRepo.updateStep(booking.getId(), SagaStep.VALIDATE_VOUCHER);                                                                                                                                   
                  log.info("Saga[{}] STEP 1 OK: {} seats held until {}",
                      booking.getId(), success.heldSeats().size(), success.expiresAt());                                                                                                                                  
                  executeValidateVoucher(booking);                                                                                                                                                                        
              },                                                                                                                                                                                                          
              failure -> {                                                                                                                                                                                                
                  log.warn("Saga[{}] STEP 1 FAIL: {}", booking.getId(), failure.errorCode());                                                                                                                             
                  booking.fail("SEATS_UNAVAILABLE: " + failure.errorCode(), clock.instant());                                                                                                                             
                  bookingRepo.save(booking);
                  publishDomainEvents(booking);                                                                                                                                                                           
              }   
          );                                                                                                                                                                                                              
      }           

      private void holdSeatsFallback(Booking booking, Exception ex) {                                                                                                                                                     
          log.error("Saga[{}] STEP 1 CIRCUIT OPEN: showtime-service unavailable",
              booking.getId(), ex);                                                                                                                                                                                       
          booking.fail("SHOWTIME_SERVICE_UNAVAILABLE", clock.instant());
          bookingRepo.save(booking);                                                                                                                                                                                      
          publishDomainEvents(booking);
      }                                                                                                                                                                                                                   
   
      // ─── STEP 2: Validate Voucher ─────────────────────────────────────────────                                                                                                                                       
      private void executeValidateVoucher(Booking booking) {
          if (!booking.hasVoucher()) {
              log.debug("Saga[{}] STEP 2: SKIPPED (no voucher)", booking.getId());                                                                                                                                        
              executeInitiatePayment(booking);
              return;                                                                                                                                                                                                     
          }       
                                                                                                                                                                                                                          
          log.debug("Saga[{}] STEP 2: ValidateVoucher code={}",                                                                                                                                                           
              booking.getId(), booking.getAppliedVoucher());
                                                                                                                                                                                                                          
          try {   
              var result = promotionPort.validateVoucher(
                  booking.getAppliedVoucher(),
                  booking.getCustomerId(),                                                                                                                                                                                
                  booking.getSubtotal(),
                  booking.getShowtimeId()                                                                                                                                                                                 
              );  

              result.ifSuccessOrElse(                                                                                                                                                                                     
                  success -> {
                      booking.applyVoucher(booking.getAppliedVoucher(),                                                                                                                                                   
                          success.discountAmount(), clock.instant());
                      bookingRepo.save(booking);                                                                                                                                                                          
                      sagaStateRepo.updateStep(booking.getId(), SagaStep.INITIATE_PAYMENT);
                      log.info("Saga[{}] STEP 2 OK: discount={}",                                                                                                                                                         
                          booking.getId(), success.discountAmount());                                                                                                                                                     
                      executeInitiatePayment(booking);
                  },                                                                                                                                                                                                      
                  failure -> {
                      log.warn("Saga[{}] STEP 2 FAIL: voucher invalid - {}",
                          booking.getId(), failure.errorCode());                                                                                                                                                          
                      compensate(booking, "INVALID_VOUCHER: " + failure.errorCode(),
                          false, false);                                                                                                                                                                                  
                  }
              );                                                                                                                                                                                                          
          } catch (Exception ex) {
              log.error("Saga[{}] STEP 2 ERROR: promotion-service unavailable", booking.getId(), ex);                                                                                                                     
              compensate(booking, "PROMOTION_SERVICE_UNAVAILABLE", false, false);
          }                                                                                                                                                                                                               
      }           
                                                                                                                                                                                                                          
      // ─── STEP 3: Initiate Payment ─────────────────────────────────────────────
      @CircuitBreaker(name = "payment-service", fallbackMethod = "initiatePaymentFallback")
      private void executeInitiatePayment(Booking booking) {                                                                                                                                                              
          log.debug("Saga[{}] STEP 3: InitiatePayment amount={}",
              booking.getId(), booking.getFinalAmount());                                                                                                                                                                 
                  
          var result = paymentPort.initiatePayment(                                                                                                                                                                       
              booking.getId(),
              booking.getCustomerId(),
              booking.getFinalAmount(),
              booking.getPaymentMethod()                                                                                                                                                                                  
          );
                                                                                                                                                                                                                          
          result.ifSuccessOrElse(
              success -> {
                  booking.markPaymentPending(
                      PaymentId.of(UUID.fromString(success.paymentId())),
                      clock.instant()                                                                                                                                                                                     
                  );
                  bookingRepo.save(booking);                                                                                                                                                                              
                  sagaStateRepo.updateStep(booking.getId(), SagaStep.AWAIT_PAYMENT);
                  publishDomainEvents(booking);                                                                                                                                                                           
                  log.info("Saga[{}] STEP 3 OK: paymentId={} url={}",
                      booking.getId(), success.paymentId(), success.paymentUrl());                                                                                                                                        
                  // Saga PAUSES HERE ― ti?p t?c khi nh?n PaymentCompleted/Failed event
              },                                                                                                                                                                                                          
              failure -> compensate(booking, "PAYMENT_INIT_FAILED: " + failure.errorCode(),
                  false, booking.hasVoucher())                                                                                                                                                                            
          );      
      }                                                                                                                                                                                                                   
                  
      private void initiatePaymentFallback(Booking booking, Exception ex) {
          log.error("Saga[{}] STEP 3 CIRCUIT OPEN: payment-service unavailable", booking.getId());
          compensate(booking, "PAYMENT_SERVICE_UNAVAILABLE",                                                                                                                                                              
              false, booking.hasVoucher());
      }                                                                                                                                                                                                                   
                  
      // ─── STEP 4+5: Resume on PaymentCompleted (called from consumer) ──────────                                                                                                                                       
      @Transactional
      public void onPaymentCompleted(UUID bookingId, UUID paymentId) {                                                                                                                                                    
          var booking = bookingRepo.findById(BookingId.of(bookingId))
              .orElseThrow(() -> new BookingNotFoundException(bookingId));                                                                                                                                                
   
          log.info("Saga[{}] Resuming: PaymentCompleted paymentId={}", bookingId, paymentId);                                                                                                                             
                  
          // Idempotency: n?u ?? CONFIRMED, b? qua                                                                                                                                                                        
          if (booking.getStatus() == BookingStatus.CONFIRMED) {
              log.warn("Saga[{}] already CONFIRMED, ignoring PaymentCompleted (duplicate)", bookingId);                                                                                                                   
              return;                                                                                                                                                                                                     
          }
          if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {                                                                                                                                                     
              log.error("Saga[{}] unexpected status {} on PaymentCompleted", bookingId, booking.getStatus());
              return;                                                                                                                                                                                                     
          }
                                                                                                                                                                                                                          
          booking.startConfirming(clock.instant());                                                                                                                                                                       
          bookingRepo.save(booking);
          executeConfirmSeats(booking);                                                                                                                                                                                   
      }           

      // ─── Compensation on PaymentFailed ───────────────────────────────────────                                                                                                                                        
      @Transactional
      public void onPaymentFailed(UUID bookingId, String reason) {                                                                                                                                                        
          var booking = bookingRepo.findById(BookingId.of(bookingId))
              .orElseThrow(() -> new BookingNotFoundException(bookingId));                                                                                                                                                
   
          if (booking.getStatus() != BookingStatus.PAYMENT_PENDING) {                                                                                                                                                     
              return; // idempotent
          }                                                                                                                                                                                                               
          log.info("Saga[{}] PaymentFailed: {}", bookingId, reason);
          compensate(booking, "PAYMENT_FAILED: " + reason, false, booking.hasVoucher());                                                                                                                                  
      }           
                                                                                                                                                                                                                          
      // ─── STEP 4: Confirm Seats + STEP 5: Redeem Voucher ──────────────────────                                                                                                                                        
      private void executeConfirmSeats(Booking booking) {
          try {                                                                                                                                                                                                           
              showtimePort.confirmSeats(
                  booking.getShowtimeId(),                                                                                                                                                                                
                  booking.getSeatIds(),
                  booking.getId()                                                                                                                                                                                         
              );  
              log.info("Saga[{}] STEP 4 OK: seats confirmed", booking.getId());
          } catch (Exception ex) {                                                                                                                                                                                        
              log.error("Saga[{}] STEP 4 FAIL: ConfirmSeats error ― initiating compensation with refund",
                  booking.getId(), ex);                                                                                                                                                                                   
              compensate(booking, "CONFIRM_SEATS_FAILED", true, booking.hasVoucher());
              return;                                                                                                                                                                                                     
          }       
                                                                                                                                                                                                                          
          if (booking.hasVoucher()) {
              try {
                  promotionPort.redeemVoucher(
                      booking.getAppliedVoucher(),                                                                                                                                                                        
                      booking.getId(),
                      booking.getCustomerId()                                                                                                                                                                             
                  );
                  log.info("Saga[{}] STEP 5 OK: voucher redeemed", booking.getId());
              } catch (Exception ex) {                                                                                                                                                                                    
                  // Voucher redeem fail → log + continue (non-critical path)
                  // Refund s? void voucher trong compensation n?u c?n                                                                                                                                                    
                  log.warn("Saga[{}] STEP 5 WARN: VoucherRedeem failed (non-critical), continuing",                                                                                                                       
                      booking.getId(), ex);                                                                                                                                                                               
              }                                                                                                                                                                                                           
          }                                                                                                                                                                                                               
                  
          booking.confirm(clock.instant());
          bookingRepo.save(booking);
          sagaStateRepo.updateStep(booking.getId(), SagaStep.COMPLETED);                                                                                                                                                  
          publishDomainEvents(booking);
          log.info("Saga[{}] COMPLETED ?", booking.getId());                                                                                                                                                              
      }                                                                                                                                                                                                                   
   
      // ─── COMPENSATION ─────────────────────────────────────────────────────────                                                                                                                                       
      private void compensate(Booking booking, String reason,
                              boolean refund, boolean voidVoucher) {                                                                                                                                                      
          log.warn("Saga[{}] Starting COMPENSATION: reason={} refund={} voidVoucher={}",
              booking.getId(), reason, refund, voidVoucher);                                                                                                                                                              
   
          booking.startCompensating(reason, clock.instant());                                                                                                                                                             
          bookingRepo.save(booking);
          sagaStateRepo.updateStep(booking.getId(), SagaStep.COMPENSATING);                                                                                                                                               
                  
          // C1: Refund                                                                                                                                                                                                   
          if (refund && booking.getPaymentId() != null) {
              try {                                                                                                                                                                                                       
                  paymentPort.refundPayment(booking.getPaymentId(), reason);
                  log.info("Saga[{}] COMP C1: refund initiated", booking.getId());                                                                                                                                        
              } catch (Exception ex) {
                  // CRITICAL: refund fail → alert, nh?ng ti?p t?c release seats                                                                                                                                          
                  log.error("CRITICAL Saga[{}] COMP C1 FAILED: manual refund needed",                                                                                                                                     
                      booking.getId(), ex);                                                                                                                                                                               
                  // TODO: alert PagerDuty / Grafana alert                                                                                                                                                                
              }                                                                                                                                                                                                           
          }       
                                                                                                                                                                                                                          
          // C2: Release seats
          try {
              showtimePort.releaseSeats(                                                                                                                                                                                  
                  booking.getShowtimeId(), booking.getSeatIds(),
                  booking.getId(), reason                                                                                                                                                                                 
              );  
              log.info("Saga[{}] COMP C2: seats released", booking.getId());
          } catch (Exception ex) {                                                                                                                                                                                        
              // Non-fatal: Redis TTL s? t? expire sau max 10 ph?t
              log.warn("Saga[{}] COMP C2 WARN: ReleaseSeats failed, TTL will expire",                                                                                                                                     
                  booking.getId(), ex);
          }                                                                                                                                                                                                               
                  
          // C3: Void voucher                                                                                                                                                                                             
          if (voidVoucher && booking.hasVoucher()) {
              try {
                  promotionPort.voidVoucher(booking.getAppliedVoucher(), booking.getId());
                  log.info("Saga[{}] COMP C3: voucher voided", booking.getId());                                                                                                                                          
              } catch (Exception ex) {
                  log.warn("Saga[{}] COMP C3 WARN: VoidVoucher failed", booking.getId(), ex);                                                                                                                             
              }                                                                                                                                                                                                           
          }
                                                                                                                                                                                                                          
          booking.cancel(reason, clock.instant());                                                                                                                                                                        
          bookingRepo.save(booking);
          sagaStateRepo.updateStep(booking.getId(), SagaStep.COMPENSATED);                                                                                                                                                
          publishDomainEvents(booking);
          log.info("Saga[{}] COMPENSATION COMPLETE", booking.getId());
      }                                                                                                                                                                                                                   
   
      private void publishDomainEvents(Booking booking) {                                                                                                                                                                 
          var events = booking.pullDomainEvents();
          if (!events.isEmpty()) {                                                                                                                                                                                        
              eventPublisher.publishAll(events);
          }                                                                                                                                                                                                               
      }           
}

? OutboxEventPublisherAdapter.java ― Outbox + Relay:

// infrastructure/messaging/OutboxEventPublisherAdapter.java                                                                                                                                                            
package com.cinetix.booking.infrastructure.messaging;

import com.cinetix.common.domain.DomainEvent;                                                                                                                                                                           
import com.cinetix.booking.domain.port.outbound.EventPublisherPort;
import com.cinetix.outbox.OutboxEvent;                                                                                                                                                                                  
import com.fasterxml.jackson.databind.ObjectMapper;                                                                                                                                                                     
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;                                                                                                                                                                                  
import lombok.extern.slf4j.Slf4j;                                                                                                                                                                                       
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;                                                                                                                                                             
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;                                                                                                                                                                        
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisherAdapter implements EventPublisherPort {

      private static final String EXCHANGE = "cinetix.events";
      private static final int BATCH_SIZE  = 100;                                                                                                                                                                         
      private static final int MAX_RETRIES = 5;
                                                                                                                                                                                                                          
      private final OutboxJpaRepository outboxRepository;
      private final RabbitTemplate      rabbitTemplate;                                                                                                                                                                   
      private final ObjectMapper        objectMapper;
                                                                                                                                                                                                                          
      // Ghi v?o outbox table ― PH?I trong c?ng transaction business                                                                                                                                                      
      @Override                                                                                                                                                                                                           
      @Transactional(propagation = Propagation.MANDATORY)                                                                                                                                                                 
      public void publishAll(List<DomainEvent> events) {
          events.forEach(event -> {                                                                                                                                                                                       
              try {
                  var outboxEvent = OutboxEvent.builder()                                                                                                                                                                 
                      .aggregateType(event.getAggregateType())
                      .aggregateId(event.getAggregateId().toString())                                                                                                                                                     
                      .eventType(event.getEventType())
                      .payload(objectMapper.valueToTree(event))                                                                                                                                                           
                      .metadata(buildMetadata())                                                                                                                                                                          
                      .build();
                  outboxRepository.save(outboxEvent);                                                                                                                                                                     
              } catch (Exception e) {
                  throw new OutboxWriteException(event.getEventType(), e);                                                                                                                                                
              }                                                                                                                                                                                                           
          });                                                                                                                                                                                                             
      }                                                                                                                                                                                                                   
                  
      // Relay job: ??c PENDING outbox records → push sang RabbitMQ                                                                                                                                                       
      // D?ng @DistributedLock ?? ch? 1 pod ch?y relay t?i 1 th?i ?i?m
      @Scheduled(fixedDelayString = "${cinetix.outbox.relay-interval-ms:500}")                                                                                                                                            
      @Transactional                                                                                                                                                                                                      
      public void relay() {                                                                                                                                                                                               
          var pending = outboxRepository.findPendingBatch(BATCH_SIZE);                                                                                                                                                    
          if (pending.isEmpty()) return;
                                                                                                                                                                                                                          
          log.debug("Relaying {} outbox events", pending.size());                                                                                                                                                         
   
          pending.forEach(event -> {                                                                                                                                                                                      
              try {
                  rabbitTemplate.convertAndSend(
                      EXCHANGE,                                                                                                                                                                                           
                      event.getEventType(),   // routing key
                      event.getPayload().toString(),                                                                                                                                                                      
                      msg -> {
                          var props = msg.getMessageProperties();
                          props.setMessageId(event.getId().toString());                                                                                                                                                   
                          props.setContentType("application/json");
                          props.setHeader("eventType", event.getEventType());                                                                                                                                             
                          props.setHeader("aggregateType", event.getAggregateType());                                                                                                                                     
                          props.setHeader("traceId",
                              event.getMetadata().path("traceId").asText(""));                                                                                                                                            
                          return msg;                                                                                                                                                                                     
                      }
                  );                                                                                                                                                                                                      
                  outboxRepository.markProcessed(event.getId(), Instant.now());
                                                                                                                                                                                                                          
              } catch (Exception e) {
                  log.error("Failed to relay outbox event id={} type={}",                                                                                                                                                 
                      event.getId(), event.getEventType(), e);
                  int newRetryCount = outboxRepository.incrementRetry(                                                                                                                                                    
                      event.getId(), e.getMessage());
                                                                                                                                                                                                                          
                  if (newRetryCount >= MAX_RETRIES) {
                      log.error("ALERT: outbox event {} failed {} times, marking FAILED",                                                                                                                                 
                          event.getId(), MAX_RETRIES);                                                                                                                                                                    
                      outboxRepository.markFailed(event.getId(), e.getMessage());
                      // TODO: Micrometer counter → Grafana alert                                                                                                                                                         
                  }                                                                                                                                                                                                       
              }
          });                                                                                                                                                                                                             
      }           

      private ObjectNode buildMetadata() {                                                                                                                                                                                
          var meta = objectMapper.createObjectNode();
          meta.put("traceId",  MDC.get("traceId") != null ? MDC.get("traceId") : "");                                                                                                                                     
          meta.put("spanId",   MDC.get("spanId")  != null ? MDC.get("spanId")  : "");                                                                                                                                     
          meta.put("hostname", System.getenv().getOrDefault("HOSTNAME", "local"));                                                                                                                                        
          return meta;                                                                                                                                                                                                    
      }                                                                                                                                                                                                                   
}

? ShowtimeGrpcAdapter.java ― Port Implementation:

// infrastructure/grpc/ShowtimeGrpcAdapter.java
package com.cinetix.booking.infrastructure.grpc;

import com.cinetix.booking.domain.model.valueobject.*;                                                                                                                                                                  
import com.cinetix.booking.domain.port.outbound.ShowtimePort;
import com.cinetix.booking.domain.port.outbound.ShowtimePort.*;                                                                                                                                                         
import com.cinetix.grpc.showtime.v1.*;                                                                                                                                                                                  
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;                                                                                                                                                                                  
import lombok.extern.slf4j.Slf4j;                                                                                                                                                                                       
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;                                                                                                                                                                                                  
import java.util.Optional;

@Component
@Slf4j
public class ShowtimeGrpcAdapter implements ShowtimePort {

      @GrpcClient("showtime-service")
      private ShowtimeServiceGrpc.ShowtimeServiceBlockingStub showtime;                                                                                                                                                   
                                                                                                                                                                                                                          
      @Override
      public HoldSeatsResult holdSeats(ShowtimeId showtimeId, List<SeatId> seatIds,                                                                                                                                       
                                        BookingId bookingId, int ttlSeconds) {                                                                                                                                            
          try {
              var request = HoldSeatsRequest.newBuilder()                                                                                                                                                                 
                  .setShowtimeId(showtimeId.value())                                                                                                                                                                      
                  .addAllSeatIds(seatIds.stream().map(SeatId::value).toList())
                  .setBookingId(bookingId.value().toString())                                                                                                                                                             
                  .setTtlSeconds(ttlSeconds)                                                                                                                                                                              
                  .setIdempotencyKey(bookingId.value().toString())
                  .build();                                                                                                                                                                                               
                  
              var response = showtime.holdSeats(request);                                                                                                                                                                 
   
              return switch (response.getResultCase()) {                                                                                                                                                                  
                  case SUCCESS -> HoldSeatsResult.success(
                      toInstant(response.getSuccess().getExpiresAt()),                                                                                                                                                    
                      response.getSuccess().getHeldSeatsList().stream()
                          .map(s -> new HeldSeat(s.getSeatId(), s.getSeatCode(), s.getCategory()))                                                                                                                        
                          .toList(),                                                                                                                                                                                      
                      response.getSuccess().getRemainingSeats()                                                                                                                                                           
                  );                                                                                                                                                                                                      
                  case FAILURE -> HoldSeatsResult.failure(
                      response.getFailure().getError().getCode(),                                                                                                                                                         
                      response.getFailure().getUnavailableSeatsList()
                  );                                                                                                                                                                                                      
                  default -> HoldSeatsResult.failure("UNKNOWN_RESPONSE", List.of());
              };                                                                                                                                                                                                          
                  
          } catch (StatusRuntimeException e) {                                                                                                                                                                            
              log.error("gRPC HoldSeats failed: status={} desc={}",
                  e.getStatus().getCode(), e.getStatus().getDescription());                                                                                                                                               
              throw new ShowtimeServiceException("HoldSeats gRPC failed", e);
          }                                                                                                                                                                                                               
      }           
                                                                                                                                                                                                                          
      @Override   
      public void confirmSeats(ShowtimeId showtimeId, List<SeatId> seatIds, BookingId bookingId) {
          try {                                                                                                                                                                                                           
              var request = ConfirmSeatsRequest.newBuilder()
                  .setShowtimeId(showtimeId.value())                                                                                                                                                                      
                  .addAllSeatIds(seatIds.stream().map(SeatId::value).toList())
                  .setBookingId(bookingId.value().toString())                                                                                                                                                             
                  .setIdempotencyKey(bookingId.value().toString())
                  .build();                                                                                                                                                                                               
                                                                                                                                                                                                                          
              var response = showtime.confirmSeats(request);
              if (response.hasError()) {                                                                                                                                                                                  
                  throw new ShowtimeServiceException(
                      "ConfirmSeats failed: " + response.getError().getCode());                                                                                                                                           
              }
          } catch (StatusRuntimeException e) {                                                                                                                                                                            
              throw new ShowtimeServiceException("ConfirmSeats gRPC failed", e);
          }                                                                                                                                                                                                               
      }
                                                                                                                                                                                                                          
      @Override   
      public void releaseSeats(ShowtimeId showtimeId, List<SeatId> seatIds,
                                BookingId bookingId, String reason) {                                                                                                                                                     
          try {
              showtime.releaseSeats(ReleaseSeatsRequest.newBuilder()                                                                                                                                                      
                  .setShowtimeId(showtimeId.value())
                  .addAllSeatIds(seatIds.stream().map(SeatId::value).toList())                                                                                                                                            
                  .setBookingId(bookingId.value().toString())
                  .setReason(reason)                                                                                                                                                                                      
                  .build());
          } catch (StatusRuntimeException e) {
              // Release fail l? non-fatal ― log v? ti?p t?c (Redis TTL s? clean up)                                                                                                                                      
              log.warn("ReleaseSeats gRPC failed (non-fatal): {}", e.getStatus());                                                                                                                                        
          }                                                                                                                                                                                                               
      }                                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      private Instant toInstant(com.google.protobuf.Timestamp ts) {
          return Instant.ofEpochSecond(ts.getSeconds(), ts.getNanos());
      }
}

? BookingController.java ― Inbound REST Adapter:

// adapter/rest/BookingController.java
package com.cinetix.booking.adapter.rest;

import com.cinetix.booking.application.dto.command.CreateBookingCommand;                                                                                                                                                
import com.cinetix.booking.domain.port.inbound.*;
import com.cinetix.booking.adapter.rest.dto.request.CreateBookingRequest;                                                                                                                                               
import com.cinetix.booking.adapter.rest.dto.response.*;
import com.cinetix.booking.adapter.rest.mapper.BookingRestMapper;                                                                                                                                                       
import com.cinetix.common.web.ApiResponse;
import com.cinetix.security.CurrentUser;                                                                                                                                                                                
import jakarta.validation.Valid;                                                                                                                                                                                        
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;                                                                                                                                                                                      
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;                                                                                                                                            
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController                                                                                                                                                                                                         
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingController {

      private final CreateBookingUseCase     createBookingUseCase;                                                                                                                                                        
      private final CancelBookingUseCase     cancelBookingUseCase;
      private final GetBookingUseCase        getBookingUseCase;                                                                                                                                                           
      private final BookingRestMapper        mapper;                                                                                                                                                                      
   
      @PostMapping                                                                                                                                                                                                        
      @ResponseStatus(HttpStatus.ACCEPTED)
      public ApiResponse<CreateBookingResponse> createBooking(
              @Valid @RequestBody CreateBookingRequest request,                                                                                                                                                           
              @AuthenticationPrincipal Jwt jwt) {
                                                                                                                                                                                                                          
          var command = mapper.toCommand(request, extractUserId(jwt));                                                                                                                                                    
          var bookingId = createBookingUseCase.execute(command);
          var booking   = getBookingUseCase.findById(bookingId);                                                                                                                                                          
                                                                                                                                                                                                                          
          return ApiResponse.success(mapper.toCreateResponse(booking));
      }                                                                                                                                                                                                                   
                                                                                                                                                                                                                          
      @GetMapping("/{bookingId}")
      public ApiResponse<BookingDetailResponse> getBooking(                                                                                                                                                               
              @PathVariable UUID bookingId,
              @AuthenticationPrincipal Jwt jwt) {

          var booking = getBookingUseCase.findById(bookingId);                                                                                                                                                            
   
          // Authorization: ch? owner m?i ???c xem                                                                                                                                                                        
          if (!booking.customerId().equals(extractUserId(jwt))) {
              throw new ForbiddenException("Access denied to booking " + bookingId);                                                                                                                                      
          }       
          return ApiResponse.success(mapper.toDetailResponse(booking));                                                                                                                                                   
      }
                                                                                                                                                                                                                          
      @GetMapping("/my")
      public ApiResponse<PagedResponse<BookingSummaryResponse>> listMyBookings(
              @RequestParam(defaultValue = "0")  int page,                                                                                                                                                                
              @RequestParam(defaultValue = "20") int size,
              @RequestParam(required = false)    String status,                                                                                                                                                           
              @AuthenticationPrincipal Jwt jwt) {                                                                                                                                                                         
   
          var userId   = extractUserId(jwt);                                                                                                                                                                              
          var bookings = getBookingUseCase.findByCustomer(userId, status, page, size);
          return ApiResponse.success(mapper.toPagedResponse(bookings));                                                                                                                                                   
      }
                                                                                                                                                                                                                          
      @DeleteMapping("/{bookingId}")                                                                                                                                                                                      
      public ApiResponse<Void> cancelBooking(
              @PathVariable UUID bookingId,                                                                                                                                                                               
              @AuthenticationPrincipal Jwt jwt) {

          cancelBookingUseCase.execute(bookingId, extractUserId(jwt));                                                                                                                                                    
          return ApiResponse.success(null);
      }                                                                                                                                                                                                                   
                  
      @PostMapping("/{bookingId}/apply-voucher")
      public ApiResponse<ApplyVoucherResponse> applyVoucher(
              @PathVariable UUID bookingId,                                                                                                                                                                               
              @Valid @RequestBody ApplyVoucherRequest request,
              @AuthenticationPrincipal Jwt jwt) {                                                                                                                                                                         
                  
          var result = createBookingUseCase.applyVoucher(                                                                                                                                                                 
              bookingId, request.voucherCode(), extractUserId(jwt));
          return ApiResponse.success(mapper.toVoucherResponse(result));                                                                                                                                                   
      }                                                                                                                                                                                                                   
   
      private UUID extractUserId(Jwt jwt) {                                                                                                                                                                               
          // Keycloak sub claim l? UUID
          return UUID.fromString(jwt.getSubject());                                                                                                                                                                       
      }
}

? application.yml ― Booking Service Config:

# booking-service/src/main/resources/application.yml
spring:         
application:
name: booking-service

    datasource:
      url: jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:booking_db}
      username: ${DB_USER:booking_user}                                                                                                                                                                                   
      password: ${DB_PASS:secret}
      hikari:                                                                                                                                                                                                             
        maximum-pool-size: 20
        minimum-idle: 5                                                                                                                                                                                                   
        connection-timeout: 3000
        idle-timeout: 600000
        max-lifetime: 1800000
                                                                                                                                                                                                                          
    jpa:
      hibernate:                                                                                                                                                                                                          
        ddl-auto: validate   # Flyway manages schema ― NEVER auto
      properties:                                                                                                                                                                                                         
        hibernate:
          dialect: org.hibernate.dialect.PostgreSQLDialect                                                                                                                                                                
          jdbc.batch_size: 50
          order_inserts: true
          order_updates: true                                                                                                                                                                                             
   
    flyway:                                                                                                                                                                                                               
      enabled: true
      locations: classpath:db/migration
      validate-on-migrate: true

    rabbitmq:
      host: ${RABBITMQ_HOST:localhost}
      port: ${RABBITMQ_PORT:5672}                                                                                                                                                                                         
      username: ${RABBITMQ_USER:cinetix}
      password: ${RABBITMQ_PASS:secret}                                                                                                                                                                                   
      virtual-host: /cinetix
      listener:                                                                                                                                                                                                           
        simple:
          acknowledge-mode: manual                                                                                                                                                                                        
          prefetch: 10
          retry:                                                                                                                                                                                                          
            enabled: false   # manual ack + custom retry logic
                                                                                                                                                                                                                          
    security:     
      oauth2:                                                                                                                                                                                                             
        resourceserver:
          jwt:
            issuer-uri: ${KEYCLOAK_URL:http://localhost:8180}/realms/cinetix
            jwk-set-uri: ${KEYCLOAK_URL:http://localhost:8180}/realms/cinetix/protocol/openid-connect/certs                                                                                                               

# gRPC client config
grpc:                                                                                                                                                                                                                   
client:       
showtime-service:
address: ${SHOWTIME_GRPC_ADDRESS:static://localhost:9083}
negotiation-type: plaintext   # TLS khi production (Istio mTLS)                                                                                                                                                   
payment-service:                                                                                                                                                                                                    
address: ${PAYMENT_GRPC_ADDRESS:static://localhost:9086}                                                                                                                                                          
negotiation-type: plaintext                                                                                                                                                                                       
promotion-service:                                                                                                                                                                                                  
address: ${PROMOTION_GRPC_ADDRESS:static://localhost:9088}
negotiation-type: plaintext

# Resilience4j
resilience4j:   
circuitbreaker:
instances:
showtime-service:
sliding-window-size: 10                                                                                                                                                                                         
failure-rate-threshold: 50
wait-duration-in-open-state: 30s                                                                                                                                                                                
permitted-number-of-calls-in-half-open-state: 3
payment-service:                                                                                                                                                                                                  
sliding-window-size: 5
failure-rate-threshold: 40                                                                                                                                                                                      
wait-duration-in-open-state: 60s
retry:                                                                                                                                                                                                                
instances:
showtime-hold:                                                                                                                                                                                                    
max-attempts: 3
wait-duration: 100ms
enable-exponential-backoff: true
exponential-backoff-multiplier: 2.0

# Outbox config
cinetix:        
outbox:                                                                                                                                                                                                               
relay-interval-ms: 500
batch-size: 100
max-retries: 5

# Observability
management:                                                                                                                                                                                                             
endpoints:    
web:
exposure:
include: health,info,prometheus,metrics
metrics:
export:
prometheus:
enabled: true                                                                                                                                                                                                   
tracing:
sampling:                                                                                                                                                                                                           
probability: 1.0   # 100% trong dev; 0.1 (10%) trong production

logging:
pattern:                                                                                                                                                                                                              
console: "%d{ISO8601} [%thread] %-5level [traceId=%X{traceId}] %logger{36} - %msg%n"
                                                                                                                                                                                                                          
---
? AggregateRoot.java ― Shared Base Class (cinetix-common lib):

// libs/cinetix-common/.../domain/AggregateRoot.java
package com.cinetix.common.domain;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;                                                                                                                                                                                   
import java.util.*;

@MappedSuperclass                                                                                                                                                                                                       
public abstract class AggregateRoot<ID> {

      @Transient
      private final List<DomainEvent> domainEvents = new ArrayList<>();
                                                                                                                                                                                                                          
      public abstract ID getId();
                                                                                                                                                                                                                          
      protected void registerEvent(DomainEvent event) {                                                                                                                                                                   
          Objects.requireNonNull(event, "Domain event cannot be null");
          domainEvents.add(event);                                                                                                                                                                                        
      }                                                                                                                                                                                                                   
   
      /**                                                                                                                                                                                                                 
       * Pull events v? clear buffer.
       * G?i sau khi save aggregate ?? tr?nh duplicate publish.
       */                                                                                                                                                                                                                 
      public List<DomainEvent> pullDomainEvents() {
          var copy = List.copyOf(domainEvents);                                                                                                                                                                           
          domainEvents.clear();                                                                                                                                                                                           
          return copy;
      }                                                                                                                                                                                                                   
                  
      public boolean hasDomainEvents() {
          return !domainEvents.isEmpty();
      }                                                                                                                                                                                                                   
}

// ── DomainEvent interface ─────────────────────────────────────────────────────                                                                                                                                       
public interface DomainEvent {
UUID getEventId();                                                                                                                                                                                                  
String getEventType();       // "booking.events.confirmed.v1"
String getAggregateType();   // "Booking"                                                                                                                                                                           
Object getAggregateId();     // BookingId
Instant getOccurredAt();                                                                                                                                                                                            
String getVersion();         // "v1"
}

// ── BookingConfirmedEvent example ─────────────────────────────────────────────                                                                                                                                       
public record BookingConfirmedEvent(
UUID     eventId,                                                                                                                                                                                                   
BookingId aggregateId,
CustomerId customerId,                                                                                                                                                                                              
ShowtimeId showtimeId,
List<SeatId> seatIds,                                                                                                                                                                                               
Money    finalAmount,
VoucherCode voucher,                                                                                                                                                                                                
Instant  confirmedAt
) implements DomainEvent {

      public static BookingConfirmedEvent from(Booking booking, Instant at) {                                                                                                                                             
          return new BookingConfirmedEvent(
              UUID.randomUUID(),                                                                                                                                                                                          
              booking.getId(),
              booking.getCustomerId(),
              booking.getShowtimeId(),
              booking.getSeatIds(),                                                                                                                                                                                       
              booking.getFinalAmount(),
              booking.getAppliedVoucher(),                                                                                                                                                                                
              at  
          );
      }

      @Override public UUID    getEventId()       { return eventId; }                                                                                                                                                     
      @Override public String  getEventType()     { return "booking.events.confirmed.v1"; }
      @Override public String  getAggregateType() { return "Booking"; }                                                                                                                                                   
      @Override public Object  getAggregateId()   { return aggregateId; }                                                                                                                                                 
      @Override public Instant getOccurredAt()    { return confirmedAt; }
      @Override public String  getVersion()       { return "v1"; }                                                                                                                                                        
}
   
---                                                                                                                                                                                                                     
? Spring Modulith ― Enforce Module Boundaries:

// booking-service/src/test/java/com/cinetix/booking/ModularityTest.java
package com.cinetix.booking;

import org.junit.jupiter.api.Test;                                                                                                                                                                                      
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

class ModularityTest {

      // ApplicationModules detect modules b?ng package structure                                                                                                                                                         
      static ApplicationModules modules =
          ApplicationModules.of(BookingServiceApplication.class);                                                                                                                                                         
                                                                                                                                                                                                                          
      @Test
      void verifyModuleStructure() {                                                                                                                                                                                      
          // Fail n?u c? circular dependency ho?c module access violation
          modules.verify();                                                                                                                                                                                               
      }
                                                                                                                                                                                                                          
      @Test       
      void generateDocumentation() {
          new Documenter(modules)
              .writeModulesAsPlantUml()                                                                                                                                                                                   
              .writeIndividualModulesAsPlantUml();
      }                                                                                                                                                                                                                   
}

// K?t qu?: Spring Modulith s? c?nh b?o n?u:                                                                                                                                                                            
// - domain/ import t? infrastructure/ (KH?NG ???c)
// - application/ import tr?c ti?p JPA entities (KH?NG ???c ― ph?i qua port)                                                                                                                                            
// - 2 modules import l?n nhau (circular dependency)
                                                                                                                                                                                                                          
---                                                                                                                                                                                                                     
Phase 6 ho?n th?nh ― to?n b? code structure, Hexagonal Architecture, v? production-ready code m?u ?? ?? ?? b?t ??u implement. G? "phase 7" ?? t?i thi?t k? Deployment & DevOps: Dockerfile, docker-compose, Helm charts,
K8s manifests, v? GitHub Actions CI/CD pipelines.

? Saut?ed for 2m 46s

? phase 7

● PHASE 7 ― DEPLOYMENT & DEVOPS

7.1 Dockerfile ― Multi-Stage Build

GraalVM Native Image ― C? n?n d?ng kh?ng?

┌─────────────────────┬──────────────────────────┬──────────────────────────┐                                                                                                                                           
│                     │ JVM Layered JAR (JRE 21) │ GraalVM Native Image     │
├─────────────────────┼──────────────────────────┼──────────────────────────┤                                                                                                                                           
│ Startup time        │ 3-8 gi?y                 │ 50-200ms ?              │
│ Memory usage        │ 256-512MB                │ 64-128MB ?              │                                                                                                                                           ─
│ Build time CI       │ 2-3 ph?t ?              │ 8-15 ph?t  ??             │
│ Debug/profiling     │ Full JVM tools ?         │ Limited  ??               │                                                                                                                                          
│ Reflection/dynamic  │ Full support ?           │ C?n hints file  ??        │                                                                                                                                          
│ Spring Boot support │ Excellent ?              │ Good (3.x+) ?           │                                                                                                                                            
│ Testcontainers      │ ? Easy                   │  ?? Complex               │                                                                                                                                          
│ JPA / Hibernate     │ ? Perfect                │  ?? C?n c?u h?nh th?m    │                                                                                                                                           
│ gRPC                │ ?                        │ ─?? Protobuf reflection   │                                                                                                                                          
└─────────────────────┴──────────────────────────┴──────────────────────────┘

Khuy?n ngh?: JVM Layered JAR cho MVP v? production hi?n t?i.                                                                                                                                                            
L? do: booking-service d?ng nhi?u reflection (JPA, Jackson, gRPC protobuf),                                                                                                                                             
GraalVM c?n hints ph?c t?p. Startup time 5s v?i K8s readiness probe                                                                                                                                              
l? ho?n to?n ch?p nh?n ???c. Revisit khi h? th?ng stable.

Dockerfile ― Production-Optimized:

# ???????????????????????????????????????????????????????????????????????????????
# Stage 1: Build
# ???????????????????????????????????????????????????????????????????????????????
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /workspace

# Cache Maven dependencies ri?ng (layer n?y ch? rebuild khi pom.xml thay ??i)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./                                                                                                                                                                                                    
COPY libs/cinetix-common/pom.xml       libs/cinetix-common/
COPY libs/cinetix-security/pom.xml     libs/cinetix-security/                                                                                                                                                           
COPY libs/cinetix-outbox/pom.xml       libs/cinetix-outbox/
COPY libs/cinetix-grpc-stubs/pom.xml   libs/cinetix-grpc-stubs/                                                                                                                                                         
COPY services/booking-service/pom.xml  services/booking-service/

# Download dependencies (cached n?u pom kh?ng ??i)
RUN ./mvnw dependency:go-offline -pl services/booking-service \                                                                                                                                                         
-am -q --no-transfer-progress

# Copy source v? build
COPY libs/             libs/                                                                                                                                                                                            
COPY services/booking-service/ services/booking-service/
COPY proto/            proto/

RUN ./mvnw package -pl services/booking-service \                                                                                                                                                                       
-am -DskipTests --no-transfer-progress -q

# Extract layered JAR (Spring Boot layered jar feature)
RUN java -Djarmode=layertools \
-jar services/booking-service/target/booking-service-*.jar \                                                                                                                                                        
extract --destination /workspace/extracted

# ???????????????????????????????????????????????????????????????????????????????
# Stage 2: Runtime ― minimal JRE image
# ???????????????????????????????????????????????????????????????????????????????
FROM eclipse-temurin:21-jre-alpine AS runtime

# Security: ch?y v?i non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup                                                                                                                                                              
USER appuser

WORKDIR /app

# Copy layers theo th? t? ?t thay ??i → nhi?u thay ??i nh?t
# Docker cache s? reuse layers kh?ng ??i
COPY --from=builder --chown=appuser:appgroup \                                                                                                                                                                          
/workspace/extracted/dependencies/          ./                                                                                                                                                                      
COPY --from=builder --chown=appuser:appgroup \                                                                                                                                                                          
/workspace/extracted/spring-boot-loader/    ./                                                                                                                                                                      
COPY --from=builder --chown=appuser:appgroup \                                                                                                                                                                          
/workspace/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup \                                                                                                                                                                          
/workspace/extracted/application/           ./

# Health check (K8s c?ng t? check nh?ng docker-compose c?n)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
CMD wget -qO- http://localhost:8082/actuator/health || exit 1

EXPOSE 8082 9082

# JVM flags t?i ?u cho container
ENTRYPOINT ["java", \
"-XX:+UseContainerSupport", \                                                                                                                                                                                       
"-XX:MaxRAMPercentage=75.0", \
"-XX:InitialRAMPercentage=50.0", \
"-XX:+OptimizeStringConcat", \                                                                                                                                                                                      
"-XX:+UseG1GC", \
"-XX:MaxGCPauseMillis=200", \                                                                                                                                                                                       
"-Djava.security.egd=file:/dev/./urandom", \
"-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:docker}", \                                                                                                                                                      
"org.springframework.boot.loader.launch.JarLauncher"]

.dockerignore:

.git
.idea
**/target/                                                                                                                                                                                                              
**/*.md
**/node_modules/                                                                                                                                                                                                        
infrastructure/k8s/
infrastructure/helm/                                                                                                                                                                                                    
.github/
**/*.log
                  
---                                                                                                                                                                                                                     
7.2 docker-compose.yml ― Full Dev Local Stack

# infrastructure/docker/docker-compose.yml
version: "3.9"

# ─── Networks ─────────────────────────────────────────────────────────────────
networks:                                                                                                                                                                                                               
cinetix-net:                                                                                                                                                                                                          
driver: bridge
ipam:                                                                                                                                                                                                               
config:   
- subnet: 172.20.0.0/16

# ─── Volumes ──────────────────────────────────────────────────────────────────
volumes:
postgres_identity:   {}                                                                                                                                                                                               
postgres_movie:      {}
postgres_cinema:     {}
postgres_showtime:   {}
postgres_booking:    {}
postgres_payment:    {}                                                                                                                                                                                               
postgres_ticket:     {}
postgres_promotion:  {}                                                                                                                                                                                               
postgres_notif:      {}
postgres_chat:       {}
postgres_keycloak:   {}                                                                                                                                                                                               
redis_data:          {}
rabbitmq_data:       {}                                                                                                                                                                                               
minio_data:          {}

# ???????????????????????????????????????????????????????????????????????????????
# INFRASTRUCTURE SERVICES
# ???????????????????????????????????????????????????????????????????????????????
services:

    # ── PostgreSQL instances (1 per service) ────────────────────────────────────                                                                                                                                        
    # Dev: d?ng chung 1 Postgres instance, m?i service 1 database ri?ng
    # Production: m?i service 1 Postgres instance ri?ng tr?n K8s                                                                                                                                                          
    postgres:                                                                                                                                                                                                             
      image: postgres:16-alpine                                                                                                                                                                                           
      container_name: cinetix-postgres                                                                                                                                                                                    
      environment:
        POSTGRES_USER:     postgres
        POSTGRES_PASSWORD: postgres                                                                                                                                                                                       
        POSTGRES_MULTIPLE_DATABASES: >
          identity_db,movie_db,cinema_db,showtime_db,                                                                                                                                                                     
          booking_db,payment_db,ticket_db,promotion_db,                                                                                                                                                                   
          notif_db,chat_db,keycloak_db
      volumes:                                                                                                                                                                                                            
        - postgres_booking:/var/lib/postgresql/data
        - ./init-scripts/create-databases.sh:/docker-entrypoint-initdb.d/init.sh                                                                                                                                          
      ports:                                                                                                                                                                                                              
        - "5432:5432"                                                                                                                                                                                                     
      networks:                                                                                                                                                                                                           
        - cinetix-net
      healthcheck:
        test: ["CMD-SHELL", "pg_isready -U postgres"]
        interval: 10s                                                                                                                                                                                                     
        timeout: 5s
        retries: 5                                                                                                                                                                                                        
                  
    # ── Redis ────────────────────────────────────────────────────────────────────
    redis:
      image: redis:7-alpine
      container_name: cinetix-redis                                                                                                                                                                                       
      command: >
        redis-server                                                                                                                                                                                                      
        --requirepass redis_secret
        --appendonly yes
        --maxmemory 512mb
        --maxmemory-policy allkeys-lru                                                                                                                                                                                    
      volumes:
        - redis_data:/data                                                                                                                                                                                                
      ports:      
        - "6379:6379"
      networks:
        - cinetix-net
      healthcheck:
        test: ["CMD", "redis-cli", "-a", "redis_secret", "ping"]                                                                                                                                                          
        interval: 10s
        timeout: 3s                                                                                                                                                                                                       
        retries: 5

    # ── RabbitMQ ─────────────────────────────────────────────────────────────────                                                                                                                                       
    rabbitmq:
      image: rabbitmq:3.13-management-alpine                                                                                                                                                                              
      container_name: cinetix-rabbitmq
      environment:
        RABBITMQ_DEFAULT_USER: cinetix                                                                                                                                                                                    
        RABBITMQ_DEFAULT_PASS: cinetix_secret
        RABBITMQ_DEFAULT_VHOST: /cinetix                                                                                                                                                                                  
      volumes:                                                                                                                                                                                                            
        - rabbitmq_data:/var/lib/rabbitmq
        - ./rabbitmq/definitions.json:/etc/rabbitmq/definitions.json                                                                                                                                                      
        - ./rabbitmq/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf                                                                                                                                                            
      ports:
        - "5672:5672"                                                                                                                                                                                                     
        - "15672:15672"   # Management UI
        - "61613:61613"   # STOMP plugin                                                                                                                                                                                  
      networks:
        - cinetix-net                                                                                                                                                                                                     
      healthcheck:
        test: ["CMD", "rabbitmq-diagnostics", "ping"]
        interval: 10s                                                                                                                                                                                                     
        timeout: 5s
        retries: 10                                                                                                                                                                                                       
                  
    # ── Keycloak ──────────────────────────────────────────────────────────────────                                                                                                                                      
    keycloak:
      image: quay.io/keycloak/keycloak:25.0                                                                                                                                                                               
      container_name: cinetix-keycloak
      command: start-dev --import-realm
      environment:                                                                                                                                                                                                        
        KC_DB:               postgres
        KC_DB_URL:           jdbc:postgresql://postgres:5432/keycloak_db                                                                                                                                                  
        KC_DB_USERNAME:      postgres                                                                                                                                                                                     
        KC_DB_PASSWORD:      postgres
        KEYCLOAK_ADMIN:      admin                                                                                                                                                                                        
        KEYCLOAK_ADMIN_PASSWORD: admin
        KC_HTTP_PORT:        8180
        KC_HOSTNAME_STRICT:  false                                                                                                                                                                                        
        KC_HTTP_ENABLED:     true
      volumes:                                                                                                                                                                                                            
        - ./keycloak/cinetix-realm.json:/opt/keycloak/data/import/realm.json
      ports:                                                                                                                                                                                                              
        - "8180:8180"
      depends_on:                                                                                                                                                                                                         
        postgres: 
          condition: service_healthy                                                                                                                                                                                      
      networks:
        - cinetix-net                                                                                                                                                                                                     
                  
    # ── MinIO (S3-compatible object storage) ─────────────────────────────────────                                                                                                                                       
    minio:
      image: minio/minio:latest                                                                                                                                                                                           
      container_name: cinetix-minio
      command: server /data --console-address ":9001"
      environment:
        MINIO_ROOT_USER:     minio_admin
        MINIO_ROOT_PASSWORD: minio_secret                                                                                                                                                                                 
      volumes:
        - minio_data:/data                                                                                                                                                                                                
      ports:      
        - "9000:9000"                                                                                                                                                                                                     
        - "9001:9001"
      networks:                                                                                                                                                                                                           
        - cinetix-net

    # ── Debezium Connect ──────────────────────────────────────────────────────────                                                                                                                                      
    debezium:
      image: debezium/connect:2.7                                                                                                                                                                                         
      container_name: cinetix-debezium
      environment:
        BOOTSTRAP_SERVERS:           rabbitmq:5672
        GROUP_ID:                    cinetix-debezium                                                                                                                                                                     
        CONFIG_STORAGE_TOPIC:        debezium_configs
        OFFSET_STORAGE_TOPIC:        debezium_offsets                                                                                                                                                                     
        STATUS_STORAGE_TOPIC:        debezium_status
        CONNECT_KEY_CONVERTER:       org.apache.kafka.connect.storage.StringConverter                                                                                                                                     
        CONNECT_VALUE_CONVERTER:     org.apache.kafka.connect.json.JsonConverter
        # D?ng RabbitMQ Sink Connector                                                                                                                                                                                    
        CONNECT_PLUGIN_PATH:         /kafka/connect
      ports:                                                                                                                                                                                                              
        - "8083:8083"
      depends_on:                                                                                                                                                                                                         
        - postgres                                                                                                                                                                                                        
        - rabbitmq
      networks:                                                                                                                                                                                                           
        - cinetix-net

# ???????????????????????????????????????????????????????????????????????????????
# APPLICATION SERVICES
# ???????????????????????????????????????????????????????????????????????????????

    # ── API Gateway ───────────────────────────────────────────────────────────────                                                                                                                                      
    api-gateway:  
      build:                                                                                                                                                                                                              
        context: ../../
        dockerfile: services/api-gateway/Dockerfile
      container_name: cinetix-api-gateway
      environment:                                                                                                                                                                                                        
        SPRING_PROFILES_ACTIVE: docker
        KEYCLOAK_URL:           http://keycloak:8180                                                                                                                                                                      
        BOOKING_SERVICE_URL:    http://booking-service:8082
        SHOWTIME_SERVICE_URL:   http://showtime-service:8083                                                                                                                                                              
        MOVIE_SERVICE_URL:      http://movie-service:8084
        CINEMA_SERVICE_URL:     http://cinema-service:8085                                                                                                                                                                
        PAYMENT_SERVICE_URL:    http://payment-service:8086
        TICKET_SERVICE_URL:     http://ticket-service:8087                                                                                                                                                                
        PROMOTION_SERVICE_URL:  http://promotion-service:8088
        NOTIF_SERVICE_URL:      http://notification-service:8089                                                                                                                                                          
        CHAT_SERVICE_URL:       http://chat-service:8090
      ports:                                                                                                                                                                                                              
        - "8080:8080"
      depends_on:                                                                                                                                                                                                         
        keycloak:
          condition: service_started                                                                                                                                                                                      
      networks:   
        - cinetix-net
      restart: unless-stopped

    # ── Booking Service ───────────────────────────────────────────────────────────                                                                                                                                      
    booking-service:
      build:                                                                                                                                                                                                              
        context: ../../
        dockerfile: services/booking-service/Dockerfile
      container_name: cinetix-booking
      environment:                                                                                                                                                                                                        
        SPRING_PROFILES_ACTIVE: docker
        DB_HOST:                postgres                                                                                                                                                                                  
        DB_NAME:                booking_db
        DB_USER:                postgres
        DB_PASS:                postgres
        REDIS_HOST:             redis                                                                                                                                                                                     
        REDIS_PORT:             6379
        REDIS_PASSWORD:         redis_secret                                                                                                                                                                              
        RABBITMQ_HOST:          rabbitmq
        RABBITMQ_USER:          cinetix
        RABBITMQ_PASS:          cinetix_secret
        KEYCLOAK_URL:           http://keycloak:8180                                                                                                                                                                      
        SHOWTIME_GRPC_ADDRESS:  static://showtime-service:9083
        PAYMENT_GRPC_ADDRESS:   static://payment-service:9086                                                                                                                                                             
        PROMOTION_GRPC_ADDRESS: static://promotion-service:9088
        JAVA_OPTS:              "-Xms256m -Xmx512m"                                                                                                                                                                       
      ports:      
        - "8082:8082"                                                                                                                                                                                                     
        - "9082:9082"
      depends_on:                                                                                                                                                                                                         
        postgres:
          condition: service_healthy                                                                                                                                                                                      
        redis:    
          condition: service_healthy
        rabbitmq:
          condition: service_healthy
      networks:
        - cinetix-net
      restart: unless-stopped                                                                                                                                                                                             
      healthcheck:
        test: ["CMD-SHELL",                                                                                                                                                                                               
               "wget -qO- http://localhost:8082/actuator/health | grep UP || exit 1"]
        interval: 30s                                                                                                                                                                                                     
        timeout: 10s
        start_period: 60s                                                                                                                                                                                                 
        retries: 3

    # ── Showtime Service ──────────────────────────────────────────────────────────                                                                                                                                      
    showtime-service:
      build:                                                                                                                                                                                                              
        context: ../../
        dockerfile: services/showtime-service/Dockerfile
      container_name: cinetix-showtime
      environment:                                                                                                                                                                                                        
        SPRING_PROFILES_ACTIVE: docker
        DB_HOST:                postgres                                                                                                                                                                                  
        DB_NAME:                showtime_db
        DB_USER:                postgres
        DB_PASS:                postgres
        REDIS_HOST:             redis
        REDIS_PORT:             6379
        REDIS_PASSWORD:         redis_secret                                                                                                                                                                              
        RABBITMQ_HOST:          rabbitmq
        RABBITMQ_USER:          cinetix                                                                                                                                                                                   
        RABBITMQ_PASS:          cinetix_secret
        KEYCLOAK_URL:           http://keycloak:8180
      ports:                                                                                                                                                                                                              
        - "8083:8083"
        - "9083:9083"                                                                                                                                                                                                     
      depends_on: 
        postgres:
          condition: service_healthy
        redis:
          condition: service_healthy
      networks:                                                                                                                                                                                                           
        - cinetix-net
      restart: unless-stopped                                                                                                                                                                                             
                  
    # (T??ng t? cho movie, cinema, payment, ticket, promotion, notification, chat)                                                                                                                                        
    # ?? ng?n g?n ― xem file docker-compose.full.yml trong repo

# ???????????????????????????????????????????????????????????????????????????????
# OBSERVABILITY STACK (t?ch sang file ri?ng cho clarity)
# ???????????????????????????????????????????????????????????????????????????????
    otel-collector:
      image: otel/opentelemetry-collector-contrib:0.104.0                                                                                                                                                                 
      container_name: cinetix-otel                                                                                                                                                                                        
      volumes:
        - ./observability/otel-config.yaml:/etc/otelcol/config.yaml                                                                                                                                                       
      ports:                                                                                                                                                                                                              
        - "4317:4317"   # gRPC
        - "4318:4318"   # HTTP                                                                                                                                                                                            
        - "8888:8888"   # metrics                                                                                                                                                                                         
      networks:
        - cinetix-net                                                                                                                                                                                                     
                  
    prometheus:
      image: prom/prometheus:v2.53.0
      container_name: cinetix-prometheus                                                                                                                                                                                  
      volumes:
        - ./observability/prometheus.yml:/etc/prometheus/prometheus.yml                                                                                                                                                   
      ports:      
        - "9090:9090"
      networks:                                                                                                                                                                                                           
        - cinetix-net
                                                                                                                                                                                                                          
    grafana:      
      image: grafana/grafana:11.1.0
      container_name: cinetix-grafana
      environment:
        GF_SECURITY_ADMIN_PASSWORD: admin
        GF_FEATURE_TOGGLES_ENABLE:  traceqlEditor                                                                                                                                                                         
      volumes:
        - ./observability/grafana/dashboards:/var/lib/grafana/dashboards                                                                                                                                                  
        - ./observability/grafana/provisioning:/etc/grafana/provisioning
      ports:                                                                                                                                                                                                              
        - "3000:3000"
      depends_on:                                                                                                                                                                                                         
        - prometheus
      networks:                                                                                                                                                                                                           
        - cinetix-net

    loki:
      image: grafana/loki:3.1.0
      container_name: cinetix-loki
      volumes:                                                                                                                                                                                                            
        - ./observability/loki-config.yaml:/etc/loki/config.yaml
      ports:                                                                                                                                                                                                              
        - "3100:3100"
      networks:
        - cinetix-net
                                                                                                                                                                                                                          
    tempo:
      image: grafana/tempo:2.5.0                                                                                                                                                                                          
      container_name: cinetix-tempo
      volumes:
        - ./observability/tempo-config.yaml:/etc/tempo/config.yaml
      ports:
        - "3200:3200"                                                                                                                                                                                                     
        - "9411:9411"   # Zipkin compat
      networks:                                                                                                                                                                                                           
        - cinetix-net
                                                                                                                                                                                                                          
---             
7.3 Helm Chart ― booking-service

infrastructure/helm/charts/booking-service/
├── Chart.yaml                                                                                                                                                                                                          
├── values.yaml
├── values-staging.yaml                                                                                                                                                                                                 
├── values-production.yaml
└── templates/
├── _helpers.tpl                                                                                                                                                                                                    
├── deployment.yaml
├── service.yaml                                                                                                                                                                                                    
├── hpa.yaml
├── ingress.yaml
├── configmap.yaml
├── secret.yaml
├── serviceaccount.yaml                                                                                                                                                                                             
├── poddisruptionbudget.yaml
└── NOTES.txt

Chart.yaml:

apiVersion: v2                                                                                                                                                                                                          
name: booking-service
description: CinemaTix Booking Service
type: application
version: 0.1.0                                                                                                                                                                                                          
appVersion: "1.0.0"
dependencies:                                                                                                                                                                                                           
- name: common
version: "0.1.x"
repository: "file://../../_common"

values.yaml:

# infrastructure/helm/charts/booking-service/values.yaml
replicaCount: 2

image:                                                                                                                                                                                                                  
repository: ghcr.io/cinetix/booking-service
pullPolicy: IfNotPresent
tag: ""   # Overridden by CI/CD via --set image.tag=<git-sha>

imagePullSecrets:
- name: ghcr-credentials

serviceAccount:
create: true
name: "booking-service"
annotations: {}

# ─── Service ──────────────────────────────────────────────────────────────────
service:        
type: ClusterIP                                                                                                                                                                                                       
httpPort: 8082
grpcPort: 9082                                                                                                                                                                                                        
annotations:  
prometheus.io/scrape: "true"
prometheus.io/port:   "8082"
prometheus.io/path:   "/actuator/prometheus"

# ─── Ingress (th??ng kh?ng expose tr?c ti?p ― qua API Gateway) ───────────────
ingress:        
enabled: false                                                                                                                                                                                                        
className: nginx
annotations:
nginx.ingress.kubernetes.io/rewrite-target: /
hosts:
- host: api.cinetix.vn
paths:                                                                                                                                                                                                            
- path: /api/v1/bookings
pathType: Prefix                                                                                                                                                                                              
tls:          
- secretName: cinetix-tls
hosts:                                                                                                                                                                                                            
- api.cinetix.vn

# ─── Resources ────────────────────────────────────────────────────────────────
resources:
requests:                                                                                                                                                                                                             
cpu:    250m
memory: 512Mi
limits:
cpu:    1000m
memory: 1Gi

# ─── HPA ──────────────────────────────────────────────────────────────────────
autoscaling:
enabled: true                                                                                                                                                                                                         
minReplicas: 2
maxReplicas: 10
targetCPUUtilizationPercentage:    70
targetMemoryUtilizationPercentage: 80                                                                                                                                                                                 
behavior:
scaleUp:                                                                                                                                                                                                            
stabilizationWindowSeconds: 60
policies:                                                                                                                                                                                                         
- type:          Pods
value:         2                                                                                                                                                                                              
periodSeconds: 60
scaleDown:
stabilizationWindowSeconds: 300   # 5 ph?t ?n ??nh tr??c khi scale down
policies:                                                                                                                                                                                                         
- type:          Pods
value:         1                                                                                                                                                                                              
periodSeconds: 120

# ─── Pod settings ─────────────────────────────────────────────────────────────
podAnnotations:                                                                                                                                                                                                         
sidecar.istio.io/inject: "true"

podSecurityContext:
runAsNonRoot: true                                                                                                                                                                                                    
runAsUser:    1000
fsGroup:      1000

securityContext:
allowPrivilegeEscalation: false                                                                                                                                                                                       
readOnlyRootFilesystem:   true
capabilities:
drop: ["ALL"]

# ─── Probes ───────────────────────────────────────────────────────────────────
livenessProbe:
httpGet:                                                                                                                                                                                                              
path: /actuator/health/liveness
port: 8082
initialDelaySeconds: 60                                                                                                                                                                                               
periodSeconds:       30
failureThreshold:    3                                                                                                                                                                                                
timeoutSeconds:      5

readinessProbe:
httpGet:
path: /actuator/health/readiness                                                                                                                                                                                    
port: 8082
initialDelaySeconds: 30                                                                                                                                                                                               
periodSeconds:       10
failureThreshold:    3
timeoutSeconds:      3

startupProbe:                                                                                                                                                                                                           
httpGet:
path: /actuator/health                                                                                                                                                                                              
port: 8082  
initialDelaySeconds: 10
periodSeconds:       10
failureThreshold:    12    # 12 × 10s = 2 ph?t ?? start

# ─── Environment variables ────────────────────────────────────────────────────
env:                                                                                                                                                                                                                    
SPRING_PROFILES_ACTIVE:  kubernetes
JAVA_TOOL_OPTIONS:       "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

envFromConfigMap:
- booking-service-config

envFromSecret:
- booking-service-secrets

# ─── Volumes ──────────────────────────────────────────────────────────────────
# tmpfs cho readOnlyRootFilesystem
extraVolumes:                                                                                                                                                                                                           
- name: tmp   
emptyDir: {}                                                                                                                                                                                                        
- name: logs  
emptyDir: {}

extraVolumeMounts:
- name: tmp
mountPath: /tmp                                                                                                                                                                                                     
- name: logs
mountPath: /app/logs

# ─── Pod Disruption Budget ────────────────────────────────────────────────────
podDisruptionBudget:
enabled: true
minAvailable: 1

# ─── Node affinity ────────────────────────────────────────────────────────────
nodeSelector: {}
tolerations:  []                                                                                                                                                                                                        
affinity:
podAntiAffinity:                                                                                                                                                                                                      
preferredDuringSchedulingIgnoredDuringExecution:
- weight: 100                                                                                                                                                                                                     
podAffinityTerm:
labelSelector:                                                                                                                                                                                                
matchExpressions:
- key:      app
operator: In
values:   ["booking-service"]
topologyKey: kubernetes.io/hostname   # Spread across nodes

# ─── Graceful shutdown ────────────────────────────────────────────────────────
lifecycle:      
preStop:                                                                                                                                                                                                              
exec:       
command: ["/bin/sh", "-c", "sleep 10"]   # Cho LB drain connections

terminationGracePeriodSeconds: 60

templates/deployment.yaml:

# templates/deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
name: {{ include "booking-service.fullname" . }}
labels:                                                                                                                                                                                                               
{{- include "booking-service.labels" . | nindent 4 }}
spec:                                                                                                                                                                                                                   
{{- if not .Values.autoscaling.enabled }}
replicas: {{ .Values.replicaCount }}
{{- end }}                                                                                                                                                                                                            
strategy:
type: RollingUpdate                                                                                                                                                                                                 
rollingUpdate:
maxSurge:       1
maxUnavailable: 0   # Zero-downtime deployment
selector:                                                                                                                                                                                                             
matchLabels:
{{- include "booking-service.selectorLabels" . | nindent 6 }}                                                                                                                                                     
template:     
metadata:                                                                                                                                                                                                           
annotations:
# Force pod restart khi configmap/secret thay ??i                                                                                                                                                               
checksum/config: {{ include (print $.Template.BasePath "/configmap.yaml") . | sha256sum }}
checksum/secret: {{ include (print $.Template.BasePath "/secret.yaml")    . | sha256sum }}                                                                                                                      
{{- with .Values.podAnnotations }}                                                                                                                                                                              
{{- toYaml . | nindent 8 }}                                                                                                                                                                                     
{{- end }}                                                                                                                                                                                                      
labels:   
{{- include "booking-service.selectorLabels" . | nindent 8 }}
version: {{ .Values.image.tag | default .Chart.AppVersion | quote }}                                                                                                                                            
spec:
serviceAccountName: {{ include "booking-service.serviceAccountName" . }}                                                                                                                                          
securityContext:                                                                                                                                                                                                  
{{- toYaml .Values.podSecurityContext | nindent 8 }}
terminationGracePeriodSeconds: {{ .Values.terminationGracePeriodSeconds }}

        containers:
          - name: {{ .Chart.Name }}                                                                                                                                                                                       
            securityContext:
              {{- toYaml .Values.securityContext | nindent 12 }}
            image: "{{ .Values.image.repository }}:{{ .Values.image.tag | default .Chart.AppVersion }}"                                                                                                                   
            imagePullPolicy: {{ .Values.image.pullPolicy }}
            ports:                                                                                                                                                                                                        
              - name: http
                containerPort: 8082
                protocol:      TCP                                                                                                                                                                                        
              - name: grpc
                containerPort: 9082                                                                                                                                                                                       
                protocol:      TCP
            envFrom:
              - configMapRef:
                  name: {{ .Values.envFromConfigMap | first }}
              - secretRef:                                                                                                                                                                                                
                  name: {{ .Values.envFromSecret | first }}
            env:                                                                                                                                                                                                          
              {{- range $key, $val := .Values.env }}
              - name:  {{ $key }}                                                                                                                                                                                         
                value: {{ $val | quote }}
              {{- end }}                                                                                                                                                                                                  
              # Pod metadata injection
              - name:  POD_NAME                                                                                                                                                                                           
                valueFrom:
                  fieldRef:                                                                                                                                                                                               
                    fieldPath: metadata.name
              - name:  POD_NAMESPACE
                valueFrom:                                                                                                                                                                                                
                  fieldRef:
                    fieldPath: metadata.namespace                                                                                                                                                                         
              - name:  NODE_NAME
                valueFrom:
                  fieldRef:
                    fieldPath: spec.nodeName
            resources:
              {{- toYaml .Values.resources | nindent 12 }}
            livenessProbe:                                                                                                                                                                                                
              {{- toYaml .Values.livenessProbe | nindent 12 }}
            readinessProbe:                                                                                                                                                                                               
              {{- toYaml .Values.readinessProbe | nindent 12 }}
            startupProbe:                                                                                                                                                                                                 
              {{- toYaml .Values.startupProbe | nindent 12 }}
            lifecycle:                                                                                                                                                                                                    
              {{- toYaml .Values.lifecycle | nindent 12 }}
            volumeMounts:                                                                                                                                                                                                 
              {{- toYaml .Values.extraVolumeMounts | nindent 12 }}
        volumes:                                                                                                                                                                                                          
          {{- toYaml .Values.extraVolumes | nindent 8 }}
        {{- with .Values.nodeSelector }}                                                                                                                                                                                  
        nodeSelector:
          {{- toYaml . | nindent 8 }}                                                                                                                                                                                     
        {{- end }}
        {{- with .Values.affinity }}
        affinity:
          {{- toYaml . | nindent 8 }}
        {{- end }}                                                                                                                                                                                                        

templates/configmap.yaml + secret.yaml:

# templates/configmap.yaml
apiVersion: v1                                                                                                                                                                                                          
kind: ConfigMap
metadata:                                                                                                                                                                                                               
name: booking-service-config
labels:
{{- include "booking-service.labels" . | nindent 4 }}
data:                                                                                                                                                                                                                   
DB_HOST:                 "postgres-booking.cinetix.svc.cluster.local"
DB_PORT:                 "5432"                                                                                                                                                                                       
DB_NAME:                 "booking_db"
REDIS_HOST:              "redis-master.cinetix.svc.cluster.local"                                                                                                                                                     
REDIS_PORT:              "6379"                                                                                                                                                                                       
RABBITMQ_HOST:           "rabbitmq.cinetix.svc.cluster.local"
RABBITMQ_PORT:           "5672"                                                                                                                                                                                       
RABBITMQ_VHOST:          "/cinetix"                                                                                                                                                                                   
KEYCLOAK_URL:            "http://keycloak.cinetix.svc.cluster.local:8180"
SHOWTIME_GRPC_ADDRESS:   "dns:///showtime-service.cinetix.svc.cluster.local:9083"                                                                                                                                     
PAYMENT_GRPC_ADDRESS:    "dns:///payment-service.cinetix.svc.cluster.local:9086"                                                                                                                                      
PROMOTION_GRPC_ADDRESS:  "dns:///promotion-service.cinetix.svc.cluster.local:9088"                                                                                                                                    
OTEL_EXPORTER_OTLP_ENDPOINT: "http://otel-collector.monitoring.svc.cluster.local:4317"                                                                                                                                
OTEL_SERVICE_NAME:       "booking-service"
OTEL_RESOURCE_ATTRIBUTES: "deployment.environment=production"

# templates/secret.yaml
# Trong production: d?ng External Secrets Operator + AWS Secrets Manager
# Ho?c HashiCorp Vault + Vault Agent Injector
apiVersion: v1                                                                                                                                                                                                          
kind: Secret                                                                                                                                                                                                            
metadata:                                                                                                                                                                                                               
name: booking-service-secrets
labels:
{{- include "booking-service.labels" . | nindent 4 }}                                                                                                                                                               
type: Opaque
# Secrets KH?NG hardcode ? ??y ― inject t? CI/CD ho?c External Secrets
# values.yaml ch?a placeholder, Helm --set override khi deploy
stringData:                                                                                                                                                                                                             
DB_USER:            {{ .Values.secrets.dbUser        | default "" | quote }}                                                                                                                                          
DB_PASS:            {{ .Values.secrets.dbPass        | default "" | quote }}                                                                                                                                          
REDIS_PASSWORD:     {{ .Values.secrets.redisPassword | default "" | quote }}
RABBITMQ_USER:      {{ .Values.secrets.mqUser        | default "" | quote }}                                                                                                                                          
RABBITMQ_PASS:      {{ .Values.secrets.mqPass        | default "" | quote }}
   
---                                                                                                                                                                                                                     
7.4 K8s Infrastructure Manifests

PostgreSQL StatefulSet (per-service pattern):

# infrastructure/k8s/infra/postgres/booking-postgres.yaml
apiVersion: apps/v1                                                                                                                                                                                                     
kind: StatefulSet
metadata:                                                                                                                                                                                                               
name:      postgres-booking
namespace: cinetix
spec:                                                                                                                                                                                                                   
serviceName: postgres-booking
replicas: 1    # Primary only ― d?ng CloudSQL/RDS n?u c?n HA                                                                                                                                                          
selector:                                                                                                                                                                                                             
matchLabels:
app: postgres-booking                                                                                                                                                                                             
template:                                                                                                                                                                                                             
metadata:
labels:                                                                                                                                                                                                           
app: postgres-booking
spec:
securityContext:
fsGroup: 999
containers:
- name: postgres
image: postgres:16-alpine                                                                                                                                                                                     
env:
- name:  POSTGRES_DB                                                                                                                                                                                        
value: booking_db
- name:  POSTGRES_USER
valueFrom:
secretKeyRef:
name: postgres-booking-secret                                                                                                                                                                         
key:  username
- name:  POSTGRES_PASSWORD                                                                                                                                                                                  
valueFrom:
secretKeyRef:
name: postgres-booking-secret
key:  password                                                                                                                                                                                        
# Debezium c?n WAL level = logical
- name:  POSTGRES_INITDB_ARGS                                                                                                                                                                               
value: "--wal-level=logical"                                                                                                                                                                              
args:
- "-c"                                                                                                                                                                                                      
- "wal_level=logical"
- "-c"
- "max_replication_slots=10"                                                                                                                                                                                
- "-c"
- "max_wal_senders=10"                                                                                                                                                                                      
- "-c"
- "shared_preload_libraries=pg_stat_statements"
ports:                                                                                                                                                                                                        
- containerPort: 5432
resources:                                                                                                                                                                                                    
requests:
cpu:    500m
memory: 1Gi
limits:
cpu:    2000m
memory: 4Gi                                                                                                                                                                                               
volumeMounts:
- name: postgres-data                                                                                                                                                                                       
mountPath: /var/lib/postgresql/data
- name: postgres-config
mountPath: /etc/postgresql/postgresql.conf                                                                                                                                                                
subPath: postgresql.conf
readinessProbe:                                                                                                                                                                                               
exec:
command: ["pg_isready", "-U", "postgres"]
initialDelaySeconds: 5                                                                                                                                                                                      
periodSeconds: 10
livenessProbe:                                                                                                                                                                                                
exec:
command: ["pg_isready", "-U", "postgres"]
initialDelaySeconds: 30
periodSeconds: 15                                                                                                                                                                                           
volumes:
- name: postgres-config                                                                                                                                                                                         
configMap:
name: postgres-booking-config
volumeClaimTemplates:
- metadata:                                                                                                                                                                                                         
name: postgres-data
spec:                                                                                                                                                                                                             
accessModes: ["ReadWriteOnce"]
storageClassName: standard-rwo   # GKE standard; d?ng gp3 tr?n EKS
resources:                                                                                                                                                                                                      
requests:
storage: 20Gi
                  
---
apiVersion: v1
kind: Service
metadata:
name:      postgres-booking
namespace: cinetix
spec:                                                                                                                                                                                                                   
selector:
app: postgres-booking                                                                                                                                                                                               
ports:        
- port:       5432
targetPort: 5432
clusterIP: None   # Headless service cho StatefulSet

RabbitMQ via Operator:

# infrastructure/k8s/infra/rabbitmq/rabbitmq-cluster.yaml
# D?ng RabbitMQ Cluster Operator (khuy?n ngh? h?n t? manage StatefulSet)
apiVersion: rabbitmq.com/v1beta1                                                                                                                                                                                        
kind: RabbitmqCluster                                                                                                                                                                                                   
metadata:                                                                                                                                                                                                               
name:      cinetix-rabbitmq
namespace: cinetix                                                                                                                                                                                                    
spec:           
replicas: 3
image: rabbitmq:3.13-management
resources:                                                                                                                                                                                                            
requests:
cpu:    500m                                                                                                                                                                                                      
memory: 1Gi
limits:
cpu:    2000m
memory: 2Gi
rabbitmq:                                                                                                                                                                                                             
additionalPlugins:
- rabbitmq_management                                                                                                                                                                                             
- rabbitmq_prometheus
- rabbitmq_shovel
- rabbitmq_stomp          # Cho WebSocket STOMP                                                                                                                                                                   
additionalConfig: |
default_vhost = /cinetix                                                                                                                                                                                          
# Message persistence
vm_memory_high_watermark.relative = 0.7                                                                                                                                                                           
# DLQ configuration
dead_letter_exchange = cinetix.dlx                                                                                                                                                                                
persistence:                                                                                                                                                                                                          
storageClassName: standard-rwo
storage:          10Gi                                                                                                                                                                                              
affinity:                                                                                                                                                                                                             
podAntiAffinity:
requiredDuringSchedulingIgnoredDuringExecution:                                                                                                                                                                   
- labelSelector:
matchLabels:
app.kubernetes.io/name: cinetix-rabbitmq                                                                                                                                                                  
topologyKey: kubernetes.io/hostname
                                                                                                                                                                                                                          
---             
# RabbitMQ exchanges, queues, bindings (via definition import)
apiVersion: rabbitmq.com/v1beta1                                                                                                                                                                                        
kind: Exchange                                                                                                                                                                                                          
metadata:                                                                                                                                                                                                               
name:      cinetix-events                                                                                                                                                                                             
namespace: cinetix
spec:
name:       cinetix.events
type:       topic
durable:    true
rabbitmqClusterReference:                                                                                                                                                                                             
name: cinetix-rabbitmq
                                                                                                                                                                                                                          
---             
apiVersion: rabbitmq.com/v1beta1
kind: Queue
metadata:
name:      booking-payment-events
namespace: cinetix                                                                                                                                                                                                    
spec:
name:    booking-svc.payment.events                                                                                                                                                                                   
durable: true                                                                                                                                                                                                         
arguments:
x-dead-letter-exchange:    cinetix.dlx                                                                                                                                                                              
x-dead-letter-routing-key: booking-svc.payment.events.dlq
x-message-ttl:             1800000   # 30 ph?t                                                                                                                                                                      
rabbitmqClusterReference:
name: cinetix-rabbitmq
                  
---                                                                                                                                                                                                                     
apiVersion: rabbitmq.com/v1beta1
kind: Binding
metadata:
name:      booking-payment-binding
namespace: cinetix                                                                                                                                                                                                    
spec:
source:          cinetix.events                                                                                                                                                                                       
destination:     booking-svc.payment.events
destinationType: queue
routingKey:      "payment.events.#"
rabbitmqClusterReference:                                                                                                                                                                                             
name: cinetix-rabbitmq

Redis via Helm (Bitnami):

# infrastructure/helm/charts/redis/values-override.yaml
# helm install redis bitnami/redis -f values-override.yaml
architecture: replication

auth:                                                                                                                                                                                                                   
enabled:  true
password: "redis_secret"

master:
count: 1
resources:
requests:
cpu:    200m
memory: 256Mi                                                                                                                                                                                                     
limits:
cpu:    1000m                                                                                                                                                                                                     
memory: 1Gi
persistence:
size: 8Gi

replica:
replicaCount: 2                                                                                                                                                                                                       
resources:    
requests:
cpu:    100m
memory: 128Mi

metrics:
enabled:          true
serviceMonitor:                                                                                                                                                                                                       
enabled:        true
namespace:      monitoring
                  
---                                                                                                                                                                                                                     
7.5 GitHub Actions CI/CD

ci.yml ― Build, Test, Scan:

# .github/workflows/ci.yml
name: CI Pipeline

on:                                                                                                                                                                                                                     
push:         
branches: [main, develop, "feature/**", "fix/**"]
pull_request:
branches: [main, develop]

env:
JAVA_VERSION: "21"                                                                                                                                                                                                    
REGISTRY:     ghcr.io
IMAGE_PREFIX: ghcr.io/${{ github.repository_owner }}/cinetix

jobs:
# ─── Detect changed services ───────────────────────────────────────────────                                                                                                                                         
detect-changes:                                                                                                                                                                                                       
runs-on: ubuntu-latest
outputs:                                                                                                                                                                                                            
services: ${{ steps.filter.outputs.services }}
matrix:   ${{ steps.set-matrix.outputs.matrix }}                                                                                                                                                                  
steps:
- uses: actions/checkout@v4                                                                                                                                                                                       
- uses: dorny/paths-filter@v3                                                                                                                                                                                     
id: filter
with:                                                                                                                                                                                                           
filters: |
booking-service:
- 'services/booking-service/**'
- 'libs/**'                                                                                                                                                                                               
showtime-service:
- 'services/showtime-service/**'                                                                                                                                                                          
- 'libs/**'
payment-service:                                                                                                                                                                                            
- 'services/payment-service/**'
- 'libs/**'                                                                                                                                                                                               
# ... other services

        - id: set-matrix
          run: |
            SERVICES=()                                                                                                                                                                                                   
            if [[ "${{ steps.filter.outputs.booking-service }}" == "true" ]]; then
              SERVICES+=("booking-service")                                                                                                                                                                               
            fi                                                                                                                                                                                                            
            if [[ "${{ steps.filter.outputs.showtime-service }}" == "true" ]]; then
              SERVICES+=("showtime-service")                                                                                                                                                                              
            fi    
            if [ ${#SERVICES[@]} -eq 0 ]; then
              SERVICES+=("none")                                                                                                                                                                                          
            fi
            echo "matrix=$(printf '%s\n' "${SERVICES[@]}" | jq -R . | jq -sc .)" \                                                                                                                                        
              >> $GITHUB_OUTPUT                                                                                                                                                                                           
   
    # ─── Build & Unit Test ─────────────────────────────────────────────────────                                                                                                                                         
    build-and-test:                                                                                                                                                                                                       
      needs: detect-changes
      if: ${{ needs.detect-changes.outputs.matrix != '["none"]' }}                                                                                                                                                        
      runs-on: ubuntu-latest                                                                                                                                                                                              
      strategy:
        matrix:                                                                                                                                                                                                           
          service: ${{ fromJson(needs.detect-changes.outputs.matrix) }}
        fail-fast: false                                                                                                                                                                                                  
   
      steps:                                                                                                                                                                                                              
        - uses: actions/checkout@v4

        - uses: actions/setup-java@v4
          with:
            java-version:     ${{ env.JAVA_VERSION }}                                                                                                                                                                     
            distribution:     temurin
            cache:            maven                                                                                                                                                                                       
                  
        - name: Build & Unit Test
          run: |
            ./mvnw test \
              -pl services/${{ matrix.service }} -am \                                                                                                                                                                    
              --no-transfer-progress \
              -Dspring.profiles.active=test                                                                                                                                                                               
                                                                                                                                                                                                                          
        - name: Upload coverage to Codecov
          uses: codecov/codecov-action@v4                                                                                                                                                                                 
          with:                                                                                                                                                                                                           
            file: services/${{ matrix.service }}/target/site/jacoco/jacoco.xml
            flags: ${{ matrix.service }}                                                                                                                                                                                  
                  
    # ─── Integration Tests (Testcontainers) ────────────────────────────────────                                                                                                                                         
    integration-test:
      needs: build-and-test                                                                                                                                                                                               
      runs-on: ubuntu-latest                                                                                                                                                                                              
      strategy:
        matrix:                                                                                                                                                                                                           
          service: ${{ fromJson(needs.detect-changes.outputs.matrix) }}

      services:                                                                                                                                                                                                           
        # Testcontainers t? qu?n l? containers ― kh?ng c?n service definitions ? ??y
        # Ch? c?n Docker socket access                                                                                                                                                                                    
                  
      steps:                                                                                                                                                                                                              
        - uses: actions/checkout@v4
        - uses: actions/setup-java@v4
          with:
            java-version:  ${{ env.JAVA_VERSION }}                                                                                                                                                                        
            distribution:  temurin
            cache:         maven                                                                                                                                                                                          
                  
        - name: Integration Tests
          run: |
            ./mvnw verify \
              -pl services/${{ matrix.service }} -am \                                                                                                                                                                    
              --no-transfer-progress \
              -P integration-test \                                                                                                                                                                                       
              -Dspring.profiles.active=test
          env:                                                                                                                                                                                                            
            TESTCONTAINERS_RYUK_DISABLED: true
                                                                                                                                                                                                                          
    # ─── SAST ― SonarQube ──────────────────────────────────────────────────────                                                                                                                                         
    sast:
      needs: build-and-test                                                                                                                                                                                               
      runs-on: ubuntu-latest
      if: github.event_name == 'pull_request'
      steps:                                                                                                                                                                                                              
        - uses: actions/checkout@v4
          with:                                                                                                                                                                                                           
            fetch-depth: 0   # SonarQube c?n full history
                                                                                                                                                                                                                          
        - uses: actions/setup-java@v4
          with:                                                                                                                                                                                                           
            java-version: ${{ env.JAVA_VERSION }}
            distribution: temurin
            cache:         maven
                                                                                                                                                                                                                          
        - name: SonarQube Scan
          run: |                                                                                                                                                                                                          
            ./mvnw sonar:sonar \
              --no-transfer-progress \
              -Dsonar.host.url=${{ secrets.SONAR_URL }} \
              -Dsonar.login=${{ secrets.SONAR_TOKEN }} \                                                                                                                                                                  
              -Dsonar.pullrequest.key=${{ github.event.pull_request.number }} \
              -Dsonar.pullrequest.branch=${{ github.head_ref }} \                                                                                                                                                         
              -Dsonar.pullrequest.base=${{ github.base_ref }}
                                                                                                                                                                                                                          
    # ─── Build Docker Images ───────────────────────────────────────────────────
    docker-build:                                                                                                                                                                                                         
      needs: [build-and-test, integration-test]
      runs-on: ubuntu-latest                                                                                                                                                                                              
      if: github.ref == 'refs/heads/main' || github.ref == 'refs/heads/develop'
      strategy:                                                                                                                                                                                                           
        matrix:   
          service: ${{ fromJson(needs.detect-changes.outputs.matrix) }}
                                                                                                                                                                                                                          
      permissions:
        contents: read                                                                                                                                                                                                    
        packages: write

      steps:
        - uses: actions/checkout@v4

        - uses: docker/setup-buildx-action@v3                                                                                                                                                                             
   
        - uses: docker/login-action@v3                                                                                                                                                                                    
          with:   
            registry: ${{ env.REGISTRY }}
            username: ${{ github.actor }}
            password: ${{ secrets.GITHUB_TOKEN }}
                                                                                                                                                                                                                          
        - name: Docker meta
          id: meta                                                                                                                                                                                                        
          uses: docker/metadata-action@v5
          with:
            images: ${{ env.IMAGE_PREFIX }}/${{ matrix.service }}
            tags: |                                                                                                                                                                                                       
              type=sha,prefix=sha-,format=short
              type=ref,event=branch                                                                                                                                                                                       
              type=raw,value=latest,enable=${{ github.ref == 'refs/heads/main' }}                                                                                                                                         
   
        - name: Build & Push                                                                                                                                                                                              
          uses: docker/build-push-action@v6
          with:
            context:   .
            file:      services/${{ matrix.service }}/Dockerfile                                                                                                                                                          
            push:      true
            tags:      ${{ steps.meta.outputs.tags }}                                                                                                                                                                     
            labels:    ${{ steps.meta.outputs.labels }}
            cache-from: type=gha,scope=${{ matrix.service }}
            cache-to:   type=gha,scope=${{ matrix.service }},mode=max                                                                                                                                                     
            build-args: |
              BUILD_DATE=${{ github.event.head_commit.timestamp }}                                                                                                                                                        
              GIT_COMMIT=${{ github.sha }}                                                                                                                                                                                
   
        - name: Container Security Scan (Trivy)                                                                                                                                                                           
          uses: aquasecurity/trivy-action@master                                                                                                                                                                          
          with:
            image-ref: ${{ env.IMAGE_PREFIX }}/${{ matrix.service }}:sha-${{ github.sha }}                                                                                                                                
            format:    sarif                                                                                                                                                                                              
            output:    trivy-results.sarif
            severity:  CRITICAL,HIGH                                                                                                                                                                                      
            exit-code: 1   # Fail CI n?u c? CRITICAL vulnerability
                                                                                                                                                                                                                          
        - name: Upload Trivy results
          uses: github/codeql-action/upload-sarif@v3                                                                                                                                                                      
          with:                                                                                                                                                                                                           
            sarif_file: trivy-results.sarif

release.yml ― Tag Release + Bump Chart Version:

# .github/workflows/release.yml
name: Release

on:
push:
tags:
- "v*.*.*"   # Semantic versioning: v1.2.3

env:
REGISTRY:     ghcr.io                                                                                                                                                                                                 
IMAGE_PREFIX: ghcr.io/${{ github.repository_owner }}/cinetix

jobs:
release:                                                                                                                                                                                                              
runs-on: ubuntu-latest
permissions:
contents: write
packages: write

      steps:                                                                                                                                                                                                              
        - uses: actions/checkout@v4
          with:                                                                                                                                                                                                           
            fetch-depth: 0

        - name: Extract version
          id: version
          run: echo "VERSION=${GITHUB_REF#refs/tags/v}" >> $GITHUB_OUTPUT
                                                                                                                                                                                                                          
        - uses: docker/setup-buildx-action@v3
        - uses: docker/login-action@v3                                                                                                                                                                                    
          with:                                                                                                                                                                                                           
            registry: ${{ env.REGISTRY }}
            username: ${{ github.actor }}                                                                                                                                                                                 
            password: ${{ secrets.GITHUB_TOKEN }}

        # Build & push t?t c? services v?i version tag                                                                                                                                                                    
        - name: Build & Push All Services
          run: |                                                                                                                                                                                                          
            SERVICES=(
              booking-service showtime-service movie-service                                                                                                                                                              
              cinema-service payment-service ticket-service
              promotion-service notification-service chat-service                                                                                                                                                         
              api-gateway identity-service                                                                                                                                                                                
            )
            for SERVICE in "${SERVICES[@]}"; do                                                                                                                                                                           
              docker buildx build \
                --platform linux/amd64,linux/arm64 \                                                                                                                                                                      
                -f services/$SERVICE/Dockerfile \
                -t "${{ env.IMAGE_PREFIX }}/$SERVICE:${{ steps.version.outputs.VERSION }}" \                                                                                                                              
                -t "${{ env.IMAGE_PREFIX }}/$SERVICE:latest" \                                                                                                                                                            
                --push \
                --cache-from type=gha,scope=$SERVICE \                                                                                                                                                                    
                .                                                                                                                                                                                                         
            done
                                                                                                                                                                                                                          
        # Update Helm chart versions (GitOps)
        - name: Bump Helm Chart Versions                                                                                                                                                                                  
          run: |
            VERSION=${{ steps.version.outputs.VERSION }}                                                                                                                                                                  
            for CHART in infrastructure/helm/charts/*/; do
              SERVICE=$(basename $CHART)                                                                                                                                                                                  
              # Update appVersion trong Chart.yaml
              sed -i "s/^appVersion:.*/appVersion: \"${VERSION}\"/" $CHART/Chart.yaml                                                                                                                                     
              # Update image tag trong values.yaml                                                                                                                                                                        
              sed -i "s/^  tag:.*/  tag: \"${VERSION}\"/" $CHART/values.yaml
            done                                                                                                                                                                                                          
                  
        - name: Commit Chart Updates                                                                                                                                                                                      
          uses: stefanzweifel/git-auto-commit-action@v5                                                                                                                                                                   
          with:
            commit_message: "chore: bump chart versions to ${{ steps.version.outputs.VERSION }}"                                                                                                                          
            branch:         main                                                                                                                                                                                          
            file_pattern:   "infrastructure/helm/**"
                                                                                                                                                                                                                          
        # Generate Release Notes t? Conventional Commits                                                                                                                                                                  
        - name: Generate Release Notes
          id: release-notes                                                                                                                                                                                               
          uses: orhun/git-cliff-action@v3
          with:                                                                                                                                                                                                           
            config: cliff.toml
            args:   --current --strip header                                                                                                                                                                              
                                                                                                                                                                                                                          
        - name: Create GitHub Release
          uses: softprops/action-gh-release@v2                                                                                                                                                                            
          with:                                                                                                                                                                                                           
            body:      ${{ steps.release-notes.outputs.content }}
            draft:     false                                                                                                                                                                                              
            prerelease: ${{ contains(github.ref, '-rc') || contains(github.ref, '-beta') }}

ArgoCD ApplicationSet:

# infrastructure/k8s/argocd/applicationset.yaml
apiVersion: argoproj.io/v1alpha1
kind: ApplicationSet
metadata:
name:      cinetix-services                                                                                                                                                                                           
namespace: argocd
spec:                                                                                                                                                                                                                   
generators:   
- list:
elements:
- service:     booking-service
namespace:   cinetix                                                                                                                                                                                        
environment: production
- service:     showtime-service                                                                                                                                                                               
namespace:   cinetix
environment: production
- service:     payment-service
namespace:   cinetix                                                                                                                                                                                        
environment: production
- service:     movie-service                                                                                                                                                                                  
namespace:   cinetix
environment: production
- service:     cinema-service
namespace:   cinetix
environment: production
- service:     ticket-service
namespace:   cinetix                                                                                                                                                                                        
environment: production
- service:     promotion-service                                                                                                                                                                              
namespace:   cinetix
environment: production
- service:     notification-service
namespace:   cinetix
environment: production
- service:     chat-service                                                                                                                                                                                   
namespace:   cinetix
environment: production                                                                                                                                                                                     
- service:     api-gateway
namespace:   cinetix
environment: production
- service:     identity-service
namespace:   cinetix
environment: production

    template:
      metadata:                                                                                                                                                                                                           
        name: "cinetix-{{service}}"
        labels:
          environment: "{{environment}}"
        annotations:                                                                                                                                                                                                      
          # Slack notification khi sync
          notifications.argoproj.io/subscribe.on-sync-succeeded.slack: cinetix-deploys                                                                                                                                    
          notifications.argoproj.io/subscribe.on-sync-failed.slack:    cinetix-alerts                                                                                                                                     
      spec:                                                                                                                                                                                                               
        project: cinetix                                                                                                                                                                                                  
                                                                                                                                                                                                                          
        source:
          repoURL:        https://github.com/your-org/cinetix                                                                                                                                                             
          targetRevision: main
          path:           infrastructure/helm/charts/{{service}}
          helm:                                                                                                                                                                                                           
            releaseName: "{{service}}"
            valueFiles:                                                                                                                                                                                                   
              - values.yaml
              - values-production.yaml                                                                                                                                                                                    
            # Secrets inject t? Vault (External Secrets)
            # Kh?ng hardcode secrets trong Git                                                                                                                                                                            
                  
        destination:                                                                                                                                                                                                      
          server:    https://kubernetes.default.svc
          namespace: "{{namespace}}"
                                                                                                                                                                                                                          
        syncPolicy:
          automated:                                                                                                                                                                                                      
            prune:    true    # X?a resources kh?ng c?n trong Git
            selfHeal: true    # Auto-revert manual changes                                                                                                                                                                
          syncOptions:
            - CreateNamespace=true                                                                                                                                                                                        
            - PrunePropagationPolicy=foreground
            - RespectIgnoreDifferences=true                                                                                                                                                                               
          retry:
            limit: 3                                                                                                                                                                                                      
            backoff:
              duration:    5s
              factor:      2                                                                                                                                                                                              
              maxDuration: 3m
                                                                                                                                                                                                                          
        # Ignore HPA replicas (managed by HPA, kh?ng c?n sync)                                                                                                                                                            
        ignoreDifferences:
          - group:       apps                                                                                                                                                                                             
            kind:        Deployment
            jsonPointers:                                                                                                                                                                                                 
              - /spec/replicas
          - group:       autoscaling                                                                                                                                                                                      
            kind:        HorizontalPodAutoscaler
            jsonPointers:
              - /spec/metrics

  ---                                                                                                                                                                                                                     
# ArgoCD Project v?i RBAC
apiVersion: argoproj.io/v1alpha1                                                                                                                                                                                        
kind: AppProject
metadata:
name:      cinetix
namespace: argocd
spec:                                                                                                                                                                                                                   
description: CinemaTix Platform
sourceRepos:                                                                                                                                                                                                          
- "https://github.com/your-org/cinetix"
destinations:                                                                                                                                                                                                         
- namespace: cinetix
server:    https://kubernetes.default.svc                                                                                                                                                                         
- namespace: monitoring
server:    https://kubernetes.default.svc
clusterResourceWhitelist:
- group: ""                                                                                                                                                                                                         
kind:  Namespace
namespaceResourceWhitelist:                                                                                                                                                                                           
- group: "*"
kind:  "*"                                                                                                                                                                                                        
roles:
- name:        developer                                                                                                                                                                                            
description: Read-only access
policies:                                                                                                                                                                                                         
- p, proj:cinetix:developer, applications, get, cinetix/*, allow
- p, proj:cinetix:developer, applications, sync, cinetix/*, deny                                                                                                                                                
- name:        deployer                                                                                                                                                                                             
description: Can sync applications                                                                                                                                                                                
policies:                                                                                                                                                                                                         
- p, proj:cinetix:deployer, applications, *, cinetix/*, allow

cliff.toml ― Conventional Commits Release Notes:

# cliff.toml
[changelog]     
header = """
# Changelog\n
All notable changes to CinemaTix.\n
"""
body = """                                                                                                                                                                                                              
{% if version %}\
## [{{ version | trim_start_matches(pat="v") }}] - {{ timestamp | date(format="%Y-%m-%d") }}
{% else %}\
## [Unreleased]
{% endif %}\                                                                                                                                                                                                            
{% for group, commits in commits | group_by(attribute="group") %}
### {{ group | striptags | trim | upper_first }}
{% for commit in commits %}
- {% if commit.scope %}**{{ commit.scope }}**: {% endif %}{{ commit.message }}\
  {% endfor %}                                                                                                                                                                                                            
  {% endfor %}\n  
  """

[git]
conventional_commits = true
commit_parsers = [
{ message = "^feat",     group = "Features" },                                                                                                                                                                        
{ message = "^fix",      group = "Bug Fixes" },                                                                                                                                                                       
{ message = "^perf",     group = "Performance" },                                                                                                                                                                     
{ message = "^refactor", group = "Refactoring" },                                                                                                                                                                     
{ message = "^chore",    group = "Miscellaneous", skip = true },
]                                                                                                                                                                                                                       
filter_commits = true
tag_pattern    = "v[0-9]*"
                  
---                                                                                                                                                                                                                     
?? Nh?ng ?i?m b?n c?n l?u ?:

1. Zero-downtime deployment v?i RollingUpdate:
   maxUnavailable: 0  → Kh?ng bao gi? c? pod unavailable trong qu? tr?nh deploy                                                                                                                                            
   maxSurge: 1        → T?o 1 pod m?i tr??c khi terminate pod c?               
   preStop sleep 10s  → Cho load balancer drain connections tr??c khi terminate                                                                                                                                            
   terminationGracePeriod: 60s → Spring Boot 60s ?? finish in-flight requests

2. Secret Management ― Kh?ng commit secrets v?o Git:                                                                                                                                                                    
   Option 1: External Secrets Operator + AWS Secrets Manager                                                                                                                                                               
   → ESO t? sync secrets t? AWS v?o K8s Secret objects

Option 2: HashiCorp Vault + Vault Agent Injector                                                                                                                                                                        
→ Vault sidecar inject secrets v?o pod filesystem

Option 3: Sealed Secrets (Bitnami)                                                                                                                                                                                      
→ Encrypt secrets b?ng cluster public key, safe to commit

Khuy?n ngh?: External Secrets Operator cho GKE/EKS (native cloud integration)

3. Database Migration trong K8s:                                                                                                                                                                                        
   D?ng Flyway migrate ch?y nh? Init Container tr??c khi app start:

initContainers:                                                                                                                                                                                                         
- name: flyway-migrate
image: flyway/flyway:10-alpine                                                                                                                                                                                      
command: ["flyway", "migrate"]
env:                          
- name: FLYWAY_URL                                                                                                                                                                                                
value: jdbc:postgresql://postgres-booking:5432/booking_db
# ??m b?o migration ho?n th?nh tr??c khi main container start

4. Multi-arch image (Apple Silicon M1/M2 dev):                                                                                                                                                                          
   --platform linux/amd64,linux/arm64                                                                                                                                                                                      
   Docker buildx t?o manifest list ― c?ng 1 tag ch?y tr?n c? x86 v? ARM.                                                                                                                                                   
   Quan tr?ng n?u team dev d?ng Mac M1/M2.

  ---                                                                                                                                                                                                                     
Phase 7 ho?n th?nh ― to?n b? DevOps pipeline t? Dockerfile ??n ArgoCD GitOps ?? s?n s?ng. G? "phase 8" ?? t?i l?n Roadmap 8 tu?n v?i sprint plan, deliverables, v? Definition of Done cho t?ng sprint.

? Churned for 2m 38s

──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
??                                                                                                                                                                                                                        
──────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────
? for shortcuts                                                                                                                                                                                           ? In READ.MD

D?ng Flyway migrate ch?y nh? Init Container tr??c khi app start:

initContainers:
- name: flyway-migrate
image: flyway/flyway:10-alpine
command: ["flyway", "migrate"]
env:
- name: FLYWAY_URL
value: jdbc:postgresql://postgres-booking:5432/booking_db
# ??m b?o migration ho?n th?nh tr??c khi main container start

4. Multi-arch image (Apple Silicon M1/M2 dev):
   --platform linux/amd64,linux/arm64
   Docker buildx t?o manifest list ― c?ng 1 tag ch?y tr?n c? x86 v? ARM.
   Quan tr?ng n?u team dev d?ng Mac M1/M2.

  ---
Phase 7 ho?n th?nh ― to?n b? DevOps pipeline t? Dockerfile ??n ArgoCD GitOps ?? s?n s?ng. G? "phase 8" ?? t?i l?n Roadmap 8 tu?n v?i sprint plan, deliverables, v? Definition of Done cho t?ng sprint.
                                                                                                                                                                                                                        