package com.vela.im.service.admin.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.admin.domain.SystemConfigEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemConfigMapper extends BaseMapper<SystemConfigEntity> {
}
