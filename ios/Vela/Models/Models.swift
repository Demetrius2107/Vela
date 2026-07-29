import Foundation

// MARK: - API 响应结构
struct ApiResponse<T: Codable>: Codable {
    let code: Int
    let msg: String?
    let data: T?

    var isOk: Bool { code == 200 }
}

// MARK: - 请求体
struct LoginReq: Codable {
    let userId: String
    let password: String
}

struct RegisterReq: Codable {
    let userId: String
    let nickName: String
    let password: String
}

struct SendMessageReq: Codable {
    let fromId: String
    let toId: String
    let appId: Int = 10000
    let messageBody: String
}

struct SyncReq: Codable {
    let operater: String
    let appId: Int = 10000
    let lastSequence: Int64 = 0
    let maxLimit: Int = 100
}

struct ConfigEntry: Codable {
    let key: String
    let value: String
}

// MARK: - 响应体
struct SendMessageResp: Codable {
    let messageKey: Int64?
    let messageTime: Int64?
}

struct FriendData: Codable, Identifiable {
    var id: String { toId ?? "" }
    let toId: String?
    let nickName: String?
    let selfSignature: String?
    let status: Int?
}

struct BotData: Codable, Identifiable {
    var id: String { botId ?? "" }
    let botId: String?
    let botName: String?
    let description: String?
    let category: String?
}

struct ConversationData: Codable, Identifiable {
    var id: String { conversationId ?? "" }
    let conversationId: String?
    let conversationType: Int?
    let fromId: String?
    let toId: String?
    let isMute: Int?
    let isTop: Int?
}

struct OfflineMessage: Codable {
    let messageKey: Int64?
    let fromId: String?
    let toId: String?
    let messageBody: String?
    let messageTime: Int64?
}

// MARK: - 数据模型
struct Conversation: Identifiable {
    let id: String
    let name: String
    let lastMessage: String
    let time: String
    let unread: Int
    let online: Bool
    let isGroup: Bool
    let colorIndex: Int
}

struct ChatMessage: Identifiable {
    let id: String
    let content: String
    let isSelf: Bool
    let time: String
}
