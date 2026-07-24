package com.vela.im.shared.constants;

/**
 * <p>Title: ImConstants</p>
 * <p>Description: IM 系统全局常量定义，包含 TraceId、Channel 属性键、Redis/RabbitMQ 队列名、回调指令和序列 Key。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2025-03-03
 * @updateTime 2026-07-24
 *
 * Copyright © 2026 wanqiu All rights reserved
 */
public class ImConstants {

    // ==================== TraceId ====================

    /**
     * 全链路追踪 ID 在 MDC/HTTP Header/MQ Header 中的 Key
     */
    public static class TraceId {
        public static final String TRACE_ID_KEY = "traceId";
        public static final String HTTP_HEADER_NAME = "X-Trace-Id";
        public static final String MQ_HEADER_NAME = "x-trace-id";
    }

    // ==================== Channel 属性键 ====================

    /** Channel 属性 — 用户 ID */
    public static final String USER_ID = "userId";

    /** Channel 属性 — 应用 ID */
    public static final String APP_ID = "appId";

    /** Channel 属性 — 客户端类型 */
    public static final String CLIENT_TYPE = "clientType";

    /** Channel 属性 — 设备 IMEI */
    public static final String IMEI = "imei";

    /** Channel 属性 — 客户端类型 + IMEI 组合键 */
    public static final String CLIENT_IMEI = "clientImei";

    /** Channel 属性 — 最后读取时间 */
    public static final String READ_TIME = "readTime";

    // ==================== ZooKeeper 路径 ====================

    /** ZooKeeper 根路径 */
    public static final String IM_CORE_ZK_ROOT = "/im-coreRoot";

    /** ZooKeeper TCP 节点路径 */
    public static final String IM_CORE_ZK_ROOT_TCP = "/tcp";

    /** ZooKeeper WebSocket 节点路径 */
    public static final String IM_CORE_ZK_ROOT_WEB = "/web";

    // ==================== Redis 常量 ====================

    /**
     * <p>Title: Redis</p>
     * <p>Description: Redis Key 格式常量，定义各类缓存 Key 前缀。</p>
     */
    public static class Redis {

        /** 用户签名，格式：appId:userSign: */
        public static final String USER_SIGN = "userSign";

        /** 用户上线通知 Channel */
        public static final String USER_LOGIN_CHANNEL = "signal/channel/LOGIN_USER_INNER_QUEUE";

        /** 用户 Session 前缀，appId + :userSession: + userId，如 10000:userSession:lld */
        public static final String USER_SESSION_PREFIX = ":userSession:";

        /** 缓存客户端消息防重，格式：appId + :cacheMessage: + messageId */
        public static final String CACHE_MESSAGE = "cacheMessage";

        /** 离线消息 Key 前缀 */
        public static final String OFFLINE_MESSAGE = "offlineMessage";

        /** 序列号 Key 前缀 */
        public static final String SEQ_PREFIX = "seq";

        /** 用户订阅列表，格式：appId + :subscribe: + userId，Hash 结构，field 为订阅者 */
        public static final String SUBSCRIBE = "subscribe";

        /** 用户自定义在线状态，格式：appId + :userCustomerStatus: + userId */
        public static final String USER_CUSTOM_STATUS = "userCustomerStatus";
    }

    // ==================== RabbitMQ 常量 ====================

    /**
     * <p>Title: RabbitMQ</p>
     * <p>Description: RabbitMQ 队列名常量。</p>
     */
    public static class RabbitMQ {

        public static final String IM_2_USER_SERVICE = "pipeline2UserService";
        public static final String IM_2_MESSAGE_SERVICE = "pipeline2MessageService";
        public static final String IM_2_GROUP_SERVICE = "pipeline2GroupService";
        public static final String IM_2_FRIENDSHIP_SERVICE = "pipeline2FriendshipService";
        public static final String MESSAGE_SERVICE_2_IM = "messageService2Pipeline";
        public static final String GROUP_SERVICE_2_IM = "GroupService2Pipeline";
        public static final String FRIENDSHIP_2_IM = "friendShip2Pipeline";
        public static final String STORE_P2P_MESSAGE = "storeP2PMessage";
        public static final String STORE_GROUP_MESSAGE = "storeGroupMessage";
    }

    // ==================== 回调指令 ====================

    /**
     * <p>Title: CallbackCommand</p>
     * <p>Description: 回调事件指令常量，定义各类业务事件的回调命令字。</p>
     */
    public static class CallbackCommand {
        public static final String MODIFY_USER_AFTER = "user.modify.after";
        public static final String CREATE_GROUP_AFTER = "group.create.after";
        public static final String UPDATE_GROUP_AFTER = "group.update.after";
        public static final String DESTROY_GROUP_AFTER = "group.destory.after";
        public static final String TRANSFER_GROUP_AFTER = "group.transfer.after";
        public static final String GROUP_MEMBER_ADD_BEFORE = "group.member.add.before";
        public static final String GROUP_MEMBER_ADD_AFTER = "group.member.add.after";
        public static final String GROUP_MEMBER_DELETE_AFTER = "group.member.delete.after";
        public static final String ADD_FRIEND_BEFORE = "friend.add.before";
        public static final String ADD_FRIEND_AFTER = "friend.add.after";
        public static final String UPDATE_FRIEND_BEFORE = "friend.update.before";
        public static final String UPDATE_FRIEND_AFTER = "friend.update.after";
        public static final String DELETE_FRIEND_AFTER = "friend.delete.after";
        public static final String ADD_BLACK_AFTER = "black.add.after";
        public static final String DELETE_BLACK = "black.delete";
        public static final String SEND_MESSAGE_AFTER = "message.send.after";
        public static final String SEND_MESSAGE_BEFORE = "message.send.before";
    }

    // ==================== 序列号 Key ====================

    /**
     * <p>Title: Seq</p>
     * <p>Description: Redis 序列号 Key 常量，用于生成各领域的单调递增序列。</p>
     */
    public static class Seq {
        public static final String MESSAGE = "messageSeq";
        public static final String GROUP_MESSAGE = "groupMessageSeq";
        public static final String FRIENDSHIP = "friendshipSeq";
        public static final String FRIENDSHIP_REQUEST = "friendshipRequestSeq";
        public static final String FRIENDSHIP_GROUP = "friendshipGrouptSeq";
        public static final String GROUP = "groupSeq";
        public static final String CONVERSATION = "conversationSeq";
    }
}
