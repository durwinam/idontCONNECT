using System.Text;
using System.Text.Json;
using IdontConnect.Windows.Models;

namespace IdontConnect.Windows.Services;

public static class ProfileParser
{
    public static ServerProfile Parse(string input)
    {
        var uri = input.Trim();
        if (uri.StartsWith("vless://", StringComparison.OrdinalIgnoreCase))
            return ParseUserInfoUri(uri, "vless");
        if (uri.StartsWith("trojan://", StringComparison.OrdinalIgnoreCase))
            return ParseUserInfoUri(uri, "trojan");
        if (uri.StartsWith("vmess://", StringComparison.OrdinalIgnoreCase))
            return ParseVmess(uri);
        if (uri.StartsWith("ss://", StringComparison.OrdinalIgnoreCase))
            return ParseShadowsocks(uri);

        throw new FormatException("Unsupported link. Supported schemes: VLESS, VMess, Trojan, Shadowsocks.");
    }

    private static ServerProfile ParseUserInfoUri(string input, string scheme)
    {
        if (!Uri.TryCreate(input, UriKind.Absolute, out var uri) || string.IsNullOrWhiteSpace(uri.Host))
            throw new FormatException("The profile link is invalid.");

        var name = Uri.UnescapeDataString(uri.Fragment.TrimStart('#'));
        if (string.IsNullOrWhiteSpace(name)) name = $"{scheme.ToUpperInvariant()} • {uri.Host}";
        return new ServerProfile(name, scheme.ToUpperInvariant(), input, uri.Host, uri.Port);
    }

    private static ServerProfile ParseVmess(string input)
    {
        var encoded = input["vmess://".Length..].Split('#')[0];
        var json = Encoding.UTF8.GetString(Convert.FromBase64String(PadBase64(encoded)));
        using var doc = JsonDocument.Parse(json);
        var root = doc.RootElement;
        var address = root.TryGetProperty("add", out var add) ? add.GetString() ?? "" : "";
        var portText = root.TryGetProperty("port", out var port) ? port.ToString() : "0";
        _ = int.TryParse(portText, out var portNumber);
        var name = Uri.UnescapeDataString(input.Contains('#') ? input[(input.IndexOf('#') + 1)..] : "");
        if (string.IsNullOrWhiteSpace(name)) name = root.TryGetProperty("ps", out var ps) ? ps.GetString() ?? "VMess" : "VMess";
        if (string.IsNullOrWhiteSpace(address) || portNumber is < 1 or > 65535)
            throw new FormatException("The VMess link has an invalid address or port.");
        return new ServerProfile(name, "VMESS", input, address, portNumber);
    }

    private static ServerProfile ParseShadowsocks(string input)
    {
        var withoutScheme = input["ss://".Length..];
        var fragmentIndex = withoutScheme.IndexOf('#');
        var name = fragmentIndex >= 0 ? Uri.UnescapeDataString(withoutScheme[(fragmentIndex + 1)..]) : "Shadowsocks";
        var body = fragmentIndex >= 0 ? withoutScheme[..fragmentIndex] : withoutScheme;
        var queryIndex = body.IndexOf('?');
        if (queryIndex >= 0) body = body[..queryIndex];

        string host;
        int portNumber;
        if (body.Contains('@'))
        {
            var at = body.LastIndexOf('@');
            var endpoint = body[(at + 1)..];
            var colon = endpoint.LastIndexOf(':');
            if (colon < 0 || !int.TryParse(endpoint[(colon + 1)..], out portNumber))
                throw new FormatException("The Shadowsocks endpoint is invalid.");
            host = endpoint[..colon];
        }
        else
        {
            var decoded = Encoding.UTF8.GetString(Convert.FromBase64String(PadBase64(body)));
            var at = decoded.LastIndexOf('@');
            var endpoint = at >= 0 ? decoded[(at + 1)..] : "";
            var colon = endpoint.LastIndexOf(':');
            if (colon < 0 || !int.TryParse(endpoint[(colon + 1)..], out portNumber))
                throw new FormatException("The Shadowsocks link is invalid.");
            host = endpoint[..colon];
        }

        if (string.IsNullOrWhiteSpace(name)) name = $"Shadowsocks • {host}";
        return new ServerProfile(name, "SHADOWSOCKS", input, host, portNumber);
    }

    private static string PadBase64(string value)
    {
        value = value.Replace('-', '+').Replace('_', '/');
        return value.PadRight(value.Length + ((4 - value.Length % 4) % 4), '=');
    }
}
