package com.vela.im.service.office.domain.service;

import com.vela.im.service.office.domain.entity.ScheduleEntity;
import com.vela.im.service.office.infrastructure.persistence.mapper.ScheduleMapper;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ScheduleService - 日程服务")
class ScheduleServiceTest {

    @Mock
    private ScheduleMapper scheduleMapper;

    private ScheduleService service;

    @BeforeEach
    void setUp() {
        service = new ScheduleService(scheduleMapper);
    }

    @Nested
    @DisplayName("create - 创建日程")
    class CreateTest {
        @Test
        @DisplayName("正常创建应返回实体并写入DB")
        void normalCreate() {
            ScheduleEntity entity = new ScheduleEntity();
            entity.setUserId("user001");
            entity.setTitle("测试日程");
            entity.setStartTime(1000L);

            Result<ScheduleEntity> result = service.create(entity);

            assertTrue(result.isOk());
            assertNotNull(result.getData().getCreateTime());
            verify(scheduleMapper, times(1)).insert(any(ScheduleEntity.class));
        }
    }

    @Nested
    @DisplayName("list - 获取日程列表")
    class ListTest {
        @Test
        @DisplayName("返回分页结果")
        void returnsPagedResult() {
            Result<Map<String, Object>> result = service.list("user001", 100, 0, 20);

            assertTrue(result.isOk());
            assertNotNull(result.getData());
        }
    }

    @Nested
    @DisplayName("updateStatus - 更新日程状态")
    class UpdateStatusTest {
        @Test
        @DisplayName("不存在的日程应返回错误")
        void nonExistentReturnsError() {
            when(scheduleMapper.selectById(999L)).thenReturn(null);

            Result<Void> result = service.updateStatus(999L, 1);

            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("存在的日程应更新状态")
        void existingUpdatesStatus() {
            ScheduleEntity existing = new ScheduleEntity();
            existing.setId(1L);
            when(scheduleMapper.selectById(1L)).thenReturn(existing);

            Result<Void> result = service.updateStatus(1L, 1);

            assertTrue(result.isOk());
            verify(scheduleMapper, times(1)).updateById(argThat(e -> e.getStatus() == 1));
        }
    }
}
