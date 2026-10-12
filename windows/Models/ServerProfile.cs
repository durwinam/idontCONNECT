namespace IdontConnect.Windows.Models;

public sealed record ServerProfile(
    string Name,
    string Protocol,
    string OriginalUri,
    string Address,
    int Port);
