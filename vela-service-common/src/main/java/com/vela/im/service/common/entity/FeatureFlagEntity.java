package com.vela.im.service.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: FeatureFlagEntity</p>
 * <p>Description: 功能开关实体，控制功能灰度发布/紧急下线/白名单。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Data
@TableName("vela_feature_flag")
public class FeatureFlagEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer appId;
    /** 功能标识，如: bot_market, ai_assistant, video_call */
    private String flagKey;
    /** 功能名称，如: Bot市场 */
    private String flagName;
    /** 0-关闭, 1-开启 */
    private Integer enabled;
    /** 灰度白名单，JSON数组 ["user1","user2"] */
    private String userWhitelist;
    /** 描述 */
    private String description;
    private Long updateTime;
}
