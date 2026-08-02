package com.vela.im.service.message.interfaces.rest;

import com.vela.im.service.message.domain.service.MessageReadService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/message/read")
public class MessageReadController {

    private final MessageReadService messageReadService;

    public MessageReadController(MessageReadService messageReadService) {
        this.messageReadService = messageReadService;
    }

    @GetMapping("/members")
    public Result<List<String>> getReadMembers(@RequestParam Long messageKey) {
        return messageReadService.getReadMembers(messageKey);
    }

    @PostMapping("/mark")
    public Result<Void> markRead(@RequestParam Integer appId,
                                 @RequestParam(required = false) String groupId,
                                 @RequestParam Long messageKey,
                                 @RequestParam String memberId) {
        messageReadService.markRead(appId, groupId, messageKey, memberId);
        return Result.ok();
    }
}
