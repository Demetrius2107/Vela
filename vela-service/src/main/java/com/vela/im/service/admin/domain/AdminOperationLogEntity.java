package com.vela.im.service.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_admin_operation_log")
public class AdminOperationLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String operatorId;   // 操作者
    private String action;       // 操作类型: user_forbidden/user_unforbidden/group_dissolve/...
    private String targetType;   // 操作对象类型: user/group/message/system
    private String targetId;     // 操作对象ID
    private String detail;       // 操作详情
    private Long operateTime;    // 操作时间
    private Integer appId;
}
