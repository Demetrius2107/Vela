package com.vela.im.service.message.interfaces.feign;

import com.vela.im.service.common.entity.GetRoleInGroupResp;
import com.vela.im.service.common.entity.GroupMemberDto;
import com.vela.im.service.common.entity.ImGroupEntity;
import com.vela.im.shared.base.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 群组服务 Feign 客户端（消息服务调用群组服务）
 */
@FeignClient(name = "vela-group", url = "http://localhost:8012")
public interface GroupServiceFeignClient {

    @GetMapping("/v1/group/getGroupInfo")
    Result<ImGroupEntity> getGroup(@RequestParam("groupId") String groupId,
                                    @RequestParam("appId") Integer appId);

    @GetMapping("/v1/group/member/getRoleInGroupOne")
    Result<GetRoleInGroupResp> getRoleInGroupOne(@RequestParam("groupId") String groupId,
                                                  @RequestParam("userId") String userId,
                                                  @RequestParam("appId") Integer appId);

    @GetMapping("/v1/group/member/getGroupMemberId")
    Result<List<String>> getGroupMemberId(@RequestParam("groupId") String groupId,
                                           @RequestParam("appId") Integer appId);

    @GetMapping("/v1/group/member/getGroupManager")
    Result<List<GroupMemberDto>> getGroupManager(@RequestParam("groupId") String groupId,
                                                  @RequestParam("appId") Integer appId);
}
