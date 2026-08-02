package com.vela.im.service.knowledge.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_document")
public class DocumentEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String title;
    private String content;
    private String summary;
    private String creatorId;
    private Long categoryId;
    private String tags;
    private Long createTime;
    private Long updateTime;
}
