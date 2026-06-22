package com.market.scale.controller;

import com.market.scale.common.ApiResult;
import com.market.scale.dto.ToleranceRuleRequest;
import com.market.scale.entity.ToleranceRule;
import com.market.scale.security.RequireRole;
import com.market.scale.service.ToleranceRuleService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tolerance-rules")
@RequireRole
public class ToleranceRuleController {

    private final ToleranceRuleService ruleService;

    public ToleranceRuleController(ToleranceRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) String category,
                                    @RequestParam(required = false) String weighingType,
                                    @RequestParam(required = false) Boolean enabled) {
        List<ToleranceRule> rules = ruleService.list(category, weighingType, enabled);
        return ApiResult.ok(rules);
    }

    @GetMapping("/match")
    public Map<String, Object> match(@RequestParam(required = false) String category,
                                     @RequestParam(required = false) String weighingType,
                                     @RequestParam int weight) {
        ToleranceRule rule = ruleService.match(category, weighingType, weight);
        return ApiResult.ok(rule);
    }

    @GetMapping("/{id}")
    public Map<String, Object> getById(@PathVariable Long id) {
        return ApiResult.ok(ruleService.getById(id));
    }

    @PostMapping
    @RequireRole({"admin"})
    public Map<String, Object> create(@Valid @RequestBody ToleranceRuleRequest req) {
        return ApiResult.ok(ruleService.create(req));
    }

    @PutMapping("/{id}")
    @RequireRole({"admin"})
    public Map<String, Object> update(@PathVariable Long id,
                                      @Valid @RequestBody ToleranceRuleRequest req) {
        return ApiResult.ok(ruleService.update(id, req));
    }

    @PatchMapping("/{id}/enabled")
    @RequireRole({"admin"})
    public Map<String, Object> setEnabled(@PathVariable Long id,
                                          @RequestParam boolean enabled) {
        ruleService.setEnabled(id, enabled);
        return ApiResult.ok();
    }

    @DeleteMapping("/{id}")
    @RequireRole({"admin"})
    public Map<String, Object> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return ApiResult.ok();
    }
}
