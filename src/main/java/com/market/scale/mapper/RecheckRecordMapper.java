package com.market.scale.mapper;

import com.market.scale.entity.RecheckRecord;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface RecheckRecordMapper {

    @Select("SELECT * FROM recheck_records WHERE id = #{id}")
    RecheckRecord findById(Long id);

    @Select("<script>" +
            "SELECT * FROM recheck_records " +
            "<where>" +
            "  <if test='stallId != null'>AND stall_id = #{stallId}</if>" +
            "  <if test='batchId != null'>AND batch_id = #{batchId}</if>" +
            "  <if test='result != null and result != \"\"'>AND result = #{result}</if>" +
            "</where>" +
            " ORDER BY rechecked_at DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<RecheckRecord> search(@Param("stallId") Long stallId,
                             @Param("batchId") Long batchId,
                             @Param("result") String result,
                             @Param("offset") int offset,
                             @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM recheck_records " +
            "<where>" +
            "  <if test='stallId != null'>AND stall_id = #{stallId}</if>" +
            "  <if test='batchId != null'>AND batch_id = #{batchId}</if>" +
            "  <if test='result != null and result != \"\"'>AND result = #{result}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("stallId") Long stallId,
                @Param("batchId") Long batchId,
                @Param("result") String result);

    @Select("SELECT stall_id AS stallId, " +
            "  COUNT(*) AS shortageCount, " +
            "  MAX(shortage_g) AS maxShortageG " +
            "FROM recheck_records " +
            "WHERE batch_id = #{batchId} " +
            "  AND result IN ('shortage', 'severe_shortage') " +
            "GROUP BY stall_id")
    List<Map<String, Object>> summaryShortageByBatch(Long batchId);

    @Select("SELECT result, COUNT(*) AS cnt FROM recheck_records WHERE batch_id = #{batchId} GROUP BY result")
    List<Map<String, Object>> countByResultAndBatch(Long batchId);

    @Insert("INSERT INTO recheck_records(stall_id, batch_id, commodity, category, weighing_type, claimed_weight_g, actual_weight_g, shortage_g, tolerance_rule_id, tolerance_rule_version, tolerance_value_g, result, judgment_basis, handled_by, remark, rechecked_at, created_at) " +
            "VALUES(#{stallId}, #{batchId}, #{commodity}, #{category}, #{weighingType}, #{claimedWeightG}, #{actualWeightG}, #{shortageG}, #{toleranceRuleId}, #{toleranceRuleVersion}, #{toleranceValueG}, #{result}, #{judgmentBasis}, #{handledBy}, #{remark}, #{recheckedAt}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RecheckRecord record);
}
