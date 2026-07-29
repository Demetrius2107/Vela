import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            ConversationListView()
                .tabItem {
                    Label("会话", systemImage: "message.fill")
                }
                .tag(0)

            ContactsView()
                .tabItem {
                    Label("通讯录", systemImage: "person.2.fill")
                }
                .tag(1)

            BotMarketView()
                .tabItem {
                    Label("Bot", systemImage: "robot.fill")
                }
                .tag(2)

            SettingsView()
                .tabItem {
                    Label("设置", systemImage: "gearshape.fill")
                }
                .tag(3)
        }
        .tint(.velaPrimary)
    }
}
