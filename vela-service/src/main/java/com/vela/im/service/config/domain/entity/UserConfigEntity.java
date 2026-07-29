package com.vela.im.service.config.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: UserConfigEntity</p>
 * <p>Description: 用户个人设置，按 key-value 存储，支持分端差异化配置。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Data
@TableName("vela_user_config")
public class UserConfigEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer appId;
    private String userId;
    /** 配置键，如: notify.enabled, privacy.searchable, display.compactMode */
    private String configKey;
    /** 配置值，JSON 字符串 */
    private String configValue;
    /** 客户端类型: web / mobile / all */
    private String clientType;
    private Long updateTime;
}
