package com.market.scale.controller;

import com.market.scale.common.ApiResult;
import com.market.scale.dto.InspectionBatchRequest;
import com.market.scale.security.RequireRole;
import com.market.scale.service.InspectionBatchService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/inspection-batches")
@RequireRole
public class InspectionBatchController {

    private final InspectionBatchService batchService;

    public InspectionBatchController(InspectionBatchService batchService) {
        this.batchService = batchService;
    }

    @GetMapping
    public Map<String, Object> page(@RequestParam(required = false) String marketName,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(batchService.page(marketName, status, page, size));
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        return ApiResult.ok(batchService.getById(id));
    }

    @GetMapping("/{id}/summary")
    public Map<String, Object> summary(@PathVariable Long id) {
        return ApiResult.ok(batchService.summary(id));
    }

    @PostMapping
    @RequireRole({"admin", "inspector"})
    public Map<String, Object> create(@Valid @RequestBody InspectionBatchRequest req) {
        return ApiResult.ok(batchService.create(req));
    }

    @PostMapping("/{id}/complete")
    @RequireRole({"admin", "inspector"})
    public Map<String, Object> complete(@PathVariable Long id) {
        return ApiResult.ok(batchService.complete(id));
    }
}
