package com.vela.im.service.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("vela_admin_user")
public class AdminUserEntity {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String userId;
    private String password;
    /** super_admin / operator / auditor */
    private String role;
    private Integer status;
    private Long createTime;
}
