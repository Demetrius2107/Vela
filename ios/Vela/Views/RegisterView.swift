import SwiftUI

struct RegisterView: View {
    @State private var userId = ""
    @State private var nickName = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMsg: String?
    @State private var successMsg: String?
    @Environment(\.dismiss) var dismiss

    var body: some View {
        ZStack {
            LinearGradient(colors: [.velaPrimary, Color(red: 0.49, green: 0.24, blue: 0.93)],
                          startPoint: .topLeading, endPoint: .bottomTrailing)
                .ignoresSafeArea()

            VStack(spacing: 20) {
                Spacer().frame(height: 60)

                Text("创建账号")
                    .font(.system(size: 28, weight: .bold))
                    .foregroundColor(.white)
                Text("加入 Vela IM")
                    .font(.system(size: 15))
                    .foregroundColor(.white.opacity(0.7))

                Spacer().frame(height: 30)

                VStack(spacing: 16) {
                    TextField("用户ID", text: $userId)
                        .textFieldStyle(.plain).padding(14).background(.white).cornerRadius(12)
                        .autocapitalization(.none)
                    TextField("昵称", text: $nickName)
                        .textFieldStyle(.plain).padding(14).background(.white).cornerRadius(12)
                    SecureField("密码（至少6位）", text: $password)
                        .textFieldStyle(.plain).padding(14).background(.white).cornerRadius(12)

                    if let error = errorMsg {
                        Text(error).font(.system(size: 13)).foregroundColor(.white)
                    }
                    if let success = successMsg {
                        Text(success).font(.system(size: 13)).foregroundColor(.white)
                    }

                    Button(action: handleRegister) {
                        Text(isLoading ? "注册中..." : "注 册")
                            .font(.system(size: 17, weight: .semibold))
                            .foregroundColor(.velaPrimary)
                            .frame(maxWidth: .infinity).padding(.vertical, 14)
                            .background(.white).cornerRadius(22)
                    }
                    .disabled(isLoading)
                }
                .padding(.horizontal, 32)

                Spacer()

                Button("已有账号？去登录") { dismiss() }
                    .font(.system(size: 14)).foregroundColor(.white.opacity(0.8))
                    .padding(.bottom, 40)
            }
        }
        .navigationBarHidden(true)
    }

    private func handleRegister() {
        guard !userId.isEmpty, !nickName.isEmpty, password.count >= 6 else {
            errorMsg = "请填写完整信息，密码至少6位"
            return
        }
        isLoading = true; errorMsg = nil
        Task {
            do {
                try await APIClient.shared.register(userId: userId, nickName: nickName, password: password)
                await MainActor.run { successMsg = "注册成功！" }
                try? await Task.sleep(nanoseconds: 1_500_000_000)
                await MainActor.run { dismiss() }
            } catch {
                await MainActor.run { errorMsg = error.localizedDescription }
            }
            await MainActor.run { isLoading = false }
        }
    }
}
