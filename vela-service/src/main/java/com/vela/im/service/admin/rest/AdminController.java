package com.vela.im.service.admin.rest;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.vela.im.service.group.domain.entity.ImGroupEntity;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageBodyMapper;
import com.vela.im.service.user.domain.entity.ImUserDataEntity;
import com.vela.im.service.user.infrastructure.persistence.mapper.ImUserDataMapper;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final ImUserDataMapper userDataMapper;
    private final ImGroupMapper groupMapper;
    private final ImMessageBodyMapper messageBodyMapper;

    public AdminController(ImUserDataMapper userDataMapper, ImGroupMapper groupMapper,
                           ImMessageBodyMapper messageBodyMapper) {
        this.userDataMapper = userDataMapper;
        this.groupMapper = groupMapper;
        this.messageBodyMapper = messageBodyMapper;
    }

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> dashboard() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userDataMapper.selectCount(new QueryWrapper<>()));
        stats.put("totalGroups", groupMapper.selectCount(new QueryWrapper<>()));
        stats.put("totalMessages", messageBodyMapper.selectCount(new QueryWrapper<>()));
        return Result.ok(stats);
    }

    @GetMapping("/users")
    public Result<List<ImUserDataEntity>> listUsers(@RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        QueryWrapper<ImUserDataEntity> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("user_id", keyword).or().like("nick_name", keyword);
        }
        query.last("LIMIT " + size + " OFFSET " + (page * size));
        return Result.ok(userDataMapper.selectList(query));
    }

    @GetMapping("/groups")
    public Result<List<ImGroupEntity>> listGroups(@RequestParam(required = false) String keyword,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        QueryWrapper<ImGroupEntity> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            query.like("group_id", keyword).or().like("group_name", keyword);
        }
        query.last("LIMIT " + size + " OFFSET " + (page * size));
        return Result.ok(groupMapper.selectList(query));
    }

    @PostMapping("/group/dissolve")
    public Result<Void> dissolveGroup(@RequestParam String groupId, @RequestParam Integer appId) {
        ImGroupEntity group = groupMapper.selectById(groupId);
        if (group == null) return Result.fail(500, "群组不存在");
        group.setStatus(1);
        groupMapper.updateById(group);
        return Result.ok();
    }
}
