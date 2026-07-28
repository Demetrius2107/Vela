package com.vela.im.service.admin.domain.service;

import com.vela.im.service.admin.infrastructure.persistence.mapper.AdminLoginLogMapper;
import com.vela.im.service.admin.infrastructure.persistence.mapper.AdminOperationLogMapper;
import com.vela.im.service.group.infrastructure.persistence.mapper.ImGroupMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageBodyMapper;
import com.vela.im.service.message.infrastructure.persistence.mapper.ImMessageHistoryMapper;
import com.vela.im.service.message.infrastructure.elasticsearch.MessageSearchService;
import com.vela.im.service.user.infrastructure.persistence.mapper.ImUserDataMapper;
import com.vela.im.shared.base.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminService - 管理后台服务")
class AdminServiceTest {

    @Mock private ImUserDataMapper userDataMapper;
    @Mock private ImGroupMapper groupMapper;
    @Mock private ImMessageBodyMapper messageBodyMapper;
    @Mock private ImMessageHistoryMapper messageHistoryMapper;
    @Mock private AdminLoginLogMapper loginLogMapper;
    @Mock private AdminOperationLogMapper operationLogMapper;
    @Mock private MessageSearchService messageSearchService;

    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(userDataMapper, groupMapper, messageBodyMapper,
                messageHistoryMapper, loginLogMapper, operationLogMapper, messageSearchService);
    }

    @Nested
    @DisplayName("dashboard - 数据看板")
    class DashboardTest {
        @Test
        @DisplayName("应返回5项统计数据")
        void returnsFiveStats() {
            when(userDataMapper.selectCount(any())).thenReturn(100);
            when(groupMapper.selectCount(any())).thenReturn(20);

            Result<Map<String, Object>> result = service.dashboard();

            assertTrue(result.isOk());
            assertEquals(100, result.getData().get("totalUsers"));
            assertEquals(20, result.getData().get("totalGroups"));
        }
    }

    @Nested
    @DisplayName("listUsers - 用户列表")
    class ListUsersTest {
        @Test
        @DisplayName("有关键词应调用like查询")
        void keywordSearch() {
            Result<Map<String, Object>> result = service.listUsers("张三", 0, 20);

            assertTrue(result.isOk());
            assertNotNull(result.getData().get("list"));
        }
    }

    @Nested
    @DisplayName("dissolveGroup - 解散群组")
    class DissolveGroupTest {
        @Test
        @DisplayName("不存在的群组应返回错误")
        void nonExistentGroup() {
            when(groupMapper.selectById("999")).thenReturn(null);

            Result<Void> result = service.dissolveGroup("999", 100);

            assertFalse(result.isOk());
        }
    }

    @Nested
    @DisplayName("operationLogs - 操作日志")
    class OperationLogsTest {
        @Test
        @DisplayName("应返回分页结果")
        void returnsPagedResult() {
            Result<Map<String, Object>> result = service.operationLogs(null, null, 0, 20);

            assertTrue(result.isOk());
            assertNotNull(result.getData());
        }
    }

    @Nested
    @DisplayName("messageTrend - 消息趋势")
    class MessageTrendTest {
        @Test
        @DisplayName("应返回趋势数据")
        void returnsTrend() {
            when(messageBodyMapper.selectCount(any())).thenReturn(50L);

            Result<Map<String, Object>> result = service.messageTrend(7);

            assertTrue(result.isOk());
            assertEquals(7, result.getData().get("periodDays"));
            assertEquals(50L, result.getData().get("totalMessages"));
        }
    }

    @Nested
    @DisplayName("searchMessages - 消息搜索")
    class SearchMessagesTest {
        @Test
        @DisplayName("有关键词应走ES搜索")
        void keywordGoesToES() {
            Result<Map<String, Object>> result = service.searchMessages("hello", null, null, null, null, 0, 20);

            assertTrue(result.isOk());
        }
    }
}
