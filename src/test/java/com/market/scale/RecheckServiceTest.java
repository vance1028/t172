package com.market.scale;

import com.market.scale.common.ApiException;
import com.market.scale.dto.JudgmentResult;
import com.market.scale.dto.RecheckRequest;
import com.market.scale.entity.RecheckRecord;
import com.market.scale.entity.Stall;
import com.market.scale.entity.ToleranceRule;
import com.market.scale.mapper.InspectionBatchMapper;
import com.market.scale.mapper.RecheckRecordMapper;
import com.market.scale.mapper.StallMapper;
import com.market.scale.mapper.ToleranceRuleMapper;
import com.market.scale.service.RecheckService;
import com.market.scale.service.StallViolationService;
import com.market.scale.service.ToleranceJudgmentService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecheckServiceTest {

    private final RecheckRecordMapper recheckMapper = mock(RecheckRecordMapper.class);
    private final StallMapper stallMapper = mock(StallMapper.class);
    private final InspectionBatchMapper batchMapper = mock(InspectionBatchMapper.class);
    private final ToleranceRuleMapper ruleMapper = mock(ToleranceRuleMapper.class);
    private final StallViolationService violationService = mock(StallViolationService.class);
    private final ToleranceJudgmentService judgmentService = new ToleranceJudgmentService(ruleMapper);
    private final RecheckService service = new RecheckService(recheckMapper, stallMapper, batchMapper, judgmentService, violationService);

    private RecheckRequest req(int claimed, int actual) {
        RecheckRequest r = new RecheckRequest();
        r.setStallId(1L);
        r.setCommodity("带鱼");
        r.setClaimedWeightG(claimed);
        r.setActualWeightG(actual);
        r.setCategory("aquatic");
        r.setWeighingType("loose");
        return r;
    }

    private ToleranceRule aquaticRule(int minG, Integer maxG, String mode,
                                      BigDecimal percent, Integer fixedG, BigDecimal severe) {
        ToleranceRule r = new ToleranceRule();
        r.setId(1L);
        r.setCategory("aquatic");
        r.setWeighingType("loose");
        r.setMinWeightG(minG);
        r.setMaxWeightG(maxG);
        r.setToleranceMode(mode);
        r.setTolerancePercent(percent);
        r.setToleranceFixedG(fixedG);
        r.setSevereMultiplier(severe);
        r.setVersion(0);
        return r;
    }

    @Test
    void percentToleranceWithinRangePass() {
        Stall s = new Stall();
        s.setCategory("aquatic");
        when(stallMapper.findById(1L)).thenReturn(s);
        when(ruleMapper.matchRule("aquatic", "loose", 1000))
                .thenReturn(aquaticRule(500, null, "percent", new BigDecimal("2.0"), null, new BigDecimal("3.0")));
        RecheckRecord rec = service.create(req(1000, 985));
        assertEquals(15, rec.getShortageG());
        assertEquals(20, rec.getToleranceValueG());
        assertEquals(JudgmentResult.PASS, rec.getResult());
        assertNotNull(rec.getJudgmentBasis());
        assertNotNull(rec.getToleranceRuleVersion());
        verify(recheckMapper).insert(any(RecheckRecord.class));
    }

    @Test
    void percentToleranceShortage() {
        Stall s = new Stall();
        s.setCategory("aquatic");
        when(stallMapper.findById(1L)).thenReturn(s);
        when(ruleMapper.matchRule("aquatic", "loose", 1000))
                .thenReturn(aquaticRule(500, null, "percent", new BigDecimal("2.0"), null, new BigDecimal("3.0")));
        RecheckRecord rec = service.create(req(1000, 960));
        assertEquals(40, rec.getShortageG());
        assertEquals(20, rec.getToleranceValueG());
        assertEquals(60, rec.getToleranceValueG() * 3);
        assertEquals(JudgmentResult.SHORTAGE, rec.getResult());
    }

    @Test
    void percentToleranceSevereShortage() {
        Stall s = new Stall();
        s.setCategory("aquatic");
        when(stallMapper.findById(1L)).thenReturn(s);
        when(ruleMapper.matchRule("aquatic", "loose", 1000))
                .thenReturn(aquaticRule(500, null, "percent", new BigDecimal("2.0"), null, new BigDecimal("3.0")));
        RecheckRecord rec = service.create(req(1000, 900));
        assertEquals(100, rec.getShortageG());
        assertEquals(JudgmentResult.SEVERE_SHORTAGE, rec.getResult());
    }

    @Test
    void maxBothTolerance() {
        Stall s = new Stall();
        s.setCategory("vegetable");
        when(stallMapper.findById(1L)).thenReturn(s);
        ToleranceRule r = aquaticRule(0, null, "max_both", new BigDecimal("1.0"), 20, new BigDecimal("3.0"));
        r.setCategory("vegetable");
        when(ruleMapper.matchRule("vegetable", "loose", 1000)).thenReturn(r);
        RecheckRequest req = req(1000, 975);
        req.setCategory("vegetable");
        RecheckRecord rec = service.create(req);
        assertEquals(25, rec.getShortageG());
        assertEquals(20, rec.getToleranceValueG());
        assertEquals(JudgmentResult.SHORTAGE, rec.getResult());
    }

    @Test
    void actualOverClaimedRejected() {
        assertThrows(ApiException.class, () -> service.create(req(1000, 1100)));
    }

    @Test
    void missingStallRejected() {
        when(stallMapper.findById(1L)).thenReturn(null);
        assertThrows(ApiException.class, () -> service.create(req(1000, 900)));
    }
}
