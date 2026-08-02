package com.vela.im.service.bot.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.bot.domain.entity.ImUserBotEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface ImUserBotMapper extends BaseMapper<ImUserBotEntity> {
}
