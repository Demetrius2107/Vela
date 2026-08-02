package com.vela.im.service.bot.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.bot.domain.entity.ImBotEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ImBotMapper extends BaseMapper<ImBotEntity> {
}
