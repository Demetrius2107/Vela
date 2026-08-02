import SwiftUI

@main
struct VelaApp: App {
    @StateObject private var appState = AppState()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(appState)
        }
    }
}

class AppState: ObservableObject {
    @Published var isLoggedIn: Bool = false
    @Published var userId: String = ""
    @Published var token: String = ""
    @Published var serverUrl: String = "http://localhost:8888"
    @Published var wsUrl: String = "ws://localhost:19000"
}
