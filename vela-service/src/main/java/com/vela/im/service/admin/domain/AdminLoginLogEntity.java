package com.vela.im.service.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_admin_login_log")
public class AdminLoginLogEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;
    private String ip;
    private String device;
    private String location;
    private Integer status; // 0-失败, 1-成功
    private Long loginTime;

    /** 关联的 appId */
    private Integer appId;
}
