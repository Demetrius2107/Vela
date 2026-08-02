package com.vela.im.service.group.interfaces.feign;

import com.vela.im.shared.base.Result;
import com.vela.im.shared.types.message.MessageReadedContent;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 消息服务 Feign 客户端
 */
@FeignClient(name = "vela-message", url = "http://localhost:8014")
public interface MessageServiceFeignClient {

    @PostMapping("/v1/message/read/groupMark")
    Result<Void> groupReadMark(@RequestBody MessageReadedContent content);
}
