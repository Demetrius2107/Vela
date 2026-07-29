import SwiftUI

struct ChatView: View {
    let conversation: Conversation
    @State private var messages: [ChatMessage] = []
    @State private var inputText = ""
    @State private var isLoading = false

    var body: some View {
        VStack(spacing: 0) {
            // 消息区
            ScrollViewReader { proxy in
                ScrollView {
                    LazyVStack(spacing: 10) {
                        ForEach(messages) { msg in
                            MessageBubbleView(msg: msg, color: convColors[conversation.colorIndex])
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 12)
                }
                .background(
                    LinearGradient(colors: [
                        convColors[conversation.colorIndex].opacity(0.04),
                        Color(.systemGray6)
                    ], startPoint: .top, endPoint: .bottom)
                )
                .onChange(of: messages.count) { _ in
                    if let last = messages.last {
                        withAnimation { proxy.scrollTo(last.id) }
                    }
                }
            }

            // 输入区
            VStack(spacing: 0) {
                Divider()
                HStack(spacing: 8) {
                    TextField("输入消息...", text: $inputText)
                        .textFieldStyle(.plain)
                        .padding(10)
                        .background(Color(.systemGray6))
                        .cornerRadius(12)
                        .overlay(
                            RoundedRectangle(cornerRadius: 12)
                                .stroke(Color(.systemGray5), lineWidth: 1)
                        )

                    Button(action: sendMessage) {
                        Text("发送")
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 20)
                            .padding(.vertical, 10)
                            .background(LinearGradient.velaPrimaryGradient)
                            .cornerRadius(12)
                    }
                    .disabled(inputText.trimmingCharacters(in: .whitespaces).isEmpty)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(Color(.systemBackground))
            }
        }
        .navigationTitle(conversation.name)
        .navigationBarTitleDisplayMode(.inline)
        .task { await loadMessages() }
    }

    private func loadMessages() async {
        isLoading = true
        // TODO: 从服务端拉取消息
        try? await Task.sleep(nanoseconds: 500_000_000)
        messages = [
            ChatMessage(id: "1", content: "你好，最近怎么样？", isSelf: false, time: "10:00"),
            ChatMessage(id: "2", content: "挺好的，刚忙完一个项目", isSelf: true, time: "10:01"),
        ]
        isLoading = false
    }

    private func sendMessage() {
        let text = inputText.trimmingCharacters(in: .whitespaces)
        guard !text.isEmpty else { return }
        let msg = ChatMessage(id: UUID().uuidString, content: text, isSelf: true,
                              time: Date().formatted(.dateTime.hour().minute()))
        withAnimation { messages.append(msg) }
        inputText = ""
    }
}

struct MessageBubbleView: View {
    let msg: ChatMessage
    let color: Color

    var body: some View {
        HStack {
            if msg.isSelf { Spacer(minLength: 60) }

            VStack(alignment: msg.isSelf ? .trailing : .leading, spacing: 2) {
                Text(msg.content)
                    .font(.system(size: 15))
                    .foregroundColor(msg.isSelf ? .white : Color(.label))
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(
                        msg.isSelf
                            ? AnyView(LinearGradient.velaMessageBubble)
                            : AnyView(Color(.systemBackground))
                    )
                    .cornerRadius(msg.isSelf ? 18 : 18, corners: msg.isSelf ? [.topLeft, .bottomLeft, .bottomRight] : [.topRight, .bottomLeft, .bottomRight])
                    .shadow(color: .black.opacity(msg.isSelf ? 0.12 : 0.04), radius: 4, x: 0, y: 2)

                Text(msg.time)
                    .font(.system(size: 11))
                    .foregroundColor(.velaTextSecondary)
                    .padding(.horizontal, 4)
            }

            if !msg.isSelf { Spacer(minLength: 60) }
        }
    }
}

// 指定圆角
extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape(RoundedCorner(radius: radius, corners: corners))
    }
}

struct RoundedCorner: Shape {
    var radius: CGFloat
    var corners: UIRectCorner

    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners,
                                cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}
