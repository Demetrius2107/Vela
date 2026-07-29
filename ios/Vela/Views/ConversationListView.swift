import SwiftUI

struct ConversationListView: View {
    @State private var conversations: [Conversation] = []
    @State private var isLoading = false

    var body: some View {
        NavigationStack {
            Group {
                if isLoading {
                    ProgressView("加载中...")
                } else if conversations.isEmpty {
                    VStack(spacing: 12) {
                        Text("💬").font(.system(size: 48))
                        Text("暂无会话").foregroundColor(.velaTextSecondary)
                    }
                } else {
                    List(conversations) { conv in
                        NavigationLink(destination: ChatView(conversation: conv)) {
                            ConversationRow(conv: conv)
                        }
                        .listRowSeparator(.hidden)
                        .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                    }
                    .listStyle(.plain)
                }
            }
            .navigationTitle("Vela")
            .refreshable { await loadConversations() }
            .task { await loadConversations() }
        }
    }

    private func loadConversations() async {
        guard !AppState.shared.userId.isEmpty else { return }
        isLoading = true
        do {
            let friends = try await APIClient.shared.getFriends(userId: AppState.shared.userId)
            await MainActor.run {
                conversations = friends.enumerated().map { (i, f) in
                    Conversation(id: f.toId ?? "", name: f.nickName ?? f.toId ?? "",
                                 lastMessage: f.selfSignature ?? "", time: "", unread: 0,
                                 online: f.status == 1, isGroup: false,
                                 colorIndex: i % convColors.count)
                }
            }
        } catch { /* 离线可用 */ }
        isLoading = false
    }
}

struct ConversationRow: View {
    let conv: Conversation

    var body: some View {
        HStack(spacing: 12) {
            // 左侧彩色边条
            Rectangle()
                .fill(convColors[conv.colorIndex])
                .frame(width: 3, height: 40)
                .cornerRadius(2)

            // 头像
            ZStack {
                Circle()
                    .fill(convColors[conv.colorIndex].opacity(0.15))
                    .frame(width: 44, height: 44)
                Text(conv.name.prefix(1))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(convColors[conv.colorIndex])
            }
            .overlay(
                Circle()
                    .fill(conv.online ? Color.velaSuccess : Color.clear)
                    .frame(width: 10, height: 10)
                    .offset(x: 16, y: 16)
            )

            // 文字
            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(conv.name)
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(convColors[conv.colorIndex])
                    Spacer()
                    Text(conv.time)
                        .font(.system(size: 12))
                        .foregroundColor(.velaTextSecondary)
                }
                Text(conv.lastMessage)
                    .font(.system(size: 13))
                    .foregroundColor(.velaTextSecondary)
                    .lineLimit(1)
            }
        }
        .padding(.vertical, 8)
    }
}
