package com.market.scale.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.scale.common.ApiException;
import com.market.scale.dto.JudgmentResult;
import com.market.scale.entity.ToleranceRule;
import com.market.scale.mapper.ToleranceRuleMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
public class ToleranceJudgmentService {

    public static final String MODE_PERCENT = "percent";
    public static final String MODE_FIXED = "fixed";
    public static final String MODE_MAX_BOTH = "max_both";

    public static final String DEFAULT_CATEGORY = "other";
    public static final String DEFAULT_WEIGHING_TYPE = "loose";

    private final ToleranceRuleMapper ruleMapper;
    private final ObjectMapper objectMapper;

    public ToleranceJudgmentService(ToleranceRuleMapper ruleMapper) {
        this.ruleMapper = ruleMapper;
        this.objectMapper = new ObjectMapper();
    }

    public JudgmentResult judge(String category, String weighingType,
                                int claimedWeightG, int actualWeightG) {
        String cat = (category == null || category.isBlank()) ? DEFAULT_CATEGORY : category;
        String wt = (weighingType == null || weighingType.isBlank()) ? DEFAULT_WEIGHING_TYPE : weighingType;

        if (actualWeightG > claimedWeightG) {
            throw ApiException.badRequest("实测重量大于计价重量，请核对录入");
        }
        int shortage = claimedWeightG - actualWeightG;

        ToleranceRule rule = ruleMapper.matchRule(cat, wt, claimedWeightG);
        if (rule == null) {
            rule = ruleMapper.matchRule(DEFAULT_CATEGORY, DEFAULT_WEIGHING_TYPE, claimedWeightG);
        }
        if (rule == null) {
            JudgmentResult fallback = new JudgmentResult();
            fallback.setShortageG(shortage);
            fallback.setToleranceValueG(0);
            fallback.setSevereThresholdG(0);
            fallback.setResult(shortage > 0 ? JudgmentResult.SHORTAGE : JudgmentResult.PASS);
            fallback.setBasis("无匹配允差规则，按零允差判定：短缺" + shortage + "克");
            fallback.setRuleSnapshotJson(null);
            fallback.setRuleId(null);
            return fallback;
        }

        int toleranceValueG = computeTolerance(rule, claimedWeightG);
        int severeThresholdG = computeSevereThreshold(rule, toleranceValueG);
        String result;
        if (shortage <= toleranceValueG) {
            result = JudgmentResult.PASS;
        } else if (shortage <= severeThresholdG) {
            result = JudgmentResult.SHORTAGE;
        } else {
            result = JudgmentResult.SEVERE_SHORTAGE;
        }

        JudgmentResult jr = new JudgmentResult();
        jr.setResult(result);
        jr.setShortageG(shortage);
        jr.setToleranceValueG(toleranceValueG);
        jr.setSevereThresholdG(severeThresholdG);
        jr.setBasis(buildBasisText(rule, claimedWeightG, shortage, toleranceValueG, severeThresholdG, result));
        jr.setRuleSnapshotJson(serializeRuleSnapshot(rule));
        jr.setRuleId(rule.getId());
        return jr;
    }

    public JudgmentResult judgeBySnapshot(String ruleSnapshotJson, int claimedWeightG, int actualWeightG) {
        if (actualWeightG > claimedWeightG) {
            throw ApiException.badRequest("实测重量大于计价重量，请核对录入");
        }
        int shortage = claimedWeightG - actualWeightG;

        if (ruleSnapshotJson == null || ruleSnapshotJson.isBlank()) {
            JudgmentResult fallback = new JudgmentResult();
            fallback.setShortageG(shortage);
            fallback.setToleranceValueG(0);
            fallback.setSevereThresholdG(0);
            fallback.setResult(shortage > 0 ? JudgmentResult.SHORTAGE : JudgmentResult.PASS);
            fallback.setBasis("无匹配允差规则，按零允差判定：短缺" + shortage + "克");
            fallback.setRuleSnapshotJson(null);
            fallback.setRuleId(null);
            return fallback;
        }

        ToleranceRule rule = deserializeRuleSnapshot(ruleSnapshotJson);
        int toleranceValueG = computeTolerance(rule, claimedWeightG);
        int severeThresholdG = computeSevereThreshold(rule, toleranceValueG);
        String result;
        if (shortage <= toleranceValueG) {
            result = JudgmentResult.PASS;
        } else if (shortage <= severeThresholdG) {
            result = JudgmentResult.SHORTAGE;
        } else {
            result = JudgmentResult.SEVERE_SHORTAGE;
        }

        JudgmentResult jr = new JudgmentResult();
        jr.setResult(result);
        jr.setShortageG(shortage);
        jr.setToleranceValueG(toleranceValueG);
        jr.setSevereThresholdG(severeThresholdG);
        jr.setBasis(buildBasisText(rule, claimedWeightG, shortage, toleranceValueG, severeThresholdG, result));
        jr.setRuleSnapshotJson(ruleSnapshotJson);
        jr.setRuleId(rule.getId());
        return jr;
    }

