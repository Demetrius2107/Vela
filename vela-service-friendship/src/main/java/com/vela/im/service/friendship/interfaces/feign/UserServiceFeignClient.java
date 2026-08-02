package com.vela.im.service.friendship.interfaces.feign;

import com.vela.im.service.common.entity.ImUserDataEntity;
import com.vela.im.shared.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务 Feign 客户端
 */
@FeignClient(name = "vela-user", url = "http://localhost:8010")
public interface UserServiceFeignClient {

    @GetMapping("/v1/user/getSingleUserInfo")
    Result<ImUserDataEntity> getSingleUserInfo(@RequestParam("userId") String userId,
                                                @RequestParam("appId") Integer appId);
}
