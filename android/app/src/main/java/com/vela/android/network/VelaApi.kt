package com.vela.android.network

import retrofit2.http.*

interface VelaApi {

    @POST("/v1/user/login")
    suspend fun login(@Body req: LoginReq): ApiResult<String>

    @POST("/v1/user/register")
    suspend fun register(@Body req: RegisterReq): ApiResult<String>

    @POST("/v1/message/send")
    suspend fun sendMessage(@Body req: SendMessageReq): ApiResult<SendMessageResp>

    @POST("/v1/message/syncOfflineMessage")
    suspend fun syncOffline(@Body req: SyncReq): ApiResult<SyncResp>
}

data class SyncResp(
    val maxSequence: Long,
    val dataList: List<OfflineMessage>?,
    val completed: Boolean = true
)
