package com.vela.im.service.user.interfaces.feign;

import com.vela.im.shared.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 群组服务 Feign 客户端
 */
@FeignClient(name = "vela-group", url = "http://localhost:8012")
public interface GroupServiceFeignClient {

    @GetMapping("/v1/group/getGroupInfo")
    Result<?> getGroupInfo(@RequestParam("groupId") String groupId,
                           @RequestParam("appId") Integer appId);

    @GetMapping("/v1/group/member/getUserGroupMaxSeq")
    Result<Long> getUserGroupMaxSeq(@RequestParam("userId") String userId,
                                     @RequestParam("appId") Integer appId);
}
