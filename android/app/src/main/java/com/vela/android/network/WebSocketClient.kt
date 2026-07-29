package com.vela.android.network

import android.util.Log
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import okhttp3.*

class WebSocketClient {

    companion object {
        private const val TAG = "VelaWS"
        private const val WS_URL = "ws://10.0.2.2:19000"
    }

    private val client = OkHttpClient()
    private var ws: WebSocket? = null
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    fun connect(userId: String) {
        val request = Request.Builder()
            .url("$WS_URL?userId=$userId")
            .build()

        client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket connected")
            }

            override fun onMessage(ws: WebSocket, text: String) {
                _messages.trySend(text)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "WebSocket error: ${t.message}")
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "WebSocket closed: $reason")
            }
        })
    }

    fun send(data: String) {
        ws?.send(data) ?: Log.w(TAG, "WebSocket not connected")
    }

    fun disconnect() {
        ws?.close(1000, "user disconnect")
        ws = null
    }
}
