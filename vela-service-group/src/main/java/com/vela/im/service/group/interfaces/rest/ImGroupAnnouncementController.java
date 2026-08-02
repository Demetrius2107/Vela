package com.vela.im.service.group.interfaces.rest;

import com.vela.im.service.group.domain.entity.ImGroupAnnouncementEntity;
import com.vela.im.service.group.domain.service.ImGroupAnnouncementService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>Title: ImGroupAnnouncementController</p>
 * <p>Description: 群公告 REST 控制器，提供发布、列表、已读、统计、删除接口。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.2
 * @createTime 2026-07-27
 */
@RestController
@RequestMapping("/v1/group/announcement")
public class ImGroupAnnouncementController {

    private final ImGroupAnnouncementService announcementService;

    public ImGroupAnnouncementController(ImGroupAnnouncementService announcementService) {
        this.announcementService = announcementService;
    }

    @PostMapping("/publish")
    public Result<ImGroupAnnouncementEntity> publish(@RequestParam Integer appId,
                                                     @RequestParam String groupId,
                                                     @RequestParam String title,
                                                     @RequestParam String content,
                                                     @RequestParam String publisherId) {
        return announcementService.publish(appId, groupId, title, content, publisherId);
    }

    @GetMapping("/list")
    public Result<List<ImGroupAnnouncementEntity>> list(@RequestParam Integer appId,
                                                        @RequestParam String groupId) {
        return announcementService.list(appId, groupId);
    }

    @PostMapping("/markRead")
    public Result<Void> markRead(@RequestParam Long announcementId,
                                 @RequestParam String memberId) {
        return announcementService.markRead(announcementId, memberId);
    }

    @GetMapping("/readStats")
    public Result<ImGroupAnnouncementService.ReadStats> readStats(@RequestParam Long announcementId,
                                                                  @RequestParam String groupId) {
        return announcementService.readStats(announcementId, groupId);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long announcementId) {
        return announcementService.delete(announcementId);
    }
}
