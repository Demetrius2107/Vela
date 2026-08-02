package com.vela.im.service.office.interfaces.rest;

import com.vela.im.service.office.domain.entity.ScheduleEntity;
import com.vela.im.service.office.domain.service.ScheduleService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/office/schedule")
public class ScheduleController {

    private final ScheduleService service;

    public ScheduleController(ScheduleService service) { this.service = service; }

    @PostMapping("/create")
    public Result<ScheduleEntity> create(@RequestBody ScheduleEntity entity) { return service.create(entity); }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam String userId, @RequestParam Integer appId,
                                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.list(userId, appId, page, size);
    }

    @PostMapping("/status")
    public Result<Void> updateStatus(@RequestParam Long id, @RequestParam Integer status) { return service.updateStatus(id, status); }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) { return service.delete(id); }
}
