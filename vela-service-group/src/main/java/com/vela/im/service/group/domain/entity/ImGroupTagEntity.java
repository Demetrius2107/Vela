package com.vela.im.service.group.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_group_tag")
public class ImGroupTagEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String userId;
    private String name;
    private String color;
    private Long createTime;
}