    public int computeTolerance(ToleranceRule rule, int claimedWeightG) {
        String mode = rule.getToleranceMode();
        BigDecimal percent = rule.getTolerancePercent();
        Integer fixed = rule.getToleranceFixedG();

        if (MODE_PERCENT.equals(mode)) {
            if (percent == null) return 0;
            return BigDecimal.valueOf(claimedWeightG)
                    .multiply(percent)
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                    .intValue();
        }
        if (MODE_FIXED.equals(mode)) {
            return fixed == null ? 0 : fixed;
        }
        if (MODE_MAX_BOTH.equals(mode)) {
            int pv = 0;
            if (percent != null) {
                pv = BigDecimal.valueOf(claimedWeightG)
                        .multiply(percent)
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                        .intValue();
            }
            int fv = fixed == null ? 0 : fixed;
            return Math.max(pv, fv);
        }
        return 0;
    }

    private int computeSevereThreshold(ToleranceRule rule, int toleranceValueG) {
        BigDecimal multiplier = rule.getSevereMultiplier();
        if (multiplier == null) return toleranceValueG * 3;
        return BigDecimal.valueOf(toleranceValueG)
                .multiply(multiplier)
                .setScale(0, RoundingMode.HALF_UP)
                .intValue();
    }

    private String buildBasisText(ToleranceRule rule, int claimed, int shortage,
                                  int tolG, int severeG, String result) {
        String modeText;
        String mode = rule.getToleranceMode();
        if (MODE_PERCENT.equals(mode)) {
            modeText = "百分比允差" + rule.getTolerancePercent() + "%";
        } else if (MODE_FIXED.equals(mode)) {
            modeText = "固定允差" + rule.getToleranceFixedG() + "克";
        } else {
            modeText = "取百分比允差" + rule.getTolerancePercent() + "%与固定允差"
                    + rule.getToleranceFixedG() + "克之较大值";
        }
        String resultText;
        if (JudgmentResult.PASS.equals(result)) {
            resultText = "合格（在允差内）";
        } else if (JudgmentResult.SHORTAGE.equals(result)) {
            resultText = "短缺（超出允差但未达严重标准）";
        } else {
            resultText = "严重短缺（超出允差×" + rule.getSevereMultiplier() + "）";
        }
        return "品类[" + rule.getCategory() + "]称量方式[" + rule.getWeighingType()
                + "]重量" + claimed + "g，适用规则#" + rule.getId()
                + "：" + modeText
                + "，计算允差" + tolG + "克，严重阈值" + severeG + "克；"
                + "实测短缺" + shortage + "克，判定：" + resultText;
    }

    private String serializeRuleSnapshot(ToleranceRule rule) {
        try {
            Map<String, Object> snap = new HashMap<>();
            snap.put("id", rule.getId());
            snap.put("category", rule.getCategory());
            snap.put("weighingType", rule.getWeighingType());
            snap.put("minWeightG", rule.getMinWeightG());
            snap.put("maxWeightG", rule.getMaxWeightG());
            snap.put("toleranceMode", rule.getToleranceMode());
            snap.put("tolerancePercent", rule.getTolerancePercent());
            snap.put("toleranceFixedG", rule.getToleranceFixedG());
            snap.put("severeMultiplier", rule.getSevereMultiplier());
            snap.put("description", rule.getDescription());
            snap.put("version", rule.getVersion());
            snap.put("snapshottedAt", java.time.LocalDateTime.now().toString());
            return objectMapper.writeValueAsString(snap);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("序列化允差规则快照失败", e);
        }
    }

    private ToleranceRule deserializeRuleSnapshot(String json) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> m = objectMapper.readValue(json, Map.class);
            ToleranceRule r = new ToleranceRule();
            if (m.get("id") != null) {
                r.setId(((Number) m.get("id")).longValue());
            }
            r.setCategory((String) m.get("category"));
            r.setWeighingType((String) m.get("weighingType"));
            if (m.get("minWeightG") != null) {
                r.setMinWeightG(((Number) m.get("minWeightG")).intValue());
            }
            if (m.get("maxWeightG") != null) {
                r.setMaxWeightG(((Number) m.get("maxWeightG")).intValue());
            }
            r.setToleranceMode((String) m.get("toleranceMode"));
            if (m.get("tolerancePercent") != null) {
                r.setTolerancePercent(new BigDecimal(m.get("tolerancePercent").toString()));
            }
            if (m.get("toleranceFixedG") != null) {
                r.setToleranceFixedG(((Number) m.get("toleranceFixedG")).intValue());
            }
            if (m.get("severeMultiplier") != null) {
                r.setSevereMultiplier(new BigDecimal(m.get("severeMultiplier").toString()));
            }
            r.setDescription((String) m.get("description"));
            if (m.get("version") != null) {
                r.setVersion(((Number) m.get("version")).intValue());
            }
            return r;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("反序列化允差规则快照失败", e);
        }
    }
}
