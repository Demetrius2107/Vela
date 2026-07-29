using System.Windows;
using VelaWPF.Services;

namespace VelaWPF.Views;

public partial class LoginWindow : Window
{
    private readonly ApiClient _api = new();

    public LoginWindow()
    {
        InitializeComponent();
    }

    private async void OnLoginClick(object sender, RoutedEventArgs e)
    {
        var userId = UserIdBox.Text.Trim();
        var password = PasswordBox.Password.Trim();

        if (string.IsNullOrEmpty(userId) || string.IsNullOrEmpty(password))
        {
            ErrorText.Text = "请输入用户ID和密码";
            return;
        }

        try
        {
            var token = await _api.Login(userId, password);
            _api.SetToken(token);

            var mainWindow = new MainWindow(_api, userId);
            mainWindow.Show();
            Close();
        }
        catch (System.Exception ex)
        {
            ErrorText.Text = ex.Message;
        }
    }

    private void OnRegisterClick(object sender, RoutedEventArgs e)
    {
        var registerWindow = new RegisterWindow();
        registerWindow.ShowDialog();
    }
}
