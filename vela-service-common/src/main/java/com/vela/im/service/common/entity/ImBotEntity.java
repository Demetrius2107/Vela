package com.vela.im.service.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_bot")
public class ImBotEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer appId;
    private String botId;
    private String botName;
    private String botAvatar;
    private String description;
    private String webhookUrl;
    private String apiKey;
    /** 0-禁用, 1-启用 */
    private Integer status;
    /** Bot 分类，如：工具/娱乐/办公/AI */
    private String category;
    /** 标签，逗号分隔 */
    private String tags;
    private Long createTime;
}
