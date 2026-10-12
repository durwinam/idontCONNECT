<<<<<<< HEAD
# idontCONNECT v1.0.0 — Professional source foundation

Android VPN client source built with Kotlin, Jetpack Compose, Material 3, DataStore, a layered package structure, and a fail-closed Xray integration boundary.

## What is included

- Split screen UI: Home, Profiles, Settings.
- Dedicated theme, reusable glass card, and animated two-series chart component.
- Light/Dark/System themes and six localized resource sets: Persian, English, Russian, Simplified Chinese, Spanish, German.
- Persisted profile list with protocol parsing, duplicate detection, favourites, folders, and settings persistence.
- QR scanning wired through the embedded ZXing scanner, import dialog and clipboard paste.
- `XrayEngine` interface and unavailable default adapter; no fake connection is displayed.
- Traffic telemetry contract; no fabricated traffic samples.
- Unit tests for profile parsing and a CI workflow for tests/build.
- Architecture and research notes.

## Current limitations — read before use

This is a **source foundation, not a ready-to-use VPN client or a verified APK**. The native Xray/libXray Android artifact is not bundled. The Android `VpnService` deliberately fails closed and does not create a tunnel. Live traffic metrics, Subscription HTTP refresh and QR result wiring still require completion and device-level testing. Smart server selection, Network Health, Kill Switch, Split Tunneling, and safe diagnostics are planned surfaces, not production-ready features in this source state.

The import parser validates link shape; it does not prove that a profile can connect. Subscription URLs are recognized as subscription entries but are not fetched in this source state. Never assume a connection is protected until the native engine, TUN route, DNS handling, and leak tests are complete.

## Build

Open this directory in a recent Android Studio installation, install Android SDK Platform 35 and JDK 17, sync Gradle, run `:app:testDebugUnitTest`, then `:app:assembleDebug`.

Or run the included GitHub Actions workflow, which installs JDK/Android SDK/Gradle, runs tests, assembles a debug APK, and uploads it as a workflow artifact.

## Package

`ir.idontconnect` (debug package suffix: `.debug`).

## Native engine integration gate

Read `docs/ARCHITECTURE.md` and `docs/RESEARCH.md` before implementing the native bridge. Pin a dated libXray release and matching Xray-core version, generate the Android artifact using the official build script, verify the API and ABIs, then implement TUN FD handoff, socket protection, DNS setup/reset, start/stop, and real metrics polling. Do not mark the VPN connected until the engine reports a verified running state.
=======
# idontCONNECT
>>>>>>> origin/main
