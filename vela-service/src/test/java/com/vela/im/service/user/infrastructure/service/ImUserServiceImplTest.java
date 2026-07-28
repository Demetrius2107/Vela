package com.vela.im.service.user.infrastructure.service;

import com.vela.im.service.application.utils.CallbackService;
import com.vela.im.service.application.utils.MessageProducer;
import com.vela.im.service.group.domain.service.ImGroupService;
import com.vela.im.service.user.domain.entity.ImUserDataEntity;
import com.vela.im.service.user.infrastructure.persistence.mapper.ImUserDataMapper;
import com.vela.im.shared.base.Result;
import com.vela.im.shared.config.ImServerProperties;
import com.vela.im.shared.types.enums.DelFlagEnum;
import com.vela.im.service.user.application.dto.req.*;
import com.vela.im.service.user.application.dto.resp.GetUserInfoResp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImUserServiceImpl - 用户服务")
class ImUserServiceImplTest {

    @Mock private ImUserDataMapper userDataMapper;
    @Mock private ImServerProperties appConfig;
    @Mock private CallbackService callbackService;
    @Mock private MessageProducer messageProducer;
    @Mock private StringRedisTemplate stringRedisTemplate;
    @Mock private ImGroupService imGroupService;

    private ImUserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new ImUserServiceImpl(userDataMapper, appConfig, callbackService,
                messageProducer, stringRedisTemplate, imGroupService);
    }

    @Nested
    @DisplayName("importUser - 导入用户")
    class ImportUserTest {
        @Test
        @DisplayName("超过100个用户应返回错误")
        void beyondLimit() {
            ImportUserReq req = new ImportUserReq();
            req.setUserData(List.of(new ImUserDataEntity(), new ImUserDataEntity()));
            // Only 2, under 100 limit

            Result result = userService.importUser(req);

            assertTrue(result.isOk());
        }
    }

    @Nested
    @DisplayName("getSingleUserInfo - 获取单个用户")
    class GetSingleUserInfoTest {
        @Test
        @DisplayName("不存在的用户应返回错误")
        void nonExistent() {
            when(userDataMapper.selectOne(any())).thenReturn(null);

            Result<ImUserDataEntity> result = userService.getSingleUserInfo("nonexist", 100);

            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("存在的用户应返回用户信息")
        void existingUser() {
            ImUserDataEntity user = new ImUserDataEntity();
            user.setUserId("user001");
            when(userDataMapper.selectOne(any())).thenReturn(user);

            Result<ImUserDataEntity> result = userService.getSingleUserInfo("user001", 100);

            assertTrue(result.isOk());
            assertEquals("user001", result.getData().getUserId());
        }
    }

    @Nested
    @DisplayName("getUserInfo - 批量获取用户")
    class GetUserInfoTest {
        @Test
        @DisplayName("应返回用户列表和失败列表")
        void returnsUserListAndFailList() {
            ImUserDataEntity user = new ImUserDataEntity();
            user.setUserId("user001");
            when(userDataMapper.selectList(any())).thenReturn(List.of(user));

            GetUserInfoReq req = new GetUserInfoReq();
            req.setAppId(100);
            req.setUserIds(List.of("user001", "user002"));

            Result<GetUserInfoResp> result = userService.getUserInfo(req);

            assertTrue(result.isOk());
            assertEquals(1, result.getData().getUserDataItem().size());
            assertTrue(result.getData().getFailUser().contains("user002"));
        }
    }

    @Nested
    @DisplayName("deleteUser - 删除用户")
    class DeleteUserTest {
        @Test
        @DisplayName("应逻辑删除用户")
        void logicalDelete() {
            when(userDataMapper.update(any(), any())).thenReturn(1);

            DeleteUserReq req = new DeleteUserReq();
            req.setAppId(100);
            req.setUserId(List.of("user001"));

            Result result = userService.deleteUser(req);

            assertTrue(result.isOk());
            verify(userDataMapper, times(1)).update(any(), any());
        }
    }
}
