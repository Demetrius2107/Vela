package com.vela.im.service.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.admin.domain.AdminLoginLogEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminLoginLogMapper extends BaseMapper<AdminLoginLogEntity> {
}
