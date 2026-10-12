using System.Collections.ObjectModel;
using System.Windows;
using System.Windows.Controls;
using Microsoft.Win32;
using IdontConnect.Windows.Models;
using IdontConnect.Windows.Services;

namespace IdontConnect.Windows;

public partial class MainWindow : Window
{
    private readonly ObservableCollection<ServerProfile> _profiles = new();
    private readonly XrayProcessManager _xray = new();

    public MainWindow()
    {
        InitializeComponent();
        ProfilesList.ItemsSource = _profiles;
        _xray.LogReceived += AppendLog;
        AppendLog("idontCONNECT Windows source foundation initialized.");
        AppendLog("No VPN tunnel is active. Do not rely on this foundation for system-wide protection.");
    }

    private void Import_Click(object sender, RoutedEventArgs e)
    {
        var dialog = new OpenFileDialog
        {
            Title = "Import profile link from text file",
            Filter = "Text files (*.txt)|*.txt|All files (*.*)|*.*"
        };

        string? text = null;
        if (dialog.ShowDialog() == true)
        {
            try { text = File.ReadAllText(dialog.FileName).Trim(); }
            catch (Exception ex) { MessageBox.Show(ex.Message, "Import error", MessageBoxButton.OK, MessageBoxImage.Error); return; }
        }
        else
        {
            var input = Microsoft.VisualBasic.Interaction.InputBox(
                "Paste one VLESS, VMess, Trojan or Shadowsocks link:",
                "Import profile");
            if (!string.IsNullOrWhiteSpace(input)) text = input.Trim();
        }

        if (string.IsNullOrWhiteSpace(text)) return;

        try
        {
            var profile = ProfileParser.Parse(text);
            if (_profiles.Any(p => p.OriginalUri == profile.OriginalUri))
            {
                AppendLog("Duplicate profile ignored.");
                return;
            }
            _profiles.Add(profile);
            ProfilesList.SelectedItem = profile;
            AppendLog($"Imported {profile.Protocol} profile: {profile.Name}");
        }
        catch (Exception ex)
        {
            MessageBox.Show(ex.Message, "Invalid profile", MessageBoxButton.OK, MessageBoxImage.Warning);
            AppendLog($"Import rejected: {ex.Message}");
        }
    }

    private void ProfilesList_SelectionChanged(object sender, SelectionChangedEventArgs e)
    {
        if (ProfilesList.SelectedItem is not ServerProfile profile) return;
        SelectedName.Text = profile.Name;
        SelectedEndpoint.Text = $"{profile.Address}:{profile.Port} • {profile.Protocol}";
    }

    private async void Connect_Click(object sender, RoutedEventArgs e)
    {
        if (ProfilesList.SelectedItem is not ServerProfile profile)
        {
            MessageBox.Show("Import and select a server profile first.", "No profile selected",
                MessageBoxButton.OK, MessageBoxImage.Information);
            return;
        }

        try
        {
            ConnectButton.IsEnabled = false;
            ConnectionLabel.Text = "STARTING";
            HeaderStatus.Text = "Starting";
            ConnectionHint.Text = "Validating profile and starting engine…";
            await _xray.StartAsync(profile, AppContext.BaseDirectory);
            // XrayConfigBuilder currently fails closed until full protocol config generation is implemented.
            ConnectionHint.Text = "Engine startup is not yet a verified VPN tunnel.";
        }
        catch (Exception ex)
        {
            ConnectionLabel.Text = "NOT CONNECTED";
            HeaderStatus.Text = "Disconnected";
            ConnectionHint.Text = "Connection not established.";
            AppendLog($"Connection not started: {ex.Message}");
            MessageBox.Show(ex.Message, "Connection unavailable", MessageBoxButton.OK, MessageBoxImage.Warning);
        }
        finally
        {
            ConnectButton.IsEnabled = true;
        }
    }

    private void AppendLog(string line)
    {
        Dispatcher.Invoke(() =>
        {
            LogBox.AppendText($"[{DateTime.Now:HH:mm:ss}] {line}{Environment.NewLine}");
            LogBox.ScrollToEnd();
        });
    }

    protected override void OnClosed(EventArgs e)
    {
        _xray.Dispose();
        base.OnClosed(e);
    }
}
