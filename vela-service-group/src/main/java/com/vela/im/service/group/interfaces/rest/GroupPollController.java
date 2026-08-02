package com.vela.im.service.group.interfaces.rest;

import com.vela.im.service.group.domain.entity.ImGroupPollEntity;
import com.vela.im.service.group.domain.service.GroupPollService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/group/poll")
public class GroupPollController {

    private final GroupPollService pollService;

    public GroupPollController(GroupPollService pollService) {
        this.pollService = pollService;
    }

    @PostMapping("/create")
    public Result<ImGroupPollEntity> create(@RequestParam Integer appId, @RequestParam String groupId,
                                            @RequestParam String title, @RequestParam String creatorId,
                                            @RequestParam(required = false) Integer multipleChoice,
                                            @RequestParam(required = false) Long endTime,
                                            @RequestParam List<String> options) {
        return pollService.createPoll(appId, groupId, title, creatorId, multipleChoice, endTime, options);
    }

    @GetMapping("/list")
    public Result<List<ImGroupPollEntity>> list(@RequestParam String groupId, @RequestParam Integer appId) {
        return pollService.listPolls(groupId, appId);
    }

    @PostMapping("/vote")
    public Result<Void> vote(@RequestParam Long pollId, @RequestParam Long optionId,
                             @RequestParam String voterId) {
        return pollService.vote(pollId, optionId, voterId);
    }

    @GetMapping("/result")
    public Result<GroupPollService.PollResult> result(@RequestParam Long pollId) {
        return pollService.getResult(pollId);
    }

    @PostMapping("/close")
    public Result<Void> close(@RequestParam Long pollId) {
        return pollService.closePoll(pollId);
    }
}
