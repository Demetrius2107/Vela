package com.vela.im.service.group.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_group_poll_option")
public class ImGroupPollOptionEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long pollId;
    private String text;
    private Integer sortOrder;
}
