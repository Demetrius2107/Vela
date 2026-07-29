package com.vela.im.service.office.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_todo")
public class TodoEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String userId;
    private String title;
    private String description;

    /** 0-待办 1-进行中 2-已完成 3-已取消 */
    private Integer status;

    /** 优先级 1-普通 2-重要 3-紧急 */
    private Integer priority;

    private Long dueTime;     // 截止时间
    private Long createTime;
    private Long updateTime;
}
