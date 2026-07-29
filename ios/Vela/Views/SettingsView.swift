import SwiftUI

struct SettingsView: View {
    @EnvironmentObject var appState: AppState
    @State private var notifyEnabled = true
    @State private var notifySound = true
    @State private var notifyPreview = true
    @State private var privacySearchable = true
    @State private var compactMode = false
    @State private var isLoading = false

    var body: some View {
        NavigationStack {
            List {
                // 显示设置
                Section("🎨 显示设置") {
                    Toggle(isOn: $compactMode) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("紧凑模式").font(.system(size: 15))
                            Text("减少聊天列表间距").font(.system(size: 12)).foregroundColor(.velaTextSecondary)
                        }
                    }
                }

                // 通知设置
                Section("🔔 通知设置") {
                    Toggle(isOn: $notifyEnabled) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("消息通知").font(.system(size: 15))
                            Text("新消息弹出通知").font(.system(size: 12)).foregroundColor(.velaTextSecondary)
                        }
                    }
                    Toggle(isOn: $notifySound) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("提示音").font(.system(size: 15))
                            Text("新消息播放提示音").font(.system(size: 12)).foregroundColor(.velaTextSecondary)
                        }
                    }
                    Toggle(isOn: $notifyPreview) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("通知预览").font(.system(size: 15))
                            Text("通知栏显示消息内容").font(.system(size: 12)).foregroundColor(.velaTextSecondary)
                        }
                    }
                }

                // 隐私设置
                Section("🔒 隐私设置") {
                    Toggle(isOn: $privacySearchable) {
                        VStack(alignment: .leading, spacing: 2) {
                            Text("允许被搜索").font(.system(size: 15))
                            Text("其他用户可以通过ID找到你").font(.system(size: 12)).foregroundColor(.velaTextSecondary)
                        }
                    }
                }

                // 账号
                Section("🛡️ 账号") {
                    HStack {
                        Text("当前账号").font(.system(size: 15))
                        Spacer()
                        Text(appState.userId.isEmpty ? "未登录" : appState.userId)
                            .font(.system(size: 14))
                            .foregroundColor(.velaTextSecondary)
                    }
                }

                // 关于
                Section("ℹ️ 关于") {
                    HStack {
                        Text("应用名称")
                        Spacer()
                        Text("Vela IM").foregroundColor(.velaTextSecondary)
                    }
                    HStack {
                        Text("版本")
                        Spacer()
                        Text("1.0").foregroundColor(.velaTextSecondary)
                    }
                }

                // 退出
                Section {
                    Button(action: handleLogout) {
                        Text("退出登录")
                            .foregroundColor(.velaError)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .navigationTitle("设置")
            .task { await loadConfig() }
            .onChange(of: compactMode) { _ in saveConfig() }
            .onChange(of: notifyEnabled) { _ in saveConfig() }
            .onChange(of: notifySound) { _ in saveConfig() }
            .onChange(of: notifyPreview) { _ in saveConfig() }
            .onChange(of: privacySearchable) { _ in saveConfig() }
        }
    }

    private func loadConfig() async {
        guard !appState.userId.isEmpty else { return }
        do {
            let config = try await APIClient.shared.getUserConfig(userId: appState.userId)
            await MainActor.run {
                if let v = config["notify.enabled"] { notifyEnabled = v == "true" }
                if let v = config["notify.sound"] { notifySound = v == "true" }
                if let v = config["notify.preview"] { notifyPreview = v == "true" }
                if let v = config["privacy.searchable"] { privacySearchable = v == "true" }
                if let v = config["display.compactMode"] { compactMode = v == "true" }
            }
        } catch { /* 使用默认值 */ }
    }

    private func saveConfig() {
        guard !appState.userId.isEmpty else { return }
        let configs = [
            ConfigEntry(key: "notify.enabled", value: String(notifyEnabled)),
            ConfigEntry(key: "notify.sound", value: String(notifySound)),
            ConfigEntry(key: "notify.preview", value: String(notifyPreview)),
            ConfigEntry(key: "privacy.searchable", value: String(privacySearchable)),
            ConfigEntry(key: "display.compactMode", value: String(compactMode)),
        ]
        Task {
            try? await APIClient.shared.saveUserConfig(userId: appState.userId, configs: configs)
        }
    }

    private func handleLogout() {
        WebSocketClient.shared.disconnect()
        appState.isLoggedIn = false
        appState.token = ""
        appState.userId = ""
    }
}
