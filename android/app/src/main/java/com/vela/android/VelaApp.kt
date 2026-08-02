package com.vela.android

import android.app.Application

class VelaApp : Application() {
    companion object {
        const val API_BASE_URL = "http://10.0.2.2:8888"
        const val WS_URL = "ws://10.0.2.2:19000"
    }
}
