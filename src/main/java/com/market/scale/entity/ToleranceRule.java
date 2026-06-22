package com.market.scale.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ToleranceRule {
    private Long id;
    private String category;
    private String weighingType;
    private Integer minWeightG;
    private Integer maxWeightG;
    private String toleranceMode;
    private BigDecimal tolerancePercent;
    private Integer toleranceFixedG;
    private BigDecimal severeMultiplier;
    private String description;
    private Boolean enabled;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
