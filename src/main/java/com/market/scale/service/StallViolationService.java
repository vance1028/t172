package com.market.scale.service;

import com.market.scale.dto.JudgmentResult;
import com.market.scale.entity.StallViolationAccum;
import com.market.scale.mapper.StallViolationAccumMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StallViolationService {

    private final StallViolationAccumMapper accumMapper;

    public StallViolationService(StallViolationAccumMapper accumMapper) {
        this.accumMapper = accumMapper;
    }

    public StallViolationAccum getByStall(Long stallId) {
        return accumMapper.findByStallId(stallId);
    }

    public Map<String, Object> pageFocused(Boolean focusFlag, int page, int size) {
        int p = Math.max(page, 1);
        int s = Math.min(Math.max(size, 1), 200);
        List<StallViolationAccum> rows = accumMapper.search(focusFlag, (p - 1) * s, s);
        long total = accumMapper.count(focusFlag);
        Map<String, Object> res = new HashMap<>();
        res.put("items", rows);
        res.put("total", total);
        res.put("page", p);
        res.put("size", s);
        return res;
    }

    @Transactional
    public void accumulate(Long stallId, JudgmentResult jr) {
        StallViolationAccum acc = accumMapper.findByStallId(stallId);
        if (acc == null) {
            acc = new StallViolationAccum();
            acc.setStallId(stallId);
            acc.setTotalRecheckCount(0);
            acc.setShortageCount(0);
            acc.setSevereCount(0);
            acc.setMaxShortageG(0);
            acc.setFocusFlag(false);
            acc.setFocusThreshold(3);
            accumMapper.insert(acc);
            acc = accumMapper.findByStallId(stallId);
        }
        acc.setTotalRecheckCount(acc.getTotalRecheckCount() + 1);
        if (JudgmentResult.SHORTAGE.equals(jr.getResult())) {
            acc.setShortageCount(acc.getShortageCount() + 1);
            acc.setLastShortageAt(LocalDateTime.now());
        } else if (JudgmentResult.SEVERE_SHORTAGE.equals(jr.getResult())) {
            acc.setShortageCount(acc.getShortageCount() + 1);
            acc.setSevereCount(acc.getSevereCount() + 1);
            acc.setLastShortageAt(LocalDateTime.now());
        }
        if (jr.getShortageG() > acc.getMaxShortageG()) {
            acc.setMaxShortageG(jr.getShortageG());
        }
        if (acc.getShortageCount() >= acc.getFocusThreshold()) {
            acc.setFocusFlag(true);
        }
        accumMapper.update(acc);
    }
}
