package com.vela.im.service.knowledge.interfaces.rest;

import com.vela.im.service.knowledge.domain.entity.DocumentEntity;
import com.vela.im.service.knowledge.domain.service.DocumentService;
import com.vela.im.shared.base.Result;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/knowledge")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) { this.service = service; }

    @PostMapping("/create")
    public Result<DocumentEntity> create(@RequestBody DocumentEntity entity) { return service.create(entity); }

    @GetMapping("/list")
    public Result<Map<String, Object>> list(@RequestParam Integer appId, @RequestParam(required = false) String keyword,
                                             @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.list(appId, keyword, page, size);
    }

    @GetMapping("/get")
    public Result<DocumentEntity> get(@RequestParam Long id) { return service.get(id); }

    @PostMapping("/update")
    public Result<Void> update(@RequestBody DocumentEntity entity) { return service.update(entity); }

    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long id) { return service.delete(id); }
}
