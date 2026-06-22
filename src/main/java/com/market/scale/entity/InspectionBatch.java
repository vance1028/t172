package com.market.scale.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InspectionBatch {
    private Long id;
    private String batchNo;
    private String marketName;
    private String category;
    private String inspector;
    private String status;
    private Integer totalCount;
    private Integer passCount;
    private Integer shortageCount;
    private Integer severeCount;
    private BigDecimal passRate;
    private String remark;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBatchNo() { return batchNo; }
    public void setBatchNo(String batchNo) { this.batchNo = batchNo; }

    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalCount() { return totalCount; }
    public void setTotalCount(Integer totalCount) { this.totalCount = totalCount; }

    public Integer getPassCount() { return passCount; }
    public void setPassCount(Integer passCount) { this.passCount = passCount; }

    public Integer getShortageCount() { return shortageCount; }
    public void setShortageCount(Integer shortageCount) { this.shortageCount = shortageCount; }

    public Integer getSevereCount() { return severeCount; }
    public void setSevereCount(Integer severeCount) { this.severeCount = severeCount; }

    public BigDecimal getPassRate() { return passRate; }
    public void setPassRate(BigDecimal passRate) { this.passRate = passRate; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
