package com.market.scale.entity;

import java.time.LocalDateTime;

public class StallViolationAccum {
    private Long id;
    private Long stallId;
    private Integer totalRecheckCount;
    private Integer shortageCount;
    private Integer severeCount;
    private Integer maxShortageG;
    private Boolean focusFlag;
    private Integer focusThreshold;
    private LocalDateTime lastShortageAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStallId() { return stallId; }
    public void setStallId(Long stallId) { this.stallId = stallId; }

    public Integer getTotalRecheckCount() { return totalRecheckCount; }
    public void setTotalRecheckCount(Integer totalRecheckCount) { this.totalRecheckCount = totalRecheckCount; }

    public Integer getShortageCount() { return shortageCount; }
    public void setShortageCount(Integer shortageCount) { this.shortageCount = shortageCount; }

    public Integer getSevereCount() { return severeCount; }
    public void setSevereCount(Integer severeCount) { this.severeCount = severeCount; }

    public Integer getMaxShortageG() { return maxShortageG; }
    public void setMaxShortageG(Integer maxShortageG) { this.maxShortageG = maxShortageG; }

    public Boolean getFocusFlag() { return focusFlag; }
    public void setFocusFlag(Boolean focusFlag) { this.focusFlag = focusFlag; }

    public Integer getFocusThreshold() { return focusThreshold; }
    public void setFocusThreshold(Integer focusThreshold) { this.focusThreshold = focusThreshold; }

    public LocalDateTime getLastShortageAt() { return lastShortageAt; }
    public void setLastShortageAt(LocalDateTime lastShortageAt) { this.lastShortageAt = lastShortageAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
