# FairSplit — Real-Time Group Expense & Financial Settlement Platform

> **FairSplit** is a production-grade, offline-first, cross-platform expense sharing and bilateral financial settlement engine built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)** for Android and iOS, backed by a high-performance **Ktor** server, **PostgreSQL**, **Exposed ORM**, and **Flyway**.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.7.3-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Ktor](https://img.shields.io/badge/Ktor-3.0.3-087CFA?logo=ktor&logoColor=white)](https://ktor.io/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Backend-brightgreen)]()

---

## Architecture Overview

FairSplit follows Clean Architecture and Strict Unidirectional Data Flow (UDF). The financial source of truth is **server-authoritative**, eliminating split discrepancies, floating-point inaccuracies, and client synchronization drift.

```text
       ┌──────────────────────────┐             ┌──────────────────────────┐
       │   Android Application    │             │     iOS Application      │
       │ (Compose Multiplatform)  │             │ (Compose Multiplatform)  │
       └────────────┬─────────────┘             └────────────┬─────────────┘
                    │                                        │
                    └───────────────────┬────────────────────┘
                                        ▼
                        ┌───────────────────────────────┐
                        │    Shared KMP Core Layer      │
                        │ • Voyager (Stack Navigation)  │
                        │ • Koin DI & MVI ViewModels    │
                        │ • StateFlow & Reactive State  │
                        │ • Ktor HTTP & WSS Engine      │
                        └───────────────┬───────────────┘
                                        │
                      HTTP REST (v1)    │    WebSocket Room Events
                    Idempotency-Key     │    (Real-time State Updates)
                                        ▼
                        ┌───────────────────────────────┐
                        │      Ktor Microservice        │
                        │ • JWT Auth & Token Rotation   │
                        │ • Bilateral Debt Calculator   │
                        │ • Idempotency Engine          │
                        │ • Real-time Event Broadcaster │
                        └───────────────┬───────────────┘
                                        │
                                Exposed ORM + HikariCP
                                Flyway Migrations
                                        ▼
                        ┌───────────────────────────────┐
                        │      PostgreSQL Database      │
                        │ • BigInt Minor Currency Units │
                        │ • Strict Foreign Keys & Locks │
                        │ • Audit Trails & JSONB Logs   │
                        └───────────────────────────────┘
```

---

## Key Features

- **Multiplatform Unified UI**: 100% shared declarative UI across Android and iOS using Compose Multiplatform.
- **Native Gestures**: iOS-style interactive edge swipe-back gesture with coordinated Voyager pop transitions (zero double-animations, zero UI thread starvation).
- **Server-Authoritative Balances**: Zero floating-point arithmetic. All balances, shares, and settlements use integer minor units (`amount_minor BIGINT`) to guarantee cent-for-cent precision.
- **Deterministic Split Engine**:
  - **Equal Split**: Deterministic cent remainder allocation to ensure $\sum \text{splits} \equiv \text{total}$.
  - **Exact Split**: Custom per-member splits verified against total.
  - **Percentage Split**: 10,000 basis points ($100.00\%$) with automated discrepancy absorption.
  - **Shares Split**: Exact proportion calculation with zero rounding drift.
- **Bilateral Debt & Lifecycle Settlements**:
  - Live bilateral pairwise debt calculation ($A \rightarrow B$).
  - Full and partial settlement recording.
  - Settlement amount adjustments with validation against outstanding balance.
  - Settlement voiding (`VOIDED` status) with full audit preservation.
  - Prevention of overpayment beyond actual bilateral debt.
- **Real-Time WebSocket Synchronization**:
  - Multiplexed `/api/v1/ws/epics/{epicId}` rooms.
  - Automatic reconnection, token verification, and membership-gated access.
  - Post-commit WebSocket broadcast triggers authoritative REST synchronization across all active devices.
- **Idempotent Financial Writes**:
  - All expense and settlement creation endpoints support `Idempotency-Key` headers with SHA-256 request payload validation, preventing duplicate charges across flaky mobile networks.
- **Secure Authentication**:
  - Phone OTP authentication with isolated development OTP (`123456`) and production random 6-digit generation.
  - SHA-256 hashed refresh token storage with atomic token rotation.
  - Google Sign-In with strict account isolation.
- **Receipt & Attachment Handling**:
  - Up to 15MB file uploads with strict extension filtering, path traversal mitigation, and unique UUID naming.

---

## Tech Stack

| Layer | Technologies |
|---|---|
| **Mobile Client** | Kotlin Multiplatform 2.1, Compose Multiplatform 1.7, Voyager Navigation, Koin, Napier Logging |
| **Networking** | Ktor Client 3.0 (CIO & Darwin engines), WebSockets, Kotlinx Serialization |
| **Backend Server** | Ktor Server 3.0 (Netty Engine), Koin Ktor, JJWT, Slf4j, Logback |
| **Persistence** | PostgreSQL 16, Exposed SQL ORM 0.56, HikariCP Connection Pool, Flyway Migrations |
| **Security** | JWT (HMAC-SHA256), SecureRandom tokens, SHA-256 OTP & refresh token hashing |
| **Deployment** | Docker Multi-Stage (Alpine Linux + Eclipse Temurin 21 JRE) |

---

## Financial Calculation Model

FairSplit maintains double-entry accounting integrity across all transactions.

### 1. Integer Minor Units
To prevent IEEE-754 floating point rounding errors, all amounts are stored and calculated in currency minor units (e.g., paise / cents):
$$\text{amount\_minor} = \text{round}(\text{amount} \times 100)$$

### 2. Net Balances
For any group member $i$:
$$\text{NetBalance}_i = \sum \text{PaidExpenses}_i + \sum \text{PaidSettlements}_i - \left( \sum \text{OwedSplits}_i + \sum \text{ReceivedSettlements}_i \right)$$

### 3. Bilateral Pairwise Debts
For every pair of users $(A, B)$, FairSplit tracks directed obligations $O(A \rightarrow B)$ resulting from expenses paid by $B$ for $A$. When settlements $S(A \rightarrow B)$ occur:
$$\text{NetDebt}(A \rightarrow B) = \max\left(0, \left(O(A \rightarrow B) - S(A \rightarrow B)\right) - \left(O(B \rightarrow A) - S(B \rightarrow A)\right)\right)$$

Voided settlements (`status = 'VOIDED'`) are dynamically excluded from debt and balance aggregations while preserving immutable audit trails.

---

## Local Development & Setup

### Prerequisites
- JDK 21 (Eclipse Temurin recommended)
- Android Studio Ladybug / Koala or IntelliJ IDEA Ultimate
- Xcode 16+ (for iOS target on macOS)
- PostgreSQL 15+ (optional for local dev; server falls back to embedded H2 in PostgreSQL mode if no `DATABASE_URL` is configured)

### 1. Running the Backend

```bash
cd "/Users/yeshuwahane/IdeaProjects/Fair Split"

# Run tests
./gradlew test

# Start the server (default port: 8080)
./gradlew run
```

### 2. Running the Mobile Application

```bash
cd "/Users/yeshuwahane/AndroidStudioProjects/FairSplit"

# Build Android Debug APK
./gradlew :androidApp:assembleDebug

# Install on connected device or emulator
adb install -r androidApp/build/outputs/apk/debug/androidApp-debug.apk

# Compile iOS target
./gradlew :shared:compileKotlinIosArm64
```

---

## Production Deployment

### Docker Deployment

A production-ready multi-stage Dockerfile is provided:

```bash
cd "/Users/yeshuwahane/IdeaProjects/Fair Split"

# Build container
docker build -t fairsplit-server:latest .

# Run container with environment configuration
docker run -d -p 8080:8080 \
  -e DATABASE_URL="jdbc:postgresql://postgres.production:5432/fairsplit" \
  -e DATABASE_USER="fairsplit_user" \
  -e DATABASE_PASSWORD="strong_production_password" \
  -e JWT_SECRET="production-jwt-secret-key-32-chars-minimum" \
  -e APP_ENV="prod" \
  --name fairsplit-backend \
  fairsplit-server:latest
```

### Health Check Endpoint
The backend exposes `GET /health` responding with HTTP 200:
```json
{
  "status": "ok",
  "timestamp": "2026-10-06T16:41:11.234Z"
}
```

---

## Database Management & Backup Procedure

### Automated Migrations
On startup in PostgreSQL mode, Flyway automatically applies all versioned SQL migrations (`V1` through `V10`) with zero downtime.

### Backup Database
```bash
pg_dump -Fc -h $PGHOST -U $PGUSER -d $PGDATABASE > fairsplit_backup_$(date +%Y%m%d_%H%M%S).dump
```

### Restore Database
```bash
pg_restore --clean --if-exists -h $PGHOST -U $PGUSER -d $PGDATABASE < fairsplit_backup.dump
```

---

## License

Copyright © 2026 Yeshu Wahane. All rights reserved.