using System.Windows;
using VelaWPF.Services;

namespace VelaWPF.Views;

public partial class MainWindow : Window
{
    private readonly ApiClient _api;
    private readonly string _userId;

    public MainWindow(ApiClient api, string userId)
    {
        InitializeComponent();
        _api = api;
        _userId = userId;
        ChatTitle.Text = $"欢迎, {userId}";
        Loaded += async (_, _) => await LoadConversations();
    }

    private async Task LoadConversations()
    {
        try
        {
            var friends = await _api.GetFriends(_userId);
            ConversationList.ItemsSource = friends.Select(f => new
            {
                Name = f.NickName ?? f.ToId ?? "未知",
                Id = f.ToId
            }).ToList();
        }
        catch { }
    }
}
