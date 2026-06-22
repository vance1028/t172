package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.dto.ToleranceRuleRequest;
import com.market.scale.entity.ToleranceRule;
import com.market.scale.mapper.ToleranceRuleMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ToleranceRuleService {

    private final ToleranceRuleMapper ruleMapper;

    public ToleranceRuleService(ToleranceRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
    }

    public ToleranceRule getById(Long id) {
        ToleranceRule r = ruleMapper.findById(id);
        if (r == null) throw ApiException.notFound("允差规则不存在");
        return r;
    }

    public List<ToleranceRule> list(String category, String weighingType, Boolean enabled) {
        return ruleMapper.search(category, weighingType, enabled);
    }

    public ToleranceRule match(String category, String weighingType, int weight) {
        String cat = (category == null || category.isBlank())
                ? ToleranceJudgmentService.DEFAULT_CATEGORY : category;
        String wt = (weighingType == null || weighingType.isBlank())
                ? ToleranceJudgmentService.DEFAULT_WEIGHING_TYPE : weighingType;
        ToleranceRule rule = ruleMapper.matchRule(cat, wt, weight);
        if (rule == null) {
            rule = ruleMapper.matchRule(ToleranceJudgmentService.DEFAULT_CATEGORY,
                    ToleranceJudgmentService.DEFAULT_WEIGHING_TYPE, weight);
        }
        return rule;
    }

    public ToleranceRule create(ToleranceRuleRequest req) {
        validateRule(req);
        ToleranceRule r = new ToleranceRule();
        r.setCategory(req.getCategory());
        r.setWeighingType(req.getWeighingType());
        r.setMinWeightG(req.getMinWeightG() == null ? 0 : req.getMinWeightG());
        r.setMaxWeightG(req.getMaxWeightG());
        r.setToleranceMode(req.getToleranceMode());
        r.setTolerancePercent(req.getTolerancePercent());
        r.setToleranceFixedG(req.getToleranceFixedG());
        r.setSevereMultiplier(req.getSevereMultiplier() == null
                ? BigDecimal.valueOf(3.0) : req.getSevereMultiplier());
        r.setDescription(req.getDescription());
        r.setEnabled(req.getEnabled() == null ? true : req.getEnabled());
        ruleMapper.insert(r);
        return r;
    }

    public ToleranceRule update(Long id, ToleranceRuleRequest req) {
        ToleranceRule existing = getById(id);
        validateRule(req);
        existing.setCategory(req.getCategory());
        existing.setWeighingType(req.getWeighingType());
        existing.setMinWeightG(req.getMinWeightG() == null ? 0 : req.getMinWeightG());
        existing.setMaxWeightG(req.getMaxWeightG());
        existing.setToleranceMode(req.getToleranceMode());
        existing.setTolerancePercent(req.getTolerancePercent());
        existing.setToleranceFixedG(req.getToleranceFixedG());
        existing.setSevereMultiplier(req.getSevereMultiplier() == null
                ? BigDecimal.valueOf(3.0) : req.getSevereMultiplier());
        existing.setDescription(req.getDescription());
        if (req.getEnabled() != null) {
            existing.setEnabled(req.getEnabled());
        }
        int rows = ruleMapper.update(existing);
        if (rows == 0) {
            throw ApiException.conflict("规则版本冲突，请刷新后重试");
        }
        return ruleMapper.findById(id);
    }

    public void setEnabled(Long id, boolean enabled) {
        if (ruleMapper.updateEnabled(id, enabled) == 0) {
            throw ApiException.notFound("允差规则不存在");
        }
    }

    public void delete(Long id) {
        if (ruleMapper.delete(id) == 0) {
            throw ApiException.notFound("允差规则不存在");
        }
    }

    private void validateRule(ToleranceRuleRequest req) {
        String mode = req.getToleranceMode();
        if (!ToleranceJudgmentService.MODE_PERCENT.equals(mode)
                && !ToleranceJudgmentService.MODE_FIXED.equals(mode)
                && !ToleranceJudgmentService.MODE_MAX_BOTH.equals(mode)) {
            throw ApiException.badRequest("允差模式不合法：percent/fixed/max_both");
        }
        if (ToleranceJudgmentService.MODE_PERCENT.equals(mode)
                && (req.getTolerancePercent() == null || req.getTolerancePercent().compareTo(BigDecimal.ZERO) < 0)) {
            throw ApiException.badRequest("百分比允差模式需配置非负 tolerancePercent");
        }
        if (ToleranceJudgmentService.MODE_FIXED.equals(mode)
                && (req.getToleranceFixedG() == null || req.getToleranceFixedG() < 0)) {
            throw ApiException.badRequest("固定允差模式需配置非负 toleranceFixedG");
        }
        if (ToleranceJudgmentService.MODE_MAX_BOTH.equals(mode)
                && ((req.getTolerancePercent() == null || req.getTolerancePercent().compareTo(BigDecimal.ZERO) < 0)
                || (req.getToleranceFixedG() == null || req.getToleranceFixedG() < 0))) {
            throw ApiException.badRequest("取两者较大值模式需同时配置非负 tolerancePercent 和 toleranceFixedG");
        }
        if (req.getMaxWeightG() != null && req.getMinWeightG() != null
                && req.getMaxWeightG() <= req.getMinWeightG()) {
            throw ApiException.badRequest("最大重量必须大于最小重量");
        }
    }
}
