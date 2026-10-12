using System.Diagnostics;
using System.Text;
using System.Text.Json;
using IdontConnect.Windows.Models;

namespace IdontConnect.Windows.Services;

/// <summary>
/// Process boundary only. This initial version writes a local SOCKS inbound config;
/// it does not create a Windows system-wide TUN/VPN tunnel.
/// </summary>
public sealed class XrayProcessManager : IDisposable
{
    private Process? _process;
    public event Action<string>? LogReceived;
    public bool IsRunning => _process is { HasExited: false };

    public async Task StartAsync(ServerProfile profile, string appDirectory, CancellationToken cancellationToken = default)
    {
        if (IsRunning) throw new InvalidOperationException("Xray is already running.");

        var executable = Path.Combine(appDirectory, "runtime", "xray.exe");
        if (!File.Exists(executable))
            throw new FileNotFoundException("xray.exe was not found. Place a trusted Windows x64 build at runtime\\xray.exe.", executable);

        var configPath = Path.Combine(Path.GetTempPath(), $"idontconnect-{Guid.NewGuid():N}.json");
        var config = XrayConfigBuilder.Create(profile);
        await File.WriteAllTextAsync(configPath, JsonSerializer.Serialize(config, new JsonSerializerOptions { WriteIndented = true }), cancellationToken);

        var start = new ProcessStartInfo
        {
            FileName = executable,
            Arguments = $"run -config \"{configPath}\"",
            WorkingDirectory = Path.GetDirectoryName(executable)!,
            UseShellExecute = false,
            RedirectStandardOutput = true,
            RedirectStandardError = true,
            CreateNoWindow = true
        };
        _process = new Process { StartInfo = start, EnableRaisingEvents = true };
        _process.OutputDataReceived += (_, e) => { if (e.Data is not null) LogReceived?.Invoke(e.Data); };
        _process.ErrorDataReceived += (_, e) => { if (e.Data is not null) LogReceived?.Invoke(e.Data); };
        if (!_process.Start()) throw new InvalidOperationException("Could not start Xray.");
        _process.BeginOutputReadLine();
        _process.BeginErrorReadLine();
        LogReceived?.Invoke("Xray process started. A running process alone does not prove the connection is healthy.");
    }

    public async Task StopAsync()
    {
        if (_process is null) return;
        try
        {
            if (!_process.HasExited)
            {
                _process.Kill(entireProcessTree: true);
                await _process.WaitForExitAsync();
            }
        }
        finally
        {
            _process.Dispose();
            _process = null;
        }
        LogReceived?.Invoke("Xray process stopped.");
    }

    public void Dispose()
    {
        if (_process is { HasExited: false })
        {
            try { _process.Kill(entireProcessTree: true); } catch { }
        }
        _process?.Dispose();
    }
}

internal static class XrayConfigBuilder
{
    public static object Create(ServerProfile profile)
    {
        // Deliberately fails closed for protocols until the URI's complete security,
        // transport, authentication and flow fields are parsed and validated.
        throw new NotSupportedException(
            $"Config generation for {profile.Protocol} is not yet enabled. " +
            "The URI parser is a profile-list foundation; complete protocol-specific config generation before connecting.");
    }
}
