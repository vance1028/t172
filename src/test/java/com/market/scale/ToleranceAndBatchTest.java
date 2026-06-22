package com.market.scale;

import com.market.scale.common.ApiException;
import com.market.scale.dto.JudgmentResult;
import com.market.scale.dto.RecheckRequest;
import com.market.scale.entity.InspectionBatch;
import com.market.scale.entity.RecheckRecord;
import com.market.scale.entity.Stall;
import com.market.scale.entity.StallViolationAccum;
import com.market.scale.entity.ToleranceRule;
import com.market.scale.mapper.InspectionBatchMapper;
import com.market.scale.mapper.RecheckRecordMapper;
import com.market.scale.mapper.StallMapper;
import com.market.scale.mapper.StallViolationAccumMapper;
import com.market.scale.mapper.ToleranceRuleMapper;
import com.market.scale.service.InspectionBatchService;
import com.market.scale.service.RecheckService;
import com.market.scale.service.StallViolationService;
import com.market.scale.service.ToleranceJudgmentService;
import com.market.scale.service.ToleranceRuleService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ToleranceAndBatchTest {

    // ============== 辅助方法 ==============
    private ToleranceRule buildRule(Long id, String cat, String wt, int minG, Integer maxG,
                                     String mode, BigDecimal percent, Integer fixedG,
                                     BigDecimal severe, int version) {
        ToleranceRule r = new ToleranceRule();
        r.setId(id);
        r.setCategory(cat);
        r.setWeighingType(wt);
        r.setMinWeightG(minG);
        r.setMaxWeightG(maxG);
        r.setToleranceMode(mode);
        r.setTolerancePercent(percent);
        r.setToleranceFixedG(fixedG);
        r.setSevereMultiplier(severe);
        r.setVersion(version);
        r.setDescription("测试规则");
        return r;
    }

    private Stall buildStall(Long id, String market, String category) {
        Stall s = new Stall();
        s.setId(id);
        s.setStallNo("T-" + id);
        s.setMarketName(market);
        s.setMerchantName("摊主" + id);
        s.setCategory(category);
        s.setStatus("active");
        return s;
    }

    private RecheckRequest buildReq(Long stallId, Long batchId, String category,
                                    int claimed, int actual) {
        RecheckRequest r = new RecheckRequest();
        r.setStallId(stallId);
        r.setBatchId(batchId);
        r.setCommodity("测试商品");
        r.setCategory(category);
        r.setWeighingType("loose");
        r.setClaimedWeightG(claimed);
        r.setActualWeightG(actual);
        r.setHandledBy("tester");
        return r;
    }

    // ============== 1. 无上限规则匹配测试 ==============
    @Test
    void noUpperBoundRuleMatchesLargeWeight() {
        ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
        ToleranceJudgmentService judgment = new ToleranceJudgmentService(ruleMapper);

        ToleranceRule rule = buildRule(10L, "aquatic", "loose", 5000, null,
                "percent", new BigDecimal("2.0"), null, BigDecimal.valueOf(3.0), 0);
        assertNull(rule.getMaxWeightG());
        when(ruleMapper.matchRule("aquatic", "loose", 10000)).thenReturn(rule);

        JudgmentResult jr = judgment.judge("aquatic", "loose", 10000, 9700);
        assertEquals(200, jr.getToleranceValueG());
        assertEquals(300, jr.getShortageG());
        assertEquals(JudgmentResult.SHORTAGE, jr.getResult());
    }

    @Test
    void noUpperBoundRule50KgSevere() {
        ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
        ToleranceJudgmentService judgment = new ToleranceJudgmentService(ruleMapper);
        ToleranceRule rule = buildRule(11L, "meat", "loose", 500, null,
                "percent", new BigDecimal("1.5"), null, BigDecimal.valueOf(3.0), 0);
        when(ruleMapper.matchRule("meat", "loose", 50000)).thenReturn(rule);

        JudgmentResult jr = judgment.judge("meat", "loose", 50000, 46000);
        assertEquals(750, jr.getToleranceValueG());
        assertEquals(2250, jr.getSevereThresholdG());
        assertEquals(4000, jr.getShortageG());
        assertEquals(JudgmentResult.SEVERE_SHORTAGE, jr.getResult());
    }

    // ============== 2. 批次品类/市场拦截测试 ==============
    @Test
    void batchMarketMismatchRejected() {
        RecheckRecordMapper recheckMapper = mock(RecheckRecordMapper.class);
        StallMapper stallMapper = mock(StallMapper.class);
        InspectionBatchMapper batchMapper = mock(InspectionBatchMapper.class);
        ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
        StallViolationService vs = mock(StallViolationService.class);
        ToleranceJudgmentService js = new ToleranceJudgmentService(ruleMapper);
        RecheckService service = new RecheckService(recheckMapper, stallMapper, batchMapper, js, vs);

        when(stallMapper.findById(1L)).thenReturn(buildStall(1L, "城东综合农贸市场", "vegetable"));
        InspectionBatch batch = new InspectionBatch();
        batch.setId(100L);
        batch.setBatchNo("INS-TEST-001");
        batch.setMarketName("滨江生鲜市场");
        batch.setStatus(InspectionBatchService.STATUS_IN_PROGRESS);
        when(batchMapper.findById(100L)).thenReturn(batch);

        RecheckRequest req = buildReq(1L, 100L, "vegetable", 1000, 980);
        ApiException ex = assertThrows(ApiException.class, () -> service.create(req));
        assertTrue(ex.getMessage().contains("不一致"), "应提示市场不一致");
    }

    @Test
    void batchCategoryMismatchRejected() {
        RecheckRecordMapper recheckMapper = mock(RecheckRecordMapper.class);
        StallMapper stallMapper = mock(StallMapper.class);
        InspectionBatchMapper batchMapper = mock(InspectionBatchMapper.class);
        ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
        StallViolationService vs = mock(StallViolationService.class);
        ToleranceJudgmentService js = new ToleranceJudgmentService(ruleMapper);
        RecheckService service = new RecheckService(recheckMapper, stallMapper, batchMapper, js, vs);

        when(stallMapper.findById(1L)).thenReturn(buildStall(1L, "城东综合农贸市场", "aquatic"));
        InspectionBatch batch = new InspectionBatch();
        batch.setId(101L);
        batch.setBatchNo("INS-TEST-002");
        batch.setMarketName("城东综合农贸市场");
        batch.setCategory("vegetable");
        batch.setStatus(InspectionBatchService.STATUS_IN_PROGRESS);
        when(batchMapper.findById(101L)).thenReturn(batch);

        RecheckRequest req = buildReq(1L, 101L, null, 1000, 980);
        ApiException ex = assertThrows(ApiException.class, () -> service.create(req));
        assertTrue(ex.getMessage().contains("品类不匹配"), "应提示品类不匹配");
    }

    @Test
    void batchCategoryMismatchButRecordOverrideRejected() {
        RecheckRecordMapper recheckMapper = mock(RecheckRecordMapper.class);
        StallMapper stallMapper = mock(StallMapper.class);
        InspectionBatchMapper batchMapper = mock(InspectionBatchMapper.class);
        ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
        StallViolationService vs = mock(StallViolationService.class);
        ToleranceJudgmentService js = new ToleranceJudgmentService(ruleMapper);
        RecheckService service = new RecheckService(recheckMapper, stallMapper, batchMapper, js, vs);

        when(stallMapper.findById(1L)).thenReturn(buildStall(1L, "城东综合农贸市场", "vegetable"));
        InspectionBatch batch = new InspectionBatch();
        batch.setId(102L);
        batch.setBatchNo("INS-TEST-003");
        batch.setMarketName("城东综合农贸市场");
        batch.setCategory("aquatic");
        batch.setStatus(InspectionBatchService.STATUS_IN_PROGRESS);
        when(batchMapper.findById(102L)).thenReturn(batch);

        RecheckRequest req = buildReq(1L, 102L, "vegetable", 1000, 980);
        ApiException ex = assertThrows(ApiException.class, () -> service.create(req));
        assertTrue(ex.getMessage().contains("品类不匹配"));
    }

    // ============== 3. 历史快照隔离测试 ==============
    @Test
    void snapshotIsolationOldRuleJudgmentUnchanged() {
        ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
        ToleranceJudgmentService judgment = new ToleranceJudgmentService(ruleMapper);

        ToleranceRule oldRule = buildRule(20L, "vegetable", "loose", 500, 2000,
                "percent", new BigDecimal("2.5"), null, BigDecimal.valueOf(3.0), 0);

        JudgmentResult first = judgment.judgeByRule(oldRule, 1000, 970);
        assertEquals(JudgmentResult.SHORTAGE, first.getResult());
        assertEquals(25, first.getToleranceValueG());
        assertEquals(30, first.getShortageG());
        String snapshotJson = first.getRuleSnapshotJson();
        assertNotNull(snapshotJson);

        ToleranceRule newRule = buildRule(20L, "vegetable", "loose", 500, 2000,
                "percent", new BigDecimal("1.0"), null, BigDecimal.valueOf(2.0), 1);
        when(ruleMapper.findById(20L)).thenReturn(newRule);

        JudgmentResult fromSnapshot = judgment.judgeBySnapshot(snapshotJson, 1000, 970);
        assertEquals(JudgmentResult.SHORTAGE, fromSnapshot.getResult());
        assertEquals(25, fromSnapshot.getToleranceValueG(), "按快照应仍用旧2.5%允差，允差=25克");
        assertEquals(30, fromSnapshot.getShortageG());

        JudgmentResult tight = judgment.judgeByRule(newRule, 1000, 970);
        assertEquals(10, tight.getToleranceValueG(), "新规则允差收紧到1%=10克");
        assertEquals(JudgmentResult.SEVERE_SHORTAGE, tight.getResult(), "30克短缺超过允差×2倍=20克，应判严重短缺");
    }

    // 扩展测试：快照规则修改后新判若用新规则则结果变化，但快照重算不变
    @Test
    void snapshotSevereMultiplierIsolation() {
        ToleranceJudgmentService judgment = new ToleranceJudgmentService(mock(ToleranceRuleMapper.class));
        ToleranceRule old = buildRule(21L, "aquatic", "loose", 500, null,
                "percent", new BigDecimal("2.0"), null, BigDecimal.valueOf(3.0), 0);
        ToleranceRule tight = buildRule(21L, "aquatic", "loose", 500, null,
                "percent", new BigDecimal("2.0"), null, BigDecimal.valueOf(1.5), 1);

        // 按旧规则: 允差=20, 严重阈值=60, 短缺50算shortage
        JudgmentResult j1 = judgment.judgeByRule(old, 1000, 950);
        assertEquals(JudgmentResult.SHORTAGE, j1.getResult());

        // 保存快照后，即便规则收紧倍率为1.5（新严重阈值=30，短缺50应算严重）
        // 用快照重算仍按旧倍率3.0，保持shortage
        JudgmentResult fromSnap = judgment.judgeBySnapshot(j1.getRuleSnapshotJson(), 1000, 950);
        assertEquals(JudgmentResult.SHORTAGE, fromSnap.getResult());

        // 用新规则直接判，则变为severe
        JudgmentResult jTight = judgment.judgeByRule(tight, 1000, 950);
        assertEquals(JudgmentResult.SEVERE_SHORTAGE, jTight.getResult());
    }

    // ============== 4. 重点关注累计测试 ==============
    @Test
    void accumulateShortageThreeTimesEntersFocus() {
        StallViolationAccumMapper accumMapper = mock(StallViolationAccumMapper.class);
        StallViolationService service = new StallViolationService(accumMapper);

        JudgmentResult jr1 = new JudgmentResult();
        jr1.setResult(JudgmentResult.SHORTAGE);
        jr1.setShortageG(25);

        JudgmentResult jr2 = new JudgmentResult();
        jr2.setResult(JudgmentResult.SHORTAGE);
        jr2.setShortageG(40);

        JudgmentResult jr3 = new JudgmentResult();
        jr3.setResult(JudgmentResult.SEVERE_SHORTAGE);
        jr3.setShortageG(150);

        when(accumMapper.findByStallId(5L))
                .thenReturn(null)
                .thenReturn(buildAccum(5L, 0, 0, 0, 0, false, 3))
                .thenReturn(buildAccum(5L, 1, 1, 0, 25, false, 3))
                .thenReturn(buildAccum(5L, 2, 2, 0, 40, false, 3));

        ArgumentCaptor<StallViolationAccum> captor = ArgumentCaptor.forClass(StallViolationAccum.class);

        service.accumulate(5L, jr1);
        verify(accumMapper, times(1)).insert(any());

        service.accumulate(5L, jr2);
        service.accumulate(5L, jr3);

        verify(accumMapper, times(3)).update(captor.capture());
        StallViolationAccum last = captor.getAllValues().get(captor.getAllValues().size() - 1);
        assertEquals(3, last.getTotalRecheckCount());
        assertEquals(3, last.getShortageCount());
        assertEquals(1, last.getSevereCount());
        assertEquals(150, last.getMaxShortageG());
        assertTrue(last.getFocusFlag(), "达到阈值3次应进入重点关注");
    }

    @Test
    void passRecordDoesNotAffectShortageCount() {
        StallViolationAccumMapper accumMapper = mock(StallViolationAccumMapper.class);
        StallViolationService service = new StallViolationService(accumMapper);

        JudgmentResult pass = new JudgmentResult();
        pass.setResult(JudgmentResult.PASS);
        pass.setShortageG(5);

        when(accumMapper.findByStallId(6L))
                .thenReturn(buildAccum(6L, 1, 1, 0, 30, false, 3));

        ArgumentCaptor<StallViolationAccum> captor = ArgumentCaptor.forClass(StallViolationAccum.class);
        service.accumulate(6L, pass);
        verify(accumMapper).update(captor.capture());

        StallViolationAccum after = captor.getValue();
        assertEquals(2, after.getTotalRecheckCount());
        assertEquals(1, after.getShortageCount(), "pass记录不应增加短缺次数");
        assertEquals(0, after.getSevereCount());
        assertFalse(after.getFocusFlag());
    }

    private StallViolationAccum buildAccum(Long stallId, int total, int shortage, int severe,
                                           int maxShortage, boolean focus, int threshold) {
        StallViolationAccum a = new StallViolationAccum();
        a.setId(stallId * 100);
        a.setStallId(stallId);
        a.setTotalRecheckCount(total);
        a.setShortageCount(shortage);
        a.setSevereCount(severe);
        a.setMaxShortageG(maxShortage);
        a.setFocusFlag(focus);
        a.setFocusThreshold(threshold);
        return a;
    }
}
