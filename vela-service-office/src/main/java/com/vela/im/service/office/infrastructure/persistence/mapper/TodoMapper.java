package com.vela.im.service.office.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.office.domain.entity.TodoEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoMapper extends BaseMapper<TodoEntity> {}
