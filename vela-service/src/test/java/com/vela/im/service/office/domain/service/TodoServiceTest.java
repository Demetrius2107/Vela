package com.vela.im.service.office.domain.service;

import com.vela.im.service.office.domain.entity.TodoEntity;
import com.vela.im.service.office.infrastructure.persistence.mapper.TodoMapper;
import com.vela.im.shared.base.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TodoService - 待办服务")
class TodoServiceTest {

    @Mock
    private TodoMapper todoMapper;

    private TodoService service;

    @BeforeEach
    void setUp() {
        service = new TodoService(todoMapper);
    }

    @Test
    @DisplayName("创建待办应写入DB")
    void createTodo() {
        TodoEntity entity = new TodoEntity();
        entity.setTitle("测试待办");
        entity.setPriority(1);

        Result<TodoEntity> result = service.create(entity);

        assertTrue(result.isOk());
        verify(todoMapper, times(1)).insert(any(TodoEntity.class));
    }

    @Test
    @DisplayName("列出待办应返回分页结果")
    void listTodos() {
        Result<Map<String, Object>> result = service.list("user001", 100, null, 0, 20);

        assertTrue(result.isOk());
        assertNotNull(result.getData());
    }

    @Test
    @DisplayName("更新不存在的待办应返回错误")
    void updateNonExistent() {
        when(todoMapper.selectById(999L)).thenReturn(null);

        Result<Void> result = service.updateStatus(999L, 2);

        assertFalse(result.isOk());
    }

    @Test
    @DisplayName("删除待办应调用mapper")
    void deleteTodo() {
        service.delete(1L);
        verify(todoMapper, times(1)).deleteById(1L);
    }
}
