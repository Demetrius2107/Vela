import SwiftUI

struct LoginView: View {
    @EnvironmentObject var appState: AppState
    @State private var userId = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMsg: String?

    var body: some View {
        ZStack {
            // 背景渐变
            LinearGradient(colors: [.velaPrimary, Color(red: 0.49, green: 0.24, blue: 0.93)],
                          startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            VStack(spacing: 24) {
                Spacer()

                // Logo
                VStack(spacing: 8) {
                    Text("V")
                        .font(.system(size: 48, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 80, height: 80)
                        .background(.white.opacity(0.15))
                        .cornerRadius(22)
                    Text("Vela IM")
                        .font(.system(size: 28, weight: .bold))
                        .foregroundColor(.white)
                    Text("欢迎回来")
                        .font(.system(size: 15))
                        .foregroundColor(.white.opacity(0.7))
                }

                Spacer().frame(height: 40)

                // 登录表单
                VStack(spacing: 16) {
                    TextField("用户ID", text: $userId)
                        .textFieldStyle(.plain)
                        .padding(14)
                        .background(.white)
                        .cornerRadius(12)
                        .autocapitalization(.none)

                    SecureField("密码", text: $password)
                        .textFieldStyle(.plain)
                        .padding(14)
                        .background(.white)
                        .cornerRadius(12)

                    if let error = errorMsg {
                        Text(error)
                            .font(.system(size: 13))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    Button(action: handleLogin) {
                        Text(isLoading ? "登录中..." : "登 录")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.velaPrimary)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 14)
                            .background(.white)
                            .cornerRadius(22)
                    }
                    .disabled(isLoading)
                }
                .padding(.horizontal, 32)

                Spacer()

                // 注册入口
                NavigationLink(destination: RegisterView()) {
                    Text("没有账号？去注册")
                        .font(.system(size: 14))
                        .foregroundColor(.white.opacity(0.8))
                }
                .padding(.bottom, 40)
            }
        }
    }

    private func handleLogin() {
        guard !userId.isEmpty, !password.isEmpty else {
            errorMsg = "请输入用户ID和密码"
            return
        }
        isLoading = true
        errorMsg = nil

        Task {
            do {
                let token = try await APIClient.shared.login(userId: userId, password: password)
                await MainActor.run {
                    appState.token = token
                    appState.userId = userId
                    appState.isLoggedIn = true
                    WebSocketClient.shared.connect(userId: userId)
                }
            } catch {
                await MainActor.run {
                    errorMsg = error.localizedDescription
                }
            }
            await MainActor.run { isLoading = false }
        }
    }
}
