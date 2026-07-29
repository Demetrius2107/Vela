import SwiftUI

struct BotMarketView: View {
    @State private var bots: [BotData] = []
    @State private var installedIds: Set<String> = []
    @State private var isLoading = false

    var body: some View {
        NavigationStack {
            Group {
                if isLoading {
                    ProgressView("加载中...")
                } else if bots.isEmpty {
                    VStack(spacing: 12) {
                        Text("🤖").font(.system(size: 48))
                        Text("暂无可用 Bot").foregroundColor(.velaTextSecondary)
                    }
                } else {
                    List(bots) { bot in
                        BotMarketRow(bot: bot, isInstalled: installedIds.contains(bot.id)) {
                            await installBot(bot)
                        }
                        .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                        .listRowSeparator(.hidden)
                    }
                    .listStyle(.plain)
                    .refreshable { await loadData() }
                }
            }
            .navigationTitle("Bot 市场")
            .task { await loadData() }
        }
    }

    private func loadData() async {
        guard !AppState.shared.userId.isEmpty else { return }
        isLoading = true
        do {
            let marketBots = try await APIClient.shared.getBotMarket()
            let myBots = try await APIClient.shared.getMyBots(userId: AppState.shared.userId)
            await MainActor.run {
                bots = marketBots
                installedIds = Set(myBots.compactMap { $0.botId })
            }
        } catch { /* 离线可用 */ }
        isLoading = false
    }

    private func installBot(_ bot: BotData) async {
        guard let botId = bot.botId else { return }
        installedIds.insert(botId)
        do {
            try await APIClient.shared.installBot(userId: AppState.shared.userId, botId: botId)
        } catch {
            await MainActor.run { installedIds.remove(botId) }
        }
    }
}

struct BotMarketRow: View {
    let bot: BotData
    let isInstalled: Bool
    let onInstall: () async -> Void

    var body: some View {
        HStack(spacing: 14) {
            // 图标
            ZStack {
                RoundedRectangle(cornerRadius: 14)
                    .fill(Color(red: 0.45, green: 0.27, blue: 0.93).opacity(0.1))
                    .frame(width: 48, height: 48)
                Text("🤖").font(.system(size: 24))
            }

            // 文字
            VStack(alignment: .leading, spacing: 3) {
                HStack(spacing: 6) {
                    Text(bot.botName ?? "未知")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundColor(.velaTextPrimary)
                    if let cat = bot.category, !cat.isEmpty {
                        Text(cat)
                            .font(.system(size: 10))
                            .foregroundColor(.velaPrimary)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(Color.velaPrimary.opacity(0.1))
                            .cornerRadius(6)
                    }
                }
                if let desc = bot.description, !desc.isEmpty {
                    Text(desc)
                        .font(.system(size: 13))
                        .foregroundColor(.velaTextSecondary)
                        .lineLimit(1)
                }
            }

            Spacer()

            // 安装按钮
            Button(action: {
                Task { await onInstall() }
            }) {
                Text(isInstalled ? "已安装" : "安装")
                    .font(.system(size: 12, weight: .semibold))
                    .foregroundColor(isInstalled ? .velaTextSecondary : .white)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 6)
                    .background(isInstalled ? Color(.systemGray5) : Color.velaPrimary)
                    .cornerRadius(14)
            }
            .disabled(isInstalled)
        }
        .padding(.vertical, 6)
    }
}
