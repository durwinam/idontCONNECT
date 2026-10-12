# Contributing

- Use four spaces, LF line endings, UTF-8, and the Kotlin official code style (`.editorconfig`).
- Keep Compose functions declarative: no DataStore, HTTP, or native engine calls inside UI composables.
- Route app data through repositories and expose UI state as immutable `StateFlow`.
- Never log raw share links, credentials, subscription bodies, decrypted content, or full Xray JSON.
- Do not show `Connected` unless the native engine and tunnel lifecycle confirm success.
- Do not synthesize traffic samples or label estimated values as live measurements.
- Add unit tests for parsers and deterministic business logic. Add device/instrumentation tests for VPN permissions, TUN lifecycle, routing and leak behavior.
- Before merging, run `gradle :app:testDebugUnitTest` and `gradle :app:assembleDebug`.
