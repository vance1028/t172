package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.dto.JudgmentResult;
import com.market.scale.dto.RecheckRequest;
import com.market.scale.entity.InspectionBatch;
import com.market.scale.entity.RecheckRecord;
import com.market.scale.entity.Stall;
import com.market.scale.mapper.InspectionBatchMapper;
import com.market.scale.mapper.RecheckRecordMapper;
import com.market.scale.mapper.StallMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecheckService {

    private final RecheckRecordMapper recheckMapper;
    private final StallMapper stallMapper;
    private final InspectionBatchMapper batchMapper;
    private final ToleranceJudgmentService judgmentService;
    private final StallViolationService violationService;

    public RecheckService(RecheckRecordMapper recheckMapper, StallMapper stallMapper,
                          InspectionBatchMapper batchMapper, ToleranceJudgmentService judgmentService,
                          StallViolationService violationService) {
        this.recheckMapper = recheckMapper;
        this.stallMapper = stallMapper;
        this.batchMapper = batchMapper;
        this.judgmentService = judgmentService;
        this.violationService = violationService;
    }

    public Map<String, Object> page(Long stallId, Long batchId, String result, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<RecheckRecord> rows = recheckMapper.search(stallId, batchId, result, (p - 1) * s, s);
        long total = recheckMapper.count(stallId, batchId, result);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    public RecheckRecord getById(Long id) {
        RecheckRecord r = recheckMapper.findById(id);
        if (r == null) throw ApiException.notFound("复称记录不存在");
        return r;
    }

    public Map<String, Object> judgePreview(RecheckRequest req) {
        Stall stall = stallMapper.findById(req.getStallId());
        if (stall == null) throw ApiException.badRequest("摊位不存在");
        String category = (req.getCategory() != null && !req.getCategory().isBlank())
                ? req.getCategory() : stall.getCategory();
        String weighingType = (req.getWeighingType() != null && !req.getWeighingType().isBlank())
                ? req.getWeighingType() : ToleranceJudgmentService.DEFAULT_WEIGHING_TYPE;
        JudgmentResult jr = judgmentService.judge(category, weighingType,
                req.getClaimedWeightG(), req.getActualWeightG());
        Map<String, Object> res = new HashMap<>();
        res.put("result", jr.getResult());
        res.put("shortageG", jr.getShortageG());
        res.put("toleranceValueG", jr.getToleranceValueG());
        res.put("severeThresholdG", jr.getSevereThresholdG());
        res.put("judgmentBasis", jr.getBasis());
        res.put("ruleId", jr.getRuleId());
        return res;
    }

    @Transactional
    public RecheckRecord create(RecheckRequest req) {
        Stall stall = stallMapper.findById(req.getStallId());
        if (stall == null) throw ApiException.badRequest("摊位不存在");

        if (req.getBatchId() != null) {
            InspectionBatch batch = batchMapper.findById(req.getBatchId());
            if (batch == null) {
                throw ApiException.badRequest("关联的抽检批次不存在");
            }
            if (!InspectionBatchService.STATUS_IN_PROGRESS.equals(batch.getStatus())) {
                throw ApiException.badRequest("抽检批次已完成，不能新增复称记录");
            }
            if (!batch.getMarketName().equals(stall.getMarketName())) {
                throw ApiException.badRequest("摊位所属市场[" + stall.getMarketName()
                        + "]与抽检批次市场[" + batch.getMarketName() + "]不一致");
            }
            if (batch.getCategory() != null && !batch.getCategory().isBlank()) {
                String recordCat = (req.getCategory() != null && !req.getCategory().isBlank())
                        ? req.getCategory() : stall.getCategory();
                if (!batch.getCategory().equals(recordCat)) {
                    throw ApiException.badRequest("品类不匹配：抽检批次限定品类[" + batch.getCategory()
                            + "]，复称记录品类[" + recordCat + "]");
                }
            }
        }

        String category = (req.getCategory() != null && !req.getCategory().isBlank())
                ? req.getCategory() : stall.getCategory();
        String weighingType = (req.getWeighingType() != null && !req.getWeighingType().isBlank())
                ? req.getWeighingType() : ToleranceJudgmentService.DEFAULT_WEIGHING_TYPE;

        JudgmentResult jr = judgmentService.judge(category, weighingType,
                req.getClaimedWeightG(), req.getActualWeightG());

        RecheckRecord rec = new RecheckRecord();
        rec.setStallId(req.getStallId());
        rec.setBatchId(req.getBatchId());
        rec.setCommodity(req.getCommodity());
        rec.setCategory(category);
        rec.setWeighingType(weighingType);
        rec.setClaimedWeightG(req.getClaimedWeightG());
        rec.setActualWeightG(req.getActualWeightG());
        rec.setShortageG(jr.getShortageG());
        rec.setToleranceRuleId(jr.getRuleId());
        rec.setToleranceRuleVersion(jr.getRuleSnapshotJson());
        rec.setToleranceValueG(jr.getToleranceValueG());
        rec.setResult(jr.getResult());
        rec.setJudgmentBasis(jr.getBasis());
        rec.setHandledBy(req.getHandledBy());
        rec.setRemark(req.getRemark());
        rec.setRecheckedAt(LocalDateTime.now());
        recheckMapper.insert(rec);

        violationService.accumulate(req.getStallId(), jr);

        return rec;
    }
}
