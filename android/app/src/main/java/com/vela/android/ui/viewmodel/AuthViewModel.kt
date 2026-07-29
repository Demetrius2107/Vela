package com.vela.android.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.vela.android.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val api = RetrofitClient.api

    fun login(userId: String, password: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = api.login(
                    com.vela.android.network.LoginReq(userId, password)
                )
                if (resp.isOk && resp.data != null) {
                    getApplication<android.app.Application>()
                        .getSharedPreferences("vela", 0)
                        .edit()
                        .putString("token", resp.data)
                        .putString("userId", userId)
                        .apply()
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun register(userId: String, nickName: String, password: String, onResult: (Boolean) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val resp = api.register(
                    com.vela.android.network.RegisterReq(userId, nickName, password)
                )
                onResult(resp.isOk)
            } catch (e: Exception) {
                onResult(false)
            }
        }
    }

    fun isLoggedIn(): Boolean {
        val prefs = getApplication<android.app.Application>()
            .getSharedPreferences("vela", 0)
        return prefs.getString("token", null) != null
    }

    fun getUserId(): String {
        return getApplication<android.app.Application>()
            .getSharedPreferences("vela", 0)
            .getString("userId", "") ?: ""
    }
}
