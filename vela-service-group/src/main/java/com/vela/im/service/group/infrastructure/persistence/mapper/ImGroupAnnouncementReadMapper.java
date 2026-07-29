package com.vela.im.service.group.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vela.im.service.group.domain.entity.ImGroupAnnouncementReadEntity;
import org.springframework.stereotype.Repository;

/**
 * 群公告已读记录 Mapper
 */
@Repository
public interface ImGroupAnnouncementReadMapper extends BaseMapper<ImGroupAnnouncementReadEntity> {
}
