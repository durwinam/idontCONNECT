# idontCONNECT Windows — Developer Build

This is a branded derivative build based on FlClash 0.8.99 source (GPL-3.0). It retains the upstream Flutter/Dart app architecture and mihomo core, including its profile/database/connection lifecycle, rather than replacing it with a nonfunctional mock UI.

## Changes in this package
- idontCONNECT branding in Windows title/version resources and installer metadata.
- Supplied idontCONNECT logo used as the app/about image and Windows executable icon.
- Logs page is hidden from navigation and its visibility toggle is removed from the General settings UI. Internal diagnostic logging required by upstream/core is not deleted.
- Existing project package IDs and internal FlClash helper names are retained where required to avoid breaking plugin and core integration.

## Build
The repository's documented build environment is Flutter 3.47.4, Go 1.26.4 and Rust stable. On Windows, install Flutter, Go, Rust (rustup), Visual Studio Build Tools C++ workload, and Inno Setup 6 if making the installer. Then run:

```powershell
flutter pub get
dart setup.dart windows --targets zip
```

The GitHub Actions workflow `windows-release-test.yml` builds a Windows release bundle and uploads it as an artifact. A green workflow means the bundle compiled; it does not mean every VPN mode has been manually tested on the user's machine.

## License
This is a derivative of FlClash, Copyright its upstream contributors, licensed under GNU GPL v3. See `LICENSE`. If distributing binaries, comply with GPL-3.0 source and notice requirements. The idontCONNECT logo is user-provided and is not part of upstream FlClash.
