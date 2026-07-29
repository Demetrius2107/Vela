using System.Windows;
using VelaWPF.Services;

namespace VelaWPF.Views;

public partial class RegisterWindow : Window
{
    private readonly ApiClient _api = new();

    public RegisterWindow()
    {
        InitializeComponent();
    }

    private async void OnRegisterClick(object sender, RoutedEventArgs e)
    {
        var userId = UserIdBox.Text.Trim();
        var nickName = NickNameBox.Text.Trim();
        var password = PasswordBox.Password.Trim();

        if (string.IsNullOrEmpty(userId) || string.IsNullOrEmpty(nickName) || password.Length < 6)
        {
            StatusText.Text = "请填写完整信息，密码至少6位";
            return;
        }

        try
        {
            await _api.Register(userId, nickName, password);
            StatusText.Foreground = System.Windows.Media.Brushes.Green;
            StatusText.Text = "注册成功！";
            await Task.Delay(1000);
            Close();
        }
        catch (System.Exception ex)
        {
            StatusText.Text = ex.Message;
        }
    }
}
