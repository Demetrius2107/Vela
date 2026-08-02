package com.vela.im.service.message.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.message.domain.entity.ImFileEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ImFileMapper extends BaseMapper<ImFileEntity> {
}
