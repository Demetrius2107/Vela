using System.Collections.Generic;
using System.Net.Http;
using System.Net.Http.Json;
using System.Threading.Tasks;

namespace VelaWPF.Services;

public class ApiClient
{
    private static readonly HttpClient _client = new() { BaseAddress = new("http://localhost:8000") };
    private string _token = "";

    public void SetToken(string token) => _token = token;

    private void AddHeaders(HttpRequestMessage req)
    {
        req.Headers.Add("Content-Type", "application/json");
        if (!string.IsNullOrEmpty(_token))
            req.Headers.Add("token", _token);
    }

    public async Task<string> Login(string userId, string password)
    {
        var body = new { userId, password };
        var resp = await _client.PostAsJsonAsync("/v1/user/login", body);
        var json = await resp.Content.ReadFromJsonAsync<ApiResponse<string>>();
        if (json == null || json.Code != 200) throw new System.Exception(json?.Msg ?? "登录失败");
        return json.Data ?? "";
    }

    public async Task Register(string userId, string nickName, string password)
    {
        var body = new { userId, nickName, password };
        var resp = await _client.PostAsJsonAsync("/v1/user/register", body);
        var json = await resp.Content.ReadFromJsonAsync<ApiResponse<string>>();
        if (json == null || json.Code != 200) throw new System.Exception(json?.Msg ?? "注册失败");
    }

    public async Task<List<FriendData>> GetFriends(string userId)
    {
        var resp = await _client.GetAsync($"/v1/friend/getAllFriend?appId=10000&fromId={userId}");
        AddHeaders(resp.Headers);
        var json = await resp.Content.ReadFromJsonAsync<ApiResponse<List<FriendData>>>();
        return json?.Data ?? new();
    }
}

public class ApiResponse<T>
{
    public int Code { get; set; }
    public string? Msg { get; set; }
    public T? Data { get; set; }
    public bool IsOk => Code == 200;
}

public record FriendData(string? ToId, string? NickName, string? SelfSignature, int? Status);
