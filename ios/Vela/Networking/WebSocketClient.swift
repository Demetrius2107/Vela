import Foundation
import Network

// MARK: - WebSocket 客户端
class WebSocketClient: NSObject, URLSessionWebSocketDelegate {
    static let shared = WebSocketClient()

    private var webSocketTask: URLSessionWebSocketTask?
    private var session: URLSession!
    private var isConnected = false

    var onMessage: ((String) -> Void)?
    var onConnected: (() -> Void)?
    var onDisconnected: ((String?) -> Void)?

    override init() {
        super.init()
        session = URLSession(configuration: .default, delegate: self, delegateQueue: OperationQueue())
    }

    func connect(userId: String) {
        let url = URL(string: "\(AppState.shared.wsUrl)?userId=\(userId)&appId=10000")!
        webSocketTask = session.webSocketTask(with: url)
        webSocketTask?.resume()
        receiveMessage()
    }

    func disconnect() {
        webSocketTask?.cancel(with: .normalClosure, reason: nil)
        webSocketTask = nil
        isConnected = false
    }

    func send(_ text: String) {
        guard isConnected else { return }
        webSocketTask?.send(.string(text)) { error in
            if let error = error {
                print("WS send error: \(error)")
            }
        }
    }

    private func receiveMessage() {
        webSocketTask?.receive { [weak self] result in
            DispatchQueue.main.async {
                switch result {
                case .success(let message):
                    if case .string(let text) = message {
                        self?.onMessage?(text)
                    }
                    self?.receiveMessage()
                case .failure(let error):
                    print("WS receive error: \(error)")
                    self?.isConnected = false
                    self?.onDisconnected?(error.localizedDescription)
                }
            }
        }
    }

    // MARK: - URLSessionWebSocketDelegate
    func urlSession(_ session: URLSession, webSocketTask: URLSessionWebSocketTask, didOpenWithProtocol protocol: String?) {
        isConnected = true
        DispatchQueue.main.async { [weak self] in
            self?.onConnected?()
        }
    }

    func urlSession(_ session: URLSession, webSocketTask: URLSessionWebSocketTask, didCloseWith closeCode: URLSessionWebSocketTask.CloseCode, reason: Data?) {
        isConnected = false
        let reasonStr = reason.flatMap { String(data: $0, encoding: .utf8) }
        DispatchQueue.main.async { [weak self] in
            self?.onDisconnected?(reasonStr)
        }
    }
}

// MARK: - AppState 单例
extension AppState {
    static let shared = AppState()
}
