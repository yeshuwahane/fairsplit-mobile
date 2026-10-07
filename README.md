# FairSplit Mobile — Compose Multiplatform (Android & iOS)

> Production-grade, cross-platform expense sharing, group debt minimization, and bilateral settlement application built with **Kotlin Multiplatform (KMP)** and **Compose Multiplatform (CMP)**.

[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Compose Multiplatform](https://img.shields.io/badge/Compose_Multiplatform-1.7.3-4285F4?logo=jetpackcompose&logoColor=white)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![iOS](https://img.shields.io/badge/Platform-iOS-000000?logo=apple&logoColor=white)](https://developer.apple.com/ios/)
[![License](https://img.shields.io/badge/License-Proprietary-blue.svg)]()

---

## Highlights & Capabilities

- **100% Shared UI & Logic**: Single declarative codebase across Android and iOS using Compose Multiplatform.
- **Server-Authoritative Precision**: Zero floating-point rounding errors. All transactions and balances operate on integer minor units (paise / cents).
- **Smart Debt Minimization Engine**: Graph-based bilateral settlement simplification that reduces chaotic group debts down to minimal pairwise transfers.
- **Instant UPI Payment Initiation**: Deep links directly to installed UPI apps (Google Pay, PhonePe, Paytm, BHIM) with recipient UPI ID, payee name, and exact amount.
- **Modern Authentication**: Streamlined Google Sign-In with JWT access and refresh token rotation.
- **Real-Time Synchronization**: Live WebSocket events (`/api/v1/ws/epics/{epicId}`) instantly broadcast expenses, settlements, and member changes to all active devices.
- **Production Asset Suite**: Includes signature glowing emerald app icon (Android adaptive vector + mipmaps, iOS AppIcon) and store presentation graphics.

---

## Architecture

The mobile client adheres to Clean Architecture and strict Unidirectional Data Flow (MVI/UDF):

```text
                  ┌───────────────────────────────────────┐
                  │       UI Layer (CMP Composables)      │
                  │  Screens, Tabs, Custom Dialogs, Modals│
                  └───────────────────┬───────────────────┘
                                      │ UI Events
                                      ▼
                  ┌───────────────────────────────────────┐
                  │       Presentation (Voyager + Koin)   │
                  │  Screen Models, ViewModels, StateFlow │
                  └───────────────────┬───────────────────┘
                                      │ Domain Calls
                                      ▼
                  ┌───────────────────────────────────────┐
                  │              Domain Layer             │
                  │  ExpenseCalculationEngine, Use Cases  │
                  └───────────────────┬───────────────────┘
                                      │ Repository Interfaces
                                      ▼
                  ┌───────────────────────────────────────┐
                  │               Data Layer              │
                  │  • RemoteDataSources (Ktor HTTP/WSS)  │
                  │  • LocalDatabase (MultiplatformSettings)
                  │  • SettingsTokenStorage (Encrypted JWT)
                  └───────────────────┬───────────────────┘
                                      │
                                      ▼
                      Railway Production Ktor Backend
```

### Module Structure

- **`:androidApp`**: Android application entry point (`MainActivity`), splash theme, manifests, and adaptive launcher icons.
- **`:iosApp`**: iOS Xcode workspace and Swift application wrapper embedding CMP views.
- **`:shared`**: Core KMP module containing:
  - `presentation/`: Compose Multiplatform screens (Onboarding, Auth, Home, Epics, Expenses, Settlements, Settings).
  - `domain/`: Business logic, mathematical calculation models, and domain entities.
  - `data/`: Ktor HTTP client, WebSocket connections, JSON serialization, and local storage.
  - `di/`: Koin dependency injection modules.
  - `theme/`: Design tokens, typography, dark/light theme palettes.

---

## Production Backend Configuration

The mobile app connects to the hosted Railway production backend:

- **HTTPS API**: `https://fairplit-backend-production.up.railway.app`
- **WSS WebSocket**: `wss://fairplit-backend-production.up.railway.app`

Managed in [`ApiEndpoints.kt`](shared/src/commonMain/kotlin/com/cybernerd404/fairsplit/data/remote/ApiEndpoints.kt).

---

## Getting Started

### Prerequisites

- **JDK**: 21 (Eclipse Temurin recommended)
- **Android Studio**: Ladybug / Koala or IntelliJ IDEA
- **Xcode**: 16+ (for building iOS target on macOS)
- **CocoaPods / SPM**: Configured via Kotlin Multiplatform Mobile plugin

### 1. Building Android Application

```bash
# Clean build
./gradlew clean

# Assemble Debug APK
./gradlew :androidApp:assembleDebug

# Install directly to connected device / emulator
./gradlew :androidApp:installDebug
```

The compiled APK will be located at:
`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

### 2. Building iOS Application

```bash
# Compile Kotlin Multiplatform iOS framework
./gradlew :shared:compileKotlinIosArm64

# Or open iOS project in Xcode
open iosApp/iosApp.xcworkspace
```

---

## App Icons & Visual Assets

- **Android Icons**: Full adaptive vector set (`ic_launcher_background.xml`, `ic_launcher_foreground.xml`) and legacy mipmaps (`mdpi` through `xxxhdpi`) located in `androidApp/src/main/res/`.
- **iOS AppIcon**: 1024×1024 master icon located in `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`.

---

## License

Copyright © 2026 Yeshu Wahane. All rights reserved.