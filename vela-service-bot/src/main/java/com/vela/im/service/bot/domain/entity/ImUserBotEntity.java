package com.vela.im.service.bot.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * <p>Title: ImUserBotEntity</p>
 * <p>Description: 用户-机器人订阅关系表，记录用户安装/启用的机器人。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Data
@TableName("vela_user_bot")
public class ImUserBotEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer appId;
    private String userId;
    private String botId;
    /** 0-禁用, 1-启用 */
    private Integer status;
    private Long createTime;
}
