# Research notes (checked 2026-10-10)

## Android architecture

Official Android architecture guidance recommends separation of concerns, layered UI/data boundaries, repositories, immutable UI state, unidirectional data flow, coroutines/Flow, and lifecycle-aware UI collection. This source follows that direction; it intentionally avoids putting persistence or native-engine calls directly in composables.

Source: https://developer.android.com/topic/architecture
Source: https://developer.android.com/topic/architecture/recommendations

## Xray integration

The official XTLS/libXray README states that it wraps Xray-core, exposes a structured `Invoke` API (API version 3 in the checked README), and does not guarantee API stability. It says the wrapper is only compatible with the latest Xray-core release and recommends using its build script. Its API includes `runXray`, `stopXray`, `getXrayState`, `testXray`, `pingBatch`, and conversion methods. It also documents Android-specific DNS setup/reset and the Xray metrics endpoint.

Source: https://github.com/XTLS/libXray/blob/main/README.md

Important: the checked source archive does not bundle a generated Android native artifact. Therefore real VPN connectivity and live traffic telemetry are explicitly marked unavailable. This is safer and more accurate than showing a fake connected state.

## Build validation

The current authoring environment does not have Android SDK or Gradle installed and has no network access for downloading them. The Gradle build and Android tests therefore cannot be claimed as passed here. CI is configured to run unit tests and assemble a debug APK on a GitHub Actions runner.
