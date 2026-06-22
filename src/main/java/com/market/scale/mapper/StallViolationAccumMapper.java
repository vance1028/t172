package com.market.scale.mapper;

import com.market.scale.entity.StallViolationAccum;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StallViolationAccumMapper {

    @Select("SELECT * FROM stall_violation_accum WHERE id = #{id}")
    StallViolationAccum findById(Long id);

    @Select("SELECT * FROM stall_violation_accum WHERE stall_id = #{stallId}")
    StallViolationAccum findByStallId(Long stallId);

    @Select("<script>" +
            "SELECT * FROM stall_violation_accum " +
            "<where>" +
            "  <if test='focusFlag != null'>AND focus_flag = #{focusFlag}</if>" +
            "</where>" +
            " ORDER BY shortage_count DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<StallViolationAccum> search(@Param("focusFlag") Boolean focusFlag,
                                    @Param("offset") int offset,
                                    @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM stall_violation_accum " +
            "<where>" +
            "  <if test='focusFlag != null'>AND focus_flag = #{focusFlag}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("focusFlag") Boolean focusFlag);

    @Insert("INSERT INTO stall_violation_accum(stall_id, total_recheck_count, shortage_count, severe_count, max_shortage_g, focus_flag, focus_threshold) " +
            "VALUES(#{stallId}, #{totalRecheckCount}, #{shortageCount}, #{severeCount}, #{maxShortageG}, #{focusFlag}, #{focusThreshold})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StallViolationAccum accum);

    @Update("UPDATE stall_violation_accum SET total_recheck_count=#{totalRecheckCount}, shortage_count=#{shortageCount}, severe_count=#{severeCount}, max_shortage_g=#{maxShortageG}, focus_flag=#{focusFlag}, focus_threshold=#{focusThreshold}, last_shortage_at=#{lastShortageAt} WHERE id=#{id}")
    int update(StallViolationAccum accum);
}
