import SwiftUI

struct ContactsView: View {
    @State private var contacts: [FriendData] = []
    @State private var searchText = ""

    var filteredContacts: [FriendData] {
        if searchText.isEmpty { return contacts }
        return contacts.filter { ($0.nickName ?? $0.toId ?? "").localizedCaseInsensitiveContains(searchText) }
    }

    var body: some View {
        NavigationStack {
            List {
                if filteredContacts.isEmpty {
                    VStack(spacing: 12) {
                        Text("👥").font(.system(size: 48))
                        Text("暂无好友").foregroundColor(.velaTextSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 60)
                    .listRowBackground(Color.clear)
                } else {
                    ForEach(filteredContacts) { contact in
                        ContactRow(contact: contact)
                            .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                            .listRowSeparator(.hidden)
                    }
                }
            }
            .listStyle(.plain)
            .navigationTitle("通讯录")
            .searchable(text: $searchText, prompt: "搜索好友")
            .refreshable { await loadContacts() }
            .task { await loadContacts() }
        }
    }

    private func loadContacts() async {
        guard !AppState.shared.userId.isEmpty else { return }
        do {
            let friends = try await APIClient.shared.getFriends(userId: AppState.shared.userId)
            await MainActor.run { contacts = friends }
        } catch { /* 离线可用 */ }
    }
}

struct ContactRow: View {
    let contact: FriendData
    let colorIndex: Int

    init(contact: FriendData) {
        self.contact = contact
        self.colorIndex = abs(contact.toId?.hashValue ?? 0) % convColors.count
    }

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                Circle()
                    .fill(convColors[colorIndex].opacity(0.15))
                    .frame(width: 46, height: 46)
                Text((contact.nickName ?? contact.toId ?? "?").prefix(1))
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(convColors[colorIndex])
            }

            VStack(alignment: .leading, spacing: 3) {
                Text(contact.nickName ?? contact.toId ?? "未知")
                    .font(.system(size: 15, weight: .semibold))
                    .foregroundColor(.velaTextPrimary)
                if let sig = contact.selfSignature, !sig.isEmpty {
                    Text(sig)
                        .font(.system(size: 13))
                        .foregroundColor(.velaTextSecondary)
                        .lineLimit(1)
                }
            }
        }
        .padding(.vertical, 6)
    }
}
