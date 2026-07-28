package com.vela.im.shared.types.enums;

/**
 * 通用状态常量，统一各模块中 status/forbiddenFlag/silentFlag 等字段的魔法值。
 */
public class StatusConstants {

    // ====== 通用开关 0/1 ======
    public static final int OFF = 0;
    public static final int ON = 1;

    // ====== 逻辑删除 ======
    /** 正常 */
    public static final int NORMAL = 0;
    /** 已删除 */
    public static final int DELETED = 1;

    // ====== 禁用标志 ======
    /** 未禁用 */
    public static final int NOT_FORBIDDEN = 0;
    /** 已禁用 */
    public static final int FORBIDDEN = 1;

    // ====== 处理状态（审批/待办等）=====
    /** 待处理 */
    public static final int PENDING = 0;
    /** 已通过/已完成 */
    public static final int DONE = 1;
    /** 已拒绝 */
    public static final int REJECTED = 2;
    /** 已取消/已撤回 */
    public static final int CANCELLED = 3;

    // ====== 群成员角色 ======
    /** 普通成员 */
    public static final int ROLE_MEMBER = 0;
    /** 管理员 */
    public static final int ROLE_ADMIN = 1;
    /** 群主 */
    public static final int ROLE_OWNER = 2;

    // ====== 群状态 ======
    /** 正常 */
    public static final int GROUP_ACTIVE = 0;
    /** 已解散 */
    public static final int GROUP_DISSOLVED = 1;

    // ====== Bot 状态 ======
    /** 启用 */
    public static final int BOT_ENABLED = 1;
    /** 禁用 */
    public static final int BOT_DISABLED = 0;

    // ====== 分页 ======
    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 20;
}
