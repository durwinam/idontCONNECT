# idontCONNECT — Architecture

## Architecture boundaries

- `app`: Android entry point and app-level state coordination.
- `core/model`: platform-light models for profiles, engine status, traffic, and network health.
- `data/profile`: profile parsing, persistence, deduplication, favourites and folders.
- `data/settings`: persisted appearance, language, accent, glow and animation preferences.
- `feature/*`: screen-level Compose UI. Screens receive state and callbacks; they do not access DataStore or native APIs.
- `engine`: the `XrayEngine` boundary. The default implementation is deliberately unavailable until a native artifact is pinned and integrated.
- `vpn`: Android `VpnService` entry point. It currently fails closed and does not create a fake tunnel.
- `telemetry`: traffic data contract. No synthetic samples are emitted.

## State flow

Repositories expose immutable `Flow` streams. `AppViewModel` combines them into `AppUiState`. UI events call ViewModel methods; the UI renders state. Persisted settings are stored in DataStore. Profiles are serialized locally; secrets must not be logged.

## Native engine integration gate

Before enabling real connection:

1. Pin a dated `XTLS/libXray` release and the matching Xray-core revision.
2. Build the official Android artifact using the repository's documented build script.
3. Verify the generated Java/Kotlin API, supported ABIs, license notices, and reproducible build provenance.
4. Implement the actual TUN file descriptor handoff, socket protection, DNS setup/reset, engine start/stop and lifecycle recovery.
5. Add Xray metrics configuration and poll only the local loopback metrics endpoint every 3 seconds while connected.
6. Add instrumentation tests against a controlled test server and confirm traffic is routed through the VPN.

`libXray` explicitly warns that its API is not guaranteed stable and that it is compatible with the latest Xray-core release. Do not copy an old API example without checking the pinned release.
