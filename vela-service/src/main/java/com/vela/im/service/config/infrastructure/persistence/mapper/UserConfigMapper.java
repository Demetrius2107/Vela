package com.vela.im.service.config.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.config.domain.entity.UserConfigEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface UserConfigMapper extends BaseMapper<UserConfigEntity> {
}
