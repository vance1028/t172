package com.market.scale.controller;

import com.market.scale.common.ApiResult;
import com.market.scale.entity.StallViolationAccum;
import com.market.scale.security.RequireRole;
import com.market.scale.service.StallViolationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stall-violations")
@RequireRole
public class StallViolationController {

    private final StallViolationService violationService;

    public StallViolationController(StallViolationService violationService) {
        this.violationService = violationService;
    }

    @GetMapping("/stall/{stallId}")
    public Map<String, Object> getByStall(@PathVariable Long stallId) {
        StallViolationAccum acc = violationService.getByStall(stallId);
        return ApiResult.ok(acc);
    }

    @GetMapping
    public Map<String, Object> page(@RequestParam(required = false) Boolean focusFlag,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "20") int size) {
        return ApiResult.ok(violationService.pageFocused(focusFlag, page, size));
    }
}
