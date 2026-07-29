package com.vela.im.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <p>Title: ImServerProperties</p>
 * <p>Description: 应用配置类，映射 application.yml 中 appconfig 前缀的配置项，包含路由/回调/多端登录等开关。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @createTime 2025-03-03
 * @updateTime 2026-07-24
 * <p>
 * Copyright © 2026 wanqiu All rights reserved
 * @since 1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "appconfig")
public class ImServerProperties {

    /**
     * 私钥，用于用户签名校验
     */
    private String privateKey;

    /**
     * ZooKeeper 连接地址
     */
    private String zookeeperAddr;

    /**
     * ZooKeeper 连接超时时间（毫秒）
     */
    private Integer zookeeperConnectTimeOut;

    /**
     * 路由策略：1-轮询，2-随机，3-一致性哈希
     */
    private Integer imRouteWay;

    /**
     * 一致性哈希算法：1-TreeMap，2-自定义Map
     */
    private Integer consistentHashWay;

    /**
     * TCP端口
     */
    private Integer tcpPort;

    /**
     * WebSocket端口
     */
    private Integer webSocketPort;

    /**
     * 是否需要开启WebSocket
     */
    private Boolean needWebSocket;

    /**
     * 登录模式：1-单端，2-双端，3-多端，4-不限制
     */
    private Integer loginModel;

    /**
     * 消息可撤回时间（毫秒），默认 120000ms (2分钟)
     */
    private Long messageRecallTimeOut = 120000L;

    /**
     * 消息撤回时钟偏差容忍度（毫秒），默认 5000ms (5秒)
     * 客户端与服务端时间差在此范围内仍然允许撤回。
     */
    private Long messageRecallClockSkewTolerance = 5000L;

    /**
     * 群最大成员数量
     */
    private Integer groupMaxMemberCount;

    /**
     * 发送消息是否校验关系链
     */
    private boolean sendMessageCheckFriend;

    /**
     * 发送消息是否校验黑名单
     */
    private boolean sendMessageCheckBlack;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 回调配置
     */
    private CallbackConfig callback = new CallbackConfig();

    /**
     * <p>Title: CallbackConfig</p>
     * <p>Description: 回调配置，控制各类事件是否触发回调。</p>
     */
    @Data
    public static class CallbackConfig {

        /**
         * 用户资料变更后回调开关
         */
        private boolean modifyUserAfterCallback;

        /**
         * 添加好友后回调开关
         */
        private boolean addFriendAfterCallback;

        /**
         * 添加好友前回调开关
         */
        private boolean addFriendBeforeCallback;

        /**
         * 修改好友后回调开关
         */
        private boolean modifyFriendAfterCallback;

        /**
         * 删除好友后回调开关
         */
        private boolean deleteFriendAfterCallback;

        /**
         * 添加黑名单后回调开关
         */
        private boolean addFriendShipBlackAfterCallback;

        /**
         * 删除黑名单后回调开关
         */
        private boolean deleteFriendShipBlackAfterCallback;

        /**
         * 创建群聊后回调开关
         */
        private boolean createGroupAfterCallback;

        /**
         * 修改群聊后回调开关
         */
        private boolean modifyGroupAfterCallback;

        /**
         * 解散群聊后回调开关
         */
        private boolean destroyGroupAfterCallback;

        /**
         * 删除群成员后回调开关
         */
        private boolean deleteGroupMemberAfterCallback;

        /**
         * 拉人入群前回调开关
         */
        private boolean addGroupMemberBeforeCallback;

        /**
         * 拉人入群后回调开关
         */
        private boolean addGroupMemberAfterCallback;

        /**
         * 发送单聊消息后回调开关
         */
        private boolean sendMessageAfterCallback;

        /**
         * 发送单聊消息前回调开关
         */
        private boolean sendMessageBeforeCallback;

        /**
         * 发送群聊消息后回调开关
         */
        private boolean sendGroupMessageAfterCallback;

        /**
         * 发送群聊消息前回调开关
         */
        private boolean sendGroupMessageBeforeCallback;
    }

    /**
     * 会话删除同步模式：1-多端同步
     */
    private Integer deleteConversationSyncMode;

    /**
     * 离线消息最大存储条数
     */
    private Integer offlineMessageCount;

    /**
     * 消息体最大字节数，默认 65536 (64KB)
     */
    private Integer messageMaxSize = 65536;

    /**
     * 每秒最大消息发送条数，默认 20
     */
    private Integer messageRateLimit = 20;

    /**
     * 消息时间最大偏差（毫秒），默认 5 分钟
     */
    private Long messageTimeMaxDeviation = 300000L;

    /**
     * 消息重试配置
     */
    private RetryConfig retry = new RetryConfig();

    /**
     * 文件存储配置
     */
    private FileConfig file = new FileConfig();

    /**
     * <p>Title: RetryConfig</p>
     * <p>Description: 消息发送重试配置，使用指数退避策略。</p>
     */
    @Data
    public static class RetryConfig {

        /**
         * 最大重试次数（含首次尝试），默认 3
         */
        private int maxRetries = 3;

        /**
         * 初始延迟（毫秒），默认 100ms
         */
        private long baseDelayMs = 100L;

        /**
         * 最大延迟（毫秒），默认 2000ms
         */
        private long maxDelayMs = 2000L;
    }

    /**
     * <p>Title: FileConfig</p>
     * <p>Description: 文件存储配置，控制上传目录、大小限制、允许类型。</p>
     */
    @Data
    public static class FileConfig {

        /** 文件存储根目录 */
        private String uploadDir = "./uploads";

        /** 单个文件最大字节数，默认 100MB */
        private long maxFileSize = 104_857_600L;

        /** 图片最大字节数，默认 20MB */
        private long maxImageSize = 20_971_520L;

        /** 允许的图片扩展名 */
        private String imageExtensions = "jpg,jpeg,png,gif,webp,bmp";
    }

}
