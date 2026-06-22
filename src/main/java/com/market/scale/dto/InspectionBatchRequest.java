package com.market.scale.dto;

import jakarta.validation.constraints.NotBlank;

public class InspectionBatchRequest {
    @NotBlank(message = "市场名称不能为空")
    private String marketName;
    private String category;
    private String inspector;
    private String remark;

    public String getMarketName() { return marketName; }
    public void setMarketName(String marketName) { this.marketName = marketName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getInspector() { return inspector; }
    public void setInspector(String inspector) { this.inspector = inspector; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
}
