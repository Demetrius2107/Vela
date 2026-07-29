package com.vela.im.service.message.domain.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vela.im.service.message.domain.entity.ImMessageFavoriteEntity;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageFavoriteMapper;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.types.enums.BusinessErrorCode;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Title: MessageFavoriteService</p>
 * <p>Description: 消息收藏服务，支持收藏/取消收藏/列表/分页查询。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Service
public class MessageFavoriteService {

    private final ImMessageFavoriteMapper favoriteMapper;

    public MessageFavoriteService(ImMessageFavoriteMapper favoriteMapper) {
        this.favoriteMapper = favoriteMapper;
    }

    /** 收藏消息 */
    public Result<Void> add(Integer appId, String userId, String conversationId,
                            String fromId, String content, Long messageTime) {
        // 检查是否已收藏
        Long count = (long) favoriteMapper.selectCount(
                new QueryWrapper<ImMessageFavoriteEntity>()
                        .eq("user_id", userId)
                        .eq("app_id", appId)
                        .eq("conversation_id", conversationId)
                        .eq("message_time", messageTime));
        if (count > 0) {
            return Result.fail(BusinessErrorCode.FAVORITE_ALREADY_EXISTS);
        }

        ImMessageFavoriteEntity entity = new ImMessageFavoriteEntity();
        entity.setAppId(appId);
        entity.setUserId(userId);
        entity.setConversationId(conversationId);
        entity.setFromId(fromId);
        entity.setContent(content);
        entity.setMessageTime(messageTime);
        entity.setCreateTime(System.currentTimeMillis());
        favoriteMapper.insert(entity);
        return Result.ok();
    }

    /** 取消收藏 */
    public Result<Void> remove(Integer appId, String userId, String conversationId, Long messageTime) {
        int affected = favoriteMapper.delete(
                new QueryWrapper<ImMessageFavoriteEntity>()
                        .eq("user_id", userId)
                        .eq("app_id", appId)
                        .eq("conversation_id", conversationId)
                        .eq("message_time", messageTime));
        if (affected == 0) return Result.fail(BusinessErrorCode.FAVORITE_NOT_FOUND);
        return Result.ok();
    }

    /** 用户收藏列表（分页） */
    public Result<Map<String, Object>> list(Integer appId, String userId, int page, int size) {
        Page<ImMessageFavoriteEntity> p = new Page<>(page, size);
        QueryWrapper<ImMessageFavoriteEntity> qw = new QueryWrapper<>();
        qw.eq("user_id", userId).eq("app_id", appId);
        qw.orderByDesc("create_time");
        IPage<ImMessageFavoriteEntity> result = favoriteMapper.selectPage(p, qw);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        return Result.ok(map);
    }

    /** 检查消息是否已收藏 */
    public Result<Boolean> isFavorited(Integer appId, String userId, String conversationId, Long messageTime) {
        Long count = (long) favoriteMapper.selectCount(
                new QueryWrapper<ImMessageFavoriteEntity>()
                        .eq("user_id", userId)
                        .eq("app_id", appId)
                        .eq("conversation_id", conversationId)
                        .eq("message_time", messageTime));
        return Result.ok(count > 0);
    }

    /** 批量检查（给前端用） */
    public Result<List<Long>> checkBatch(Integer appId, String userId, List<Long> messageTimes) {
        List<ImMessageFavoriteEntity> list = favoriteMapper.selectList(
                new QueryWrapper<ImMessageFavoriteEntity>()
                        .eq("user_id", userId)
                        .eq("app_id", appId)
                        .in("message_time", messageTimes));
        List<Long> favorited = list.stream()
                .map(ImMessageFavoriteEntity::getMessageTime)
                .collect(java.util.stream.Collectors.toList());
        return Result.ok(favorited);
    }
}
