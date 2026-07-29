package com.vela.android.network

data class ApiResult<T>(
    val code: Int,
    val msg: String,
    val data: T?
) {
    val isOk: Boolean get() = code == 200
}

data class SendMessageReq(
    val fromId: String,
    val toId: String,
    val appId: Int = 10000,
    val messageBody: String
)

data class SendMessageResp(
    val messageKey: Long,
    val messageTime: Long
)

data class LoginReq(
    val userId: String,
    val password: String
)

data class RegisterReq(
    val userId: String,
    val nickName: String,
    val password: String
)

data class SyncReq(
    val operater: String,
    val appId: Int = 10000,
    val lastSequence: Long = 0,
    val maxLimit: Int = 100
)

data class OfflineMessage(
    val messageKey: Long,
    val fromId: String,
    val toId: String,
    val messageBody: String?,
    val messageSequence: Long,
    val messageTime: Long,
    val conversationType: Int?
)
