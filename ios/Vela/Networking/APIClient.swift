import Foundation

// MARK: - HTTP API 客户端
class APIClient {
    static let shared = APIClient()
    private let session: URLSession
    private var baseURL: String { AppState.shared.serverUrl }

    private init() {
        let config = URLSessionConfiguration.default
        config.timeoutIntervalForRequest = 15
        config.timeoutIntervalForResource = 30
        self.session = URLSession(configuration: config)
    }

    // MARK: - 通用请求
    private func request<T: Codable>(
        method: String,
        path: String,
        query: [String: String]? = nil,
        body: Encodable? = nil
    ) async throws -> T {
        var components = URLComponents(string: "\(baseURL)\(path)")!
        if let query = query {
            components.queryItems = query.map { URLQueryItem(name: $0.key, value: $0.value) }
        }

        var urlRequest = URLRequest(url: components.url!)
        urlRequest.httpMethod = method
        urlRequest.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if !AppState.shared.token.isEmpty {
            urlRequest.setValue(AppState.shared.token, forHTTPHeaderField: "token")
        }

        if let body = body {
            urlRequest.httpBody = try JSONEncoder().encode(AnyEncodable(body))
        }

        let (data, response) = try await session.data(for: urlRequest)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }
        guard (200...299).contains(httpResponse.statusCode) else {
            throw APIError.httpError(httpResponse.statusCode)
        }

        let decoder = JSONDecoder()
        let apiResponse = try decoder.decode(ApiResponse<T>.self, from: data)
        guard apiResponse.isOk else {
            throw APIError.businessError(apiResponse.msg ?? "未知错误")
        }
        guard let result = apiResponse.data else {
            throw APIError.noData
        }
        return result
    }

    // MARK: - 认证
    func login(userId: String, password: String) async throws -> String {
        let resp: ApiResponse<String> = try await request(
            method: "POST", path: "/v1/user/login",
            body: LoginReq(userId: userId, password: password)
        )
        return resp.data ?? ""
    }

    func register(userId: String, nickName: String, password: String) async throws {
        let _: ApiResponse<String> = try await request(
            method: "POST", path: "/v1/user/register",
            body: RegisterReq(userId: userId, nickName: nickName, password: password)
        )
    }

    // MARK: - 好友
    func getFriends(appId: Int = 10000, userId: String) async throws -> [FriendData] {
        try await request(method: "GET", path: "/v1/friend/getAllFriend",
                          query: ["appId": "\(appId)", "fromId": userId])
    }

    // MARK: - 消息
    func sendMessage(fromId: String, toId: String, body: String) async throws -> SendMessageResp {
        try await request(method: "POST", path: "/v1/message/send",
                          body: SendMessageReq(fromId: fromId, toId: toId, messageBody: body))
    }

    func syncOffline(userId: String) async throws -> SyncResp {
        try await request(method: "POST", path: "/v1/message/syncOfflineMessage",
                          body: SyncReq(operater: userId))
    }

    // MARK: - 用户配置
    func getUserConfig(userId: String) async throws -> [String: String] {
        try await request(method: "GET", path: "/v1/user/config/get",
                          query: ["appId": "10000", "userId": userId, "clientType": "ios"])
    }

    func saveUserConfig(userId: String, configs: [ConfigEntry]) async throws {
        let _: ApiResponse<[String: String]> = try await request(
            method: "POST", path: "/v1/user/config/save",
            query: ["appId": "10000", "userId": userId, "clientType": "ios"],
            body: configs
        )
    }

    // MARK: - Bot 市场
    func getBotMarket() async throws -> [BotData] {
        try await request(method: "GET", path: "/v1/bot/market/list",
                          query: ["appId": "10000"])
    }

    func installBot(userId: String, botId: String) async throws {
        let _: ApiResponse<String> = try await request(
            method: "POST", path: "/v1/bot/market/install",
            query: ["appId": "10000", "userId": userId, "botId": botId]
        )
    }

    func getMyBots(userId: String) async throws -> [BotData] {
        try await request(method: "GET", path: "/v1/bot/market/my",
                          query: ["appId": "10000", "userId": userId])
    }

    // MARK: - 功能开关
    func getFeatureFlags(userId: String) async throws -> [String: Bool] {
        try await request(method: "GET", path: "/v1/feature/flags",
                          query: ["appId": "10000", "userId": userId])
    }
}

// MARK: - 辅助类型
struct AnyEncodable: Encodable {
    private let _encode: (Encoder) throws -> Void
    init(_ wrapped: Encodable) {
        _encode = { try wrapped.encode(to: $0) }
    }
    func encode(to encoder: Encoder) throws {
        try _encode(encoder)
    }
}

enum APIError: LocalizedError {
    case invalidResponse
    case httpError(Int)
    case businessError(String)
    case noData

    var errorDescription: String? {
        switch self {
        case .invalidResponse: return "无效响应"
        case .httpError(let code): return "HTTP错误: \(code)"
        case .businessError(let msg): return msg
        case .noData: return "无数据"
        }
    }
}
