import SwiftUI

// MARK: - Vela 品牌色（与 Web/Android 一致）
extension Color {
    static let velaPrimary = Color(red: 0.31, green: 0.43, blue: 0.97)       // #4F6EF7
    static let velaPrimaryDark = Color(red: 0.23, green: 0.34, blue: 0.85)   // #3B57D9
    static let velaPrimaryLight = Color(red: 0.42, green: 0.53, blue: 0.98)  // #6B86FA
    static let velaSuccess = Color(red: 0.13, green: 0.77, blue: 0.37)       // #22C55E
    static let velaWarning = Color(red: 0.96, green: 0.62, blue: 0.04)       // #F59E0B
    static let velaError = Color(red: 0.94, green: 0.27, blue: 0.27)         // #EF4444
    static let velaBg = Color(red: 0.94, green: 0.95, blue: 0.96)            // #F0F2F5
    static let velaSurface = Color(red: 0.97, green: 0.98, blue: 1.0)        // #F8F9FF
    static let velaTextPrimary = Color(red: 0.10, green: 0.10, blue: 0.18)   // #1A1A2E
    static let velaTextSecondary = Color(red: 0.60, green: 0.60, blue: 0.60) // #999
    static let velaDivider = Color(red: 0.91, green: 0.91, blue: 0.91)       // #E8E8E8
}

// MARK: - 会话个性色（8色循环）
let convColors: [Color] = [
    Color(red: 0.31, green: 0.43, blue: 0.97),  // 靛蓝
    Color(red: 0.13, green: 0.77, blue: 0.37),  // 翠绿
    Color(red: 0.94, green: 0.27, blue: 0.27),  // 玫瑰红
    Color(red: 0.96, green: 0.62, blue: 0.04),  // 琥珀
    Color(red: 0.55, green: 0.35, blue: 0.96),  // 紫色
    Color(red: 0.02, green: 0.71, blue: 0.83),  // 青色
    Color(red: 0.93, green: 0.28, blue: 0.60),  // 粉红
    Color(red: 0.08, green: 0.72, blue: 0.65),  // 蓝绿
]

// MARK: - Gradient 扩展
extension LinearGradient {
    static let velaPrimaryGradient = LinearGradient(
        colors: [.velaPrimary, Color(red: 0.49, green: 0.24, blue: 0.93)],
        startPoint: .leading, endPoint: .trailing
    )

    static let velaMessageBubble = LinearGradient(
        colors: [.velaPrimary, Color(red: 0.49, green: 0.24, blue: 0.93)],
        startPoint: .topLeading, endPoint: .bottomTrailing
    )
}

// MARK: - View 修饰符扩展
extension View {
    func velaCard() -> some View {
        self
            .background(Color.white)
            .cornerRadius(14)
            .shadow(color: .black.opacity(0.04), radius: 8, x: 0, y: 2)
    }

    func velaButton() -> some View {
        self
            .font(.system(size: 16, weight: .semibold))
            .foregroundColor(.white)
            .frame(height: 44)
            .background(LinearGradient.velaPrimaryGradient)
            .cornerRadius(22)
    }
}
