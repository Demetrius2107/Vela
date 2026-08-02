package com.vela.im.service.group.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_group_poll")
public class ImGroupPollEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String groupId;
    private String title;
    private String creatorId;

    /** 0-单选, 1-多选 */
    private Integer multipleChoice;

    /** 0-进行中, 1-已结束, 2-已删除 */
    private Integer status;

    private Long createTime;
    private Long endTime;
}
