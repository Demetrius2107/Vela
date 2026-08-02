package com.vela.im.service.group.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_group_file")
public class ImGroupFileEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String groupId;
    private String uploaderId;
    private String fileName;
    private String fileUrl;
    private Long fileSize;
    private String fileType;
    private Long createTime;
}
