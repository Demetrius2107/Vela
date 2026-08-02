package com.vela.im.service.group.interfaces.rest;

import com.vela.im.service.group.domain.entity.ImGroupTagEntity;
import com.vela.im.service.group.domain.service.GroupTagService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/group/tag")
public class GroupTagController {

    private final GroupTagService tagService;

    public GroupTagController(GroupTagService tagService) {
        this.tagService = tagService;
    }

    @PostMapping("/create")
    public Result<ImGroupTagEntity> create(@RequestParam Integer appId, @RequestParam String userId,
                                           @RequestParam String name, @RequestParam(required = false) String color) {
        return tagService.createTag(appId, userId, name, color);
    }

    @GetMapping("/list")
    public Result<List<ImGroupTagEntity>> list(@RequestParam String userId, @RequestParam Integer appId) {
        return tagService.listTags(userId, appId);
    }

    @PostMapping("/tagGroup")
    public Result<Void> tagGroup(@RequestParam Long tagId, @RequestParam String groupId) {
        return tagService.tagGroup(tagId, groupId);
    }

    @PostMapping("/untagGroup")
    public Result<Void> untagGroup(@RequestParam Long tagId, @RequestParam String groupId) {
        return tagService.untagGroup(tagId, groupId);
    }

    @GetMapping("/groups")
    public Result<List<String>> getGroupTags(@RequestParam Long tagId) {
        return tagService.getGroupTags(tagId);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long tagId) {
        return tagService.deleteTag(tagId);
    }
}
