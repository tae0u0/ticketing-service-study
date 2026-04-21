# 이벤트 기반 티켓팅 시스템

대규모 트래픽 환경에서 좌석 선점과 결제 정합성을 보장하는 이벤트 기반 티켓팅 시스템입니다.

## 서비스 포트

| 서비스 | 포트 | 역할 |
|---|---|---|
| user-service | 8081 | 회원가입/로그인/JWT 발급 |
| event-service | 8082 | 공연/좌석 관리, Redis 캐시 |
| reservation-service | 8083 | 좌석 임시 선점 (Redis 분산락 + Outbox) |
| payment-service | 8084 | 결제 처리 (멱등성 + Outbox) |
| booking-service | 8085 | 예매 확정 (Kafka Consumer + 멱등성) |
| notification-service | 8086 | 알림 발송 (Kafka Consumer) |
| Kafka UI | 8090 | Kafka 토픽/메시지 모니터링 |
| Prometheus | 9090 | 메트릭 수집 |
| Grafana | 3000 | 메트릭 시각화 (admin/admin1234) |

## 실행 방법

### 1. 인프라 실행

```bash
docker-compose up -d
```

MySQL, Redis, Kafka, Prometheus, Grafana가 실행됩니다.

### 2. 각 서비스 빌드

```bash
./gradlew build -x test
```

### 3. 서비스 실행 (각 터미널에서)

```bash
# Terminal 1
./gradlew :user-service:bootRun

# Terminal 2
./gradlew :event-service:bootRun

# Terminal 3
./gradlew :reservation-service:bootRun

# Terminal 4
./gradlew :payment-service:bootRun

# Terminal 5
./gradlew :booking-service:bootRun

# Terminal 6
./gradlew :notification-service:bootRun
```

## API 사용 예시

### 1. 회원가입

```bash
curl -X POST http://localhost:8081/api/v1/users/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123","name":"홍길동","phone":"010-1234-5678"}'
```

### 2. 로그인

```bash
curl -X POST http://localhost:8081/api/v1/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"password123"}'
```

### 3. 공연 조회

```bash
curl http://localhost:8082/api/v1/events
```

### 4. 좌석 조회

```bash
curl http://localhost:8082/api/v1/events/1/seats
```

### 5. 좌석 임시 선점 (핵심)

```bash
curl -X POST http://localhost:8083/api/v1/reservations \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{"eventId":1,"seatId":1}'
```

### 6. 결제 요청

```bash
curl -X POST http://localhost:8084/api/v1/payments \
  -H "Content-Type: application/json" \
  -H "X-User-Id: 1" \
  -H "Idempotency-Key: $(uuidgen)" \
  -d '{"reservationId":1,"seatId":1,"eventId":1,"amount":165000,"paymentMethod":"CARD"}'
```

### 7. 예매 내역 조회

```bash
curl http://localhost:8085/api/v1/bookings \
  -H "X-User-Id: 1"
```

### 8. 예매 취소

```bash
curl -X DELETE http://localhost:8085/api/v1/bookings/1 \
  -H "X-User-Id: 1"
```

## 핵심 설계 포인트

### Outbox 패턴
- 각 서비스는 비즈니스 로직과 OutboxEvent를 **같은 트랜잭션**에 저장
- OutboxPublisher 스케줄러가 1초 간격으로 미발행 이벤트를 Kafka에 발행
- 이벤트 유실 원천 차단

### Redis 분산 락 (Redisson)
- 좌석 선점 시 `seat:lock:{seatId}` 키로 즉시 실패 락
- DB Unique 제약이 2차 방어선
- Redis 장애 시 DB SELECT FOR UPDATE로 Fallback

### Consumer 멱등성
- ProcessedEvent 테이블에 Kafka 이벤트 ID 저장
- 비즈니스 처리 + ProcessedEvent 저장이 **같은 트랜잭션**
- 재소비 시 ProcessedEvent 조회로 중복 처리 방지

### SAGA 보상 트랜잭션
- 결제 실패 → PaymentFailed 이벤트 → 좌석 해제
- 예매 취소 → BookingCancelled 이벤트 → 좌석 복구

## Kafka 토픽

| 토픽 | 파티션 | 발행 서비스 | 소비 서비스 |
|---|---|---|---|
| reservation.events | 12 | reservation-service | event-service |
| payment.events | 12 | payment-service | booking-service |
| booking.events | 12 | booking-service | event-service, notification-service |
| *.DLQ | 3 | 자동 | 수동 재처리 |

## 모니터링

- Kafka UI: http://localhost:8090 (Consumer Lag 모니터링)
- Prometheus: http://localhost:9090
- Grafana: http://localhost:3000 (admin/admin1234)

주요 메트릭:
- `ticketing_seat_hold_success_total` - 좌석 선점 성공
- `ticketing_seat_hold_failure_total` - 좌석 선점 실패
- `ticketing_payment_success_total` - 결제 성공
- `ticketing_payment_failure_total` - 결제 실패
