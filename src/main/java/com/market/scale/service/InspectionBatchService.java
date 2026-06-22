package com.market.scale.service;

import com.market.scale.common.ApiException;
import com.market.scale.dto.InspectionBatchRequest;
import com.market.scale.entity.InspectionBatch;
import com.market.scale.mapper.InspectionBatchMapper;
import com.market.scale.mapper.RecheckRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class InspectionBatchService {

    public static final String STATUS_IN_PROGRESS = "in_progress";
    public static final String STATUS_COMPLETED = "completed";

    private final InspectionBatchMapper batchMapper;
    private final RecheckRecordMapper recheckMapper;

    public InspectionBatchService(InspectionBatchMapper batchMapper, RecheckRecordMapper recheckMapper) {
        this.batchMapper = batchMapper;
        this.recheckMapper = recheckMapper;
    }

    public InspectionBatch getById(Long id) {
        InspectionBatch b = batchMapper.findById(id);
        if (b == null) throw ApiException.notFound("抽检批次不存在");
        return b;
    }

    public Map<String, Object> page(String marketName, String status, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<InspectionBatch> rows = batchMapper.search(marketName, status, (p - 1) * s, s);
        long total = batchMapper.count(marketName, status);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    @Transactional
    public InspectionBatch create(InspectionBatchRequest req) {
        String batchNo = generateBatchNo();
        while (batchMapper.findByBatchNo(batchNo) != null) {
            batchNo = generateBatchNo();
        }
        InspectionBatch b = new InspectionBatch();
        b.setBatchNo(batchNo);
        b.setMarketName(req.getMarketName());
        b.setCategory(req.getCategory());
        b.setInspector(req.getInspector());
        b.setStatus(STATUS_IN_PROGRESS);
        b.setRemark(req.getRemark());
        b.setTotalCount(0);
        b.setPassCount(0);
        b.setShortageCount(0);
        b.setSevereCount(0);
        b.setStartedAt(LocalDateTime.now());
        batchMapper.insert(b);
        return b;
    }

    @Transactional
    public InspectionBatch complete(Long id) {
        InspectionBatch b = getById(id);
        if (!STATUS_IN_PROGRESS.equals(b.getStatus())) {
            throw ApiException.badRequest("批次已完成，不能重复执行");
        }
        List<Map<String, Object>> counts = recheckMapper.countByResultAndBatch(id);
        int pass = 0, shortage = 0, severe = 0;
        for (Map<String, Object> row : counts) {
            String r = (String) row.get("result");
            int cnt = ((Number) row.get("cnt")).intValue();
            if ("pass".equals(r)) pass = cnt;
            else if ("shortage".equals(r)) shortage = cnt;
            else if ("severe_shortage".equals(r)) severe = cnt;
        }
        int total = pass + shortage + severe;
        BigDecimal passRate = total == 0 ? BigDecimal.ZERO
                : BigDecimal.valueOf(pass * 100L).divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
        b.setTotalCount(total);
        b.setPassCount(pass);
        b.setShortageCount(shortage);
        b.setSevereCount(severe);
        b.setPassRate(passRate);
        b.setStatus(STATUS_COMPLETED);
        b.setCompletedAt(LocalDateTime.now());
        batchMapper.update(b);
        return b;
    }

    public Map<String, Object> summary(Long id) {
        InspectionBatch b = getById(id);
        List<Map<String, Object>> stallShortages = recheckMapper.summaryShortageByBatch(id);
        Map<String, Object> result = new HashMap<>();
        result.put("batch", b);
        result.put("stallShortages", stallShortages);
        return result;
    }

    private String generateBatchNo() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int seq = (int) (Math.random() * 900) + 100;
        return "INS-" + date + "-" + seq;
    }
}
