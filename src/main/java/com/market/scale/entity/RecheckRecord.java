package com.market.scale.entity;

import java.time.LocalDateTime;

public class RecheckRecord {
    private Long id;
    private Long stallId;
    private Long batchId;
    private String commodity;
    private String category;
    private String weighingType;
    private Integer claimedWeightG;
    private Integer actualWeightG;
    private Integer shortageG;
    private Long toleranceRuleId;
    private String toleranceRuleVersion;
    private Integer toleranceValueG;
    private String result;
    private String judgmentBasis;
    private String handledBy;
    private String remark;
    private LocalDateTime recheckedAt;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }

    public String getCommodity() { return commodity; }
    public void setCommodity(String commodity) { this.commodity = commodity; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getWeighingType() { return weighingType; }
    public void setWeighingType(String weighingType) { this.weighingType = weighingType; }

    public Integer getClaimedWeightG() { return claimedWeightG; }
    public void setClaimedWeightG(Integer claimedWeightG) { this.claimedWeightG = claimedWeightG; }

    public Integer getActualWeightG() { return actualWeightG; }
    public void setActualWeightG(Integer actualWeightG) { this.actualWeightG = actualWeightG; }

    public Integer getShortageG() { return shortageG; }
    public void setShortageG(Integer shortageG) { this.shortageG = shortageG; }

    public Long getToleranceRuleId() { return toleranceRuleId; }
    public void setToleranceRuleId(Long toleranceRuleId) { this.toleranceRuleId = toleranceRuleId; }

    public String getToleranceRuleVersion() { return toleranceRuleVersion; }
    public void setToleranceRuleVersion(String toleranceRuleVersion) { this.toleranceRuleVersion = toleranceRuleVersion; }

    public Integer getToleranceValueG() { return toleranceValueG; }
    public void setToleranceValueG(Integer toleranceValueG) { this.toleranceValueG = toleranceValueG; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public String getJudgmentBasis() { return judgmentBasis; }
    public void setJudgmentBasis(String judgmentBasis) { this.judgmentBasis = judgmentBasis; }

    public String getHandledBy() { return handledBy; }
    public void setHandledBy(String handledBy) { this.handledBy = handledBy; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getRecheckedAt() { return recheckedAt; }
    public void setRecheckedAt(LocalDateTime recheckedAt) { this.recheckedAt = recheckedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
