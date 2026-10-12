# Native Xray integration contract

The app intentionally depends on `engine.XrayEngine`, not directly on a particular generated libXray class. Replace `UnavailableXrayEngine` only after building and pinning a dated official `XTLS/libXray` Android artifact.

Required lifecycle work:

1. Request `VpnService.prepare()` permission in the Activity and handle cancellation.
2. Establish a correctly configured Android TUN with `VpnService.Builder`.
3. Protect native sockets with `VpnService.protect()` before the core dials the proxy, using the integration hook supported by the pinned libXray version.
4. Supply the TUN file descriptor in the exact config field expected by that pinned release; current upstream README documents `xray.tun.fd` in the root `env` object and says `SetTunFd` was removed.
5. Set the selected DNS using the Android-specific libXray controller API before starting; reset it after stop.
6. Call the structured `Invoke` API with the supported API version and handle its structured `success/error` response.
7. Stop the core, close TUN descriptors, reset process DNS, and update state on all failure/stop/service-destroy paths.
8. Expose local Xray metrics only while the instance is active. Poll the documented loopback endpoint every 3 seconds and calculate deltas from monotonic counters; do not infer speed from total bytes without a time delta.
9. Verify real routing, DNS behavior, IPv4/IPv6, app allow/deny lists, reconnect, background operation, and leak behavior on physical Android devices.

Upstream README: https://github.com/XTLS/libXray/blob/main/README.md

Do not enable a UI connected state or advertise Kill Switch until these steps are implemented and verified.
