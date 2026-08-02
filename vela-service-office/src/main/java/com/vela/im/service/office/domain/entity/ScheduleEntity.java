package com.vela.im.service.office.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_schedule")
public class ScheduleEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer appId;
    private String userId;
    private String title;
    private String description;
    private String location;

    private Long startTime;  // 毫秒时间戳
    private Long endTime;

    /** 0-待办 1-已完成 2-已取消 */
    private Integer status;

    /** 提醒：0-不提醒 1-提前5分钟 2-提前15分钟 3-提前1小时 */
    private Integer remindType;

    private Long createTime;
    private Long updateTime;
}
