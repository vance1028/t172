package com.market.scale.dto;

public class JudgmentResult {
    public static final String PASS = "pass";
    public static final String SHORTAGE = "shortage";
    public static final String SEVERE_SHORTAGE = "severe_shortage";

    private String result;
    private int toleranceValueG;
    private int shortageG;
    private int severeThresholdG;
    private String basis;
    private String ruleSnapshotJson;
    private Long ruleId;

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public int getToleranceValueG() { return toleranceValueG; }
    public void setToleranceValueG(int toleranceValueG) { this.toleranceValueG = toleranceValueG; }

    public int getShortageG() { return shortageG; }
    public void setShortageG(int shortageG) { this.shortageG = shortageG; }

    public int getSevereThresholdG() { return severeThresholdG; }
    public void setSevereThresholdG(int severeThresholdG) { this.severeThresholdG = severeThresholdG; }

    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }

    public String getRuleSnapshotJson() { return ruleSnapshotJson; }
    public void setRuleSnapshotJson(String ruleSnapshotJson) { this.ruleSnapshotJson = ruleSnapshotJson; }

    public Long getRuleId() { return ruleId; }
    public void setRuleId(Long ruleId) { this.ruleId = ruleId; }
}
