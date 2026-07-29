import SwiftUI

struct FavoritesView: View {
    @Environment(\.dismiss) var dismiss

    var body: some View {
        NavigationStack {
            VStack(spacing: 16) {
                Spacer()
                Text("⭐").font(.system(size: 48))
                Text("我的收藏")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(.velaTextPrimary)
                Text("还没有收藏任何消息")
                    .font(.system(size: 14))
                    .foregroundColor(.velaTextSecondary)
                Text("在聊天中长按消息可以收藏")
                    .font(.system(size: 13))
                    .foregroundColor(.velaTextSecondary)
                Spacer()
            }
            .frame(maxWidth: .infinity)
            .navigationTitle("我的收藏")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("关闭") { dismiss() }
                }
            }
        }
    }
}
