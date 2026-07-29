package com.vela.im.service.office.interfaces.rest;

import com.vela.im.service.office.domain.entity.TodoEntity;
import com.vela.im.service.office.domain.service.TodoService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/office/todo")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) { this.service = service; }

    @PostMapping("/create")
    public Result<TodoEntity> create(@RequestBody TodoEntity entity) { return service.create(entity); }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam String userId, @RequestParam Integer appId,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        return service.list(userId, appId, status, page, size);
    }

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) {
        return service.updateStatus(id, status);
    }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) { return service.delete(id); }
}
