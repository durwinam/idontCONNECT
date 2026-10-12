# idontCONNECT Windows — source foundation

Target: Windows 10/11 x64  
UI: C# / WPF (.NET 8)  
Core: Xray-core launched as a separate process

## Current scope

This is a Windows desktop **source foundation**, not a finished VPN client.
It provides:
- dark desktop UI shell;
- profile list and basic URI import for VLESS, VMess, Trojan, and Shadowsocks;
- connection state/log presentation;
- an Xray process boundary and configuration-generation starting point;
- a Windows CI workflow to compile the project.

## Important limitations

- The Xray executable is not bundled. Place a trusted Windows x64 `xray.exe` at `runtime/xray.exe`.
- The current connection runner starts Xray with a generated config and local SOCKS inbound. It does **not** yet create a system-wide TUN tunnel.
- System-wide VPN routing requires a properly implemented and tested Wintun/TUN + tun2socks (or equivalent) integration, DNS handling, route restoration, kill switch, and leak tests. Do not describe this foundation as a full VPN until those are implemented.
- URI parsers are an initial foundation and need validation against real-world links and subscriptions.
- The code has not been compiled or tested in this environment.

## Build

Install .NET 8 SDK on Windows, then from this folder:

```powershell
dotnet restore
dotnet build -c Release
dotnet run
```

## Runtime

Put `xray.exe` in `runtime/xray.exe` next to the project folder. Review the generated config and use only servers you are authorized to access.

## Planned next steps

1. Complete robust protocol parsing and subscription refresh.
2. Add Xray config validation (`xray run -test -config ...`) before connection.
3. Add reliable process lifecycle, health checks, and real traffic counters.
4. Implement Wintun/TUN integration, DNS management, routes, and safe cleanup.
5. Add auto-reconnect, latency/stability probing, split tunneling, kill switch, and diagnostics redaction.
6. Test on clean Windows 10 and Windows 11 machines.
