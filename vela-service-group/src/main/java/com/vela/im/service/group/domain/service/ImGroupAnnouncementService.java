package com.vela.im.service.group.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.common.utils.MessageProducer;
import com.vela.im.service.group.domain.entity.ImGroupAnnouncementEntity;
import com.vela.im.service.group.domain.entity.ImGroupAnnouncementReadEntity;
import com.vela.im.service.group.domain.entity.ImGroupMemberEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupAnnouncementMapper;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupAnnouncementReadMapper;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupMemberMapper;
import com.vela.im.shared.base.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>Title: ImGroupAnnouncementService</p>
 * <p>Description: 群公告领域服务，提供公告发布、列表查询、已读标记、已读统计等功能。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@Service
public class ImGroupAnnouncementService {

    private static final Logger log = LoggerFactory.getLogger(ImGroupAnnouncementService.class);

    private final ImGroupAnnouncementMapper announcementMapper;
    private final ImGroupAnnouncementReadMapper readMapper;
    private final ImGroupMemberMapper groupMemberMapper;
    private final ImGroupService imGroupService;
    private final MessageProducer messageProducer;

    public ImGroupAnnouncementService(ImGroupAnnouncementMapper announcementMapper,
                                      ImGroupAnnouncementReadMapper readMapper,
                                      ImGroupMemberMapper groupMemberMapper,
                                      ImGroupService imGroupService,
                                      MessageProducer messageProducer) {
        this.announcementMapper = announcementMapper;
        this.readMapper = readMapper;
        this.groupMemberMapper = groupMemberMapper;
        this.imGroupService = imGroupService;
        this.messageProducer = messageProducer;
    }

    @Transactional
    public Result<ImGroupAnnouncementEntity> publish(Integer appId, String groupId, String title,
                                                     String content, String publisherId) {
        Result<com.vela.im.service.group.domain.entity.ImGroupEntity> groupResult = imGroupService.getGroup(groupId, appId);
        if (!groupResult.isOk() || groupResult.getData() == null) {
            return Result.fail(500, "群组不存在");
        }

        ImGroupAnnouncementEntity entity = new ImGroupAnnouncementEntity();
        entity.setAppId(appId);
        entity.setGroupId(groupId);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setPublisherId(publisherId);
        entity.setPublishTime(System.currentTimeMillis());
        entity.setUpdateTime(entity.getPublishTime());
        entity.setDelFlag(0);
        announcementMapper.insert(entity);

        com.vela.im.service.group.application.dto.req.UpdateGroupRequest updateReq =
                new com.vela.im.service.group.application.dto.req.UpdateGroupRequest();
        updateReq.setGroupId(groupId);
        updateReq.setNotification(title);
        imGroupService.updateBaseGroupInfo(updateReq);

        log.info("Announcement published: groupId={}, title={}, publisherId={}", groupId, title, publisherId);
        return Result.ok(entity);
    }

    public Result<List<ImGroupAnnouncementEntity>> list(Integer appId, String groupId) {
        QueryWrapper<ImGroupAnnouncementEntity> query = new QueryWrapper<>();
        query.eq("app_id", appId).eq("group_id", groupId).eq("del_flag", 0).orderByDesc("publish_time");
        return Result.ok(announcementMapper.selectList(query));
    }

    public Result<Void> markRead(Long announcementId, String memberId) {
        QueryWrapper<ImGroupAnnouncementReadEntity> check = new QueryWrapper<>();
        check.eq("announcement_id", announcementId).eq("member_id", memberId);
        if (readMapper.selectCount(check) > 0) return Result.ok();

        ImGroupAnnouncementReadEntity read = new ImGroupAnnouncementReadEntity();
        read.setAnnouncementId(announcementId);
        read.setMemberId(memberId);
        read.setReadTime(System.currentTimeMillis());
        readMapper.insert(read);
        return Result.ok();
    }

    public Result<ReadStats> readStats(Long announcementId, String groupId) {
        int total = groupMemberMapper.selectCount(new QueryWrapper<ImGroupMemberEntity>().eq("group_id", groupId));
        int read = readMapper.selectCount(new QueryWrapper<ImGroupAnnouncementReadEntity>().eq("announcement_id", announcementId));
        return Result.ok(new ReadStats(total, read));
    }

    @Transactional
    public Result<Void> delete(Long announcementId) {
        ImGroupAnnouncementEntity entity = announcementMapper.selectById(announcementId);
        if (entity == null) return Result.fail(500, "公告不存在");
        entity.setDelFlag(1);
        announcementMapper.updateById(entity);
        return Result.ok();
    }

    public static class ReadStats {
        private final int totalMemberCount;
        private final int readMemberCount;

        public ReadStats(int totalMemberCount, int readMemberCount) {
            this.totalMemberCount = totalMemberCount;
            this.readMemberCount = readMemberCount;
        }

        public int getTotalMemberCount() { return totalMemberCount; }
        public int getReadMemberCount() { return readMemberCount; }
        public int getUnreadCount() { return totalMemberCount - readMemberCount; }
    }
}
