package com.market.scale.mapper;

import com.market.scale.entity.InspectionBatch;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface InspectionBatchMapper {

    @Select("SELECT * FROM inspection_batches WHERE id = #{id}")
    InspectionBatch findById(Long id);

    @Select("SELECT * FROM inspection_batches WHERE batch_no = #{batchNo}")
    InspectionBatch findByBatchNo(String batchNo);

    @Select("<script>" +
            "SELECT * FROM inspection_batches " +
            "<where>" +
            "  <if test='marketName != null and marketName != \"\"'>AND market_name LIKE CONCAT('%', #{marketName}, '%')</if>" +
            "  <if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "</where>" +
            " ORDER BY created_at DESC LIMIT #{offset}, #{limit}" +
            "</script>")
    List<InspectionBatch> search(@Param("marketName") String marketName,
                               @Param("status") String status,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    @Select("<script>" +
            "SELECT COUNT(*) FROM inspection_batches " +
            "<where>" +
            "  <if test='marketName != null and marketName != \"\"'>AND market_name LIKE CONCAT('%', #{marketName}, '%')</if>" +
            "  <if test='status != null and status != \"\"'>AND status = #{status}</if>" +
            "</where>" +
            "</script>")
    long count(@Param("marketName") String marketName, @Param("status") String status);

    @Insert("INSERT INTO inspection_batches(batch_no, market_name, category, inspector, status, remark, started_at) " +
            "VALUES(#{batchNo}, #{marketName}, #{category}, #{inspector}, #{status}, #{remark}, #{startedAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(InspectionBatch batch);

    @Update("UPDATE inspection_batches SET market_name=#{marketName}, category=#{category}, inspector=#{inspector}, status=#{status}, total_count=#{totalCount}, pass_count=#{passCount}, shortage_count=#{shortageCount}, severe_count=#{severeCount}, pass_rate=#{passRate}, remark=#{remark}, completed_at=#{completedAt} WHERE id=#{id}")
    int update(InspectionBatch batch);
}
