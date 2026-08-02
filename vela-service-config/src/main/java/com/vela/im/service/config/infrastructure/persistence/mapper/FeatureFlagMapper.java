package com.vela.im.service.config.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.config.domain.entity.FeatureFlagEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureFlagMapper extends BaseMapper<FeatureFlagEntity> {
}
