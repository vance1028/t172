package com.market.scale.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ToleranceRuleRequest {
    @NotBlank(message = "经营品类不能为空")
    private String category;
    @NotBlank(message = "称量方式不能为空")
    private String weighingType;
    @NotNull(message = "最小适用重量不能为空")
    private Integer minWeightG;
    private Integer maxWeightG;
    @NotBlank(message = "允差模式不能为空")
    private String toleranceMode;
    private BigDecimal tolerancePercent;
    private Integer toleranceFixedG;
    private BigDecimal severeMultiplier;
    private String description;
    private Boolean enabled;

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getWeighingType() { return weighingType; }
    public void setWeighingType(String weighingType) { this.weighingType = weighingType; }

    public Integer getMinWeightG() { return minWeightG; }
    public void setMinWeightG(Integer minWeightG) { this.minWeightG = minWeightG; }

    public Integer getMaxWeightG() { return maxWeightG; }
    public void setMaxWeightG(Integer maxWeightG) { this.maxWeightG = maxWeightG; }

    public String getToleranceMode() { return toleranceMode; }
    public void setToleranceMode(String toleranceMode) { this.toleranceMode = toleranceMode; }

    public BigDecimal getTolerancePercent() { return tolerancePercent; }
    public void setTolerancePercent(BigDecimal tolerancePercent) { this.tolerancePercent = tolerancePercent; }

    public Integer getToleranceFixedG() { return toleranceFixedG; }
    public void setToleranceFixedG(Integer toleranceFixedG) { this.toleranceFixedG = toleranceFixedG; }

    public BigDecimal getSevereMultiplier() { return severeMultiplier; }
    public void setSevereMultiplier(BigDecimal severeMultiplier) { this.severeMultiplier = severeMultiplier; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
