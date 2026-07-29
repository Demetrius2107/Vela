package com.vela.android.network

import retrofit2.http.*

interface VelaApi {

    // ===== 认证 =====
    @POST("/v1/user/login")
    suspend fun login(@Body req: LoginReq): ApiResult<String>

    @POST("/v1/user/register")
    suspend fun register(@Body req: RegisterReq): ApiResult<String>

    // ===== 消息 =====
    @POST("/v1/message/send")
    suspend fun sendMessage(@Body req: SendMessageReq): ApiResult<SendMessageResp>

    @POST("/v1/message/syncOfflineMessage")
    suspend fun syncOffline(@Body req: SyncReq): ApiResult<SyncResp>

    // ===== 好友 =====
    @GET("/v1/friend/getAllFriend")
    suspend fun getFriends(@Query("appId") appId: Int, @Query("fromId") fromId: String): ApiResult<List<FriendData>>

    // ===== 用户配置 (UserConfig) =====
    @GET("/v1/user/config/get")
    suspend fun getUserConfig(@Query("appId") appId: Int,
                              @Query("userId") userId: String,
                              @Query("clientType") clientType: String = "android"): ApiResult<Map<String, String>>

    @POST("/v1/user/config/save")
    suspend fun saveUserConfig(@Query("appId") appId: Int,
                               @Query("userId") userId: String,
                               @Query("clientType") clientType: String = "android",
                               @Body configs: List<ConfigEntry>): ApiResult<Unit>

    // ===== 功能开关 (FeatureFlag) =====
    @GET("/v1/feature/flags")
    suspend fun getFeatureFlags(@Query("appId") appId: Int,
                                @Query("userId") userId: String): ApiResult<Map<String, Boolean>>

    // ===== Bot 市场 =====
    @GET("/v1/bot/market/list")
    suspend fun getBotMarket(@Query("appId") appId: Int,
                             @Query("category") category: String? = null,
                             @Query("keyword") keyword: String? = null): ApiResult<List<BotData>>

    @POST("/v1/bot/market/install")
    suspend fun installBot(@Query("appId") appId: Int,
                           @Query("userId") userId: String,
                           @Query("botId") botId: String): ApiResult<Unit>

    @GET("/v1/bot/market/my")
    suspend fun getMyBots(@Query("appId") appId: Int,
                          @Query("userId") userId: String): ApiResult<List<BotData>>
}

// ===== 请求/响应体 =====
data class ConfigEntry(val key: String, val value: String)

data class BotData(
    val id: Long?,
    val botId: String?,
    val botName: String?,
    val description: String?,
    val category: String?,
    val status: Int?
)

data class SyncResp(
    val maxSequence: Long,
    val dataList: List<OfflineMessage>?,
    val completed: Boolean = true
)

data class FriendData(
    val toId: String?,
    val nickName: String?,
    val photo: String?,
    val selfSignature: String?,
    val status: Int?
)
