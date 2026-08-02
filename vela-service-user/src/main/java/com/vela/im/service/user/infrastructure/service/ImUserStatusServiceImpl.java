package com.vela.im.service.user.infrastructure.service;

import com.vela.im.service.user.application.dto.UserStatusChangeNotifyContent;
import com.vela.im.service.user.application.dto.req.PullFriendOnlineStatusReq;
import com.vela.im.service.user.application.dto.req.PullUserOnlineStatusReq;
import com.vela.im.service.user.application.dto.req.SetUserCustomerStatusReq;
import com.vela.im.service.user.application.dto.req.SubscribeUserOnlineStatusReq;
import com.vela.im.service.user.application.dto.resp.UserOnlineStatusResp;
import com.vela.im.service.user.domain.service.ImUserStatusService;
import com.vela.im.shared.constants.ImConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: ImUserStatusServiceImpl</p>
 * <p>Description: 用户在线状态服务实现，基于 Redis 存储用户上下线状态。</p>
 * <p>项目名称: Vela</p>
 *
 * @author wanqiu
 * @since 1.0
 * @createTime 2026-07-29
 */
@Service
public class ImUserStatusServiceImpl implements ImUserStatusService {

    private static final Logger logger = LoggerFactory.getLogger(ImUserStatusServiceImpl.class);

    private final StringRedisTemplate stringRedisTemplate;

    public ImUserStatusServiceImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void processUserOnlineStatusNotify(UserStatusChangeNotifyContent content) {
        if (content == null || content.getUserId() == null) return;

        String redisKey = content.getAppId() + ImConstants.Redis.USER_SESSION_PREFIX + content.getUserId();

        if (content.getStatus() == 1) {
            // 上线：写入 Redis，设置过期时间（30 分钟无心跳自动离线）
            stringRedisTemplate.opsForValue().set(redisKey, "online", 30, TimeUnit.MINUTES);
            logger.info("User online: userId={}, appId={}", content.getUserId(), content.getAppId());
        } else {
            // 下线：删除 Redis 中的在线标记
            stringRedisTemplate.delete(redisKey);
            logger.info("User offline: userId={}, appId={}", content.getUserId(), content.getAppId());
        }
    }

    @Override
    public void subscribeUserOnlineStatus(SubscribeUserOnlineStatusReq req) {
        // TODO: 用户订阅好友在线状态通知（后续实现）
    }

    @Override
    public void setUserCustomerStatus(SetUserCustomerStatusReq req) {
        // TODO: 用户自定义状态（后续实现）
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryFriendOnlineStatus(PullFriendOnlineStatusReq req) {
        // 接口定义待完善，暂返回空
        return new HashMap<>();
    }

    @Override
    public Map<String, UserOnlineStatusResp> queryUserOnlineStatus(PullUserOnlineStatusReq req) {
        Map<String, UserOnlineStatusResp> result = new HashMap<>();
        if (req.getUserList() == null) return result;

        for (String userId : req.getUserList()) {
            String redisKey = req.getAppId() + ImConstants.Redis.USER_SESSION_PREFIX + userId;
            String statusStr = stringRedisTemplate.opsForValue().get(redisKey);

            UserOnlineStatusResp resp = new UserOnlineStatusResp();
            result.put(userId, resp);
        }
        return result;
    }
}
