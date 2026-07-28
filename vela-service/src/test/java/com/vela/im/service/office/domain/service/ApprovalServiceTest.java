package com.vela.im.service.office.domain.service;

import com.vela.im.service.office.domain.entity.ApprovalEntity;
import com.vela.im.service.office.infrastructure.persistence.mapper.ApprovalMapper;
import com.vela.im.shared.base.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApprovalService - 审批服务")
class ApprovalServiceTest {

    @Mock
    private ApprovalMapper approvalMapper;

    private ApprovalService service;

    @BeforeEach
    void setUp() {
        service = new ApprovalService(approvalMapper);
    }

    @Nested
    @DisplayName("submit - 提交审批")
    class SubmitTest {
        @Test
        @DisplayName("正常提交应设置待审批状态并写入DB")
        void normalSubmit() {
            ApprovalEntity entity = new ApprovalEntity();
            entity.setTitle("请假");
            entity.setApplicantId("user001");

            Result<ApprovalEntity> result = service.submit(entity);

            assertTrue(result.isOk());
            assertEquals(0, result.getData().getStatus()); // 待审批
            verify(approvalMapper, times(1)).insert(any(ApprovalEntity.class));
        }
    }

    @Nested
    @DisplayName("approve - 审批处理")
    class ApproveTest {
        @Test
        @DisplayName("不存在的审批应返回错误")
        void nonExistent() {
            when(approvalMapper.selectById(999L)).thenReturn(null);

            Result<Void> result = service.approve(999L, "admin", "同意", true);

            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("已处理的审批不应重复处理")
        void alreadyProcessed() {
            ApprovalEntity existing = new ApprovalEntity();
            existing.setId(1L);
            existing.setStatus(1);
            when(approvalMapper.selectById(1L)).thenReturn(existing);

            Result<Void> result = service.approve(1L, "admin", null, true);

            assertFalse(result.isOk());
        }

        @Test
        @DisplayName("通过审批应更新状态为已通过")
        void approvePassed() {
            ApprovalEntity existing = new ApprovalEntity();
            existing.setId(1L);
            existing.setStatus(0);
            when(approvalMapper.selectById(1L)).thenReturn(existing);

            Result<Void> result = service.approve(1L, "admin", "同意", true);

            assertTrue(result.isOk());
            verify(approvalMapper, times(1)).updateById(argThat(e ->
                    e.getStatus() == 1 && "同意".equals(e.getComment())));
        }

        @Test
        @DisplayName("拒绝审批应更新状态为已拒绝")
        void approveRejected() {
            ApprovalEntity existing = new ApprovalEntity();
            existing.setId(1L);
            existing.setStatus(0);
            when(approvalMapper.selectById(1L)).thenReturn(existing);

            Result<Void> result = service.approve(1L, "admin", "不同意", false);

            assertTrue(result.isOk());
            verify(approvalMapper, times(1)).updateById(argThat(e -> e.getStatus() == 2));
        }
    }
}
