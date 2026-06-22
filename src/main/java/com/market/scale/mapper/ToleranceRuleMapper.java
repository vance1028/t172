package com.market.scale.mapper;

import com.market.scale.entity.ToleranceRule;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ToleranceRuleMapper {

    @Select("SELECT * FROM tolerance_rules WHERE id = #{id}")
    ToleranceRule findById(Long id);

    @Select("<script>" +
            "SELECT * FROM tolerance_rules " +
            "<where>" +
            "  <if test='category != null and category != \"\"'>AND category = #{category}</if>" +
            "  <if test='weighingType != null and weighingType != \"\"'>AND weighing_type = #{weighingType}</if>" +
            "  <if test='enabled != null'>AND enabled = #{enabled}</if>" +
            "</where>" +
            " ORDER BY min_weight_g ASC" +
            "</script>")
    List<ToleranceRule> search(@Param("category") String category,
                            @Param("weighingType") String weighingType,
                            @Param("enabled") Boolean enabled);

    @Select("SELECT * FROM tolerance_rules " +
            "WHERE category = #{category} " +
            "  AND weighing_type = #{weighingType} " +
            "  AND enabled = 1 " +
            "  AND min_weight_g <= #{weight} " +
            "  AND (max_weight_g IS NULL OR max_weight_g > #{weight}) " +
            "ORDER BY min_weight_g DESC " +
            "LIMIT 1")
    ToleranceRule matchRule(@Param("category") String category,
                          @Param("weighingType") String weighingType,
                          @Param("weight") int weight);

    @Insert("<script>" +
            "INSERT INTO tolerance_rules(category, weighing_type, min_weight_g, max_weight_g, tolerance_mode, tolerance_percent, tolerance_fixed_g, severe_multiplier, description, enabled, version) " +
            "VALUES(#{category}, #{weighingType}, #{minWeightG}, " +
            "<choose><when test='maxWeightG != null'>#{maxWeightG}</when><otherwise>NULL</otherwise></choose>, " +
            "#{toleranceMode}, " +
            "<choose><when test='tolerancePercent != null'>#{tolerancePercent}</when><otherwise>NULL</otherwise></choose>, " +
            "<choose><when test='toleranceFixedG != null'>#{toleranceFixedG}</when><otherwise>NULL</otherwise></choose>, " +
            "#{severeMultiplier}, #{description}, #{enabled}, 0)" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ToleranceRule rule);

    @Update("<script>" +
            "UPDATE tolerance_rules SET category=#{category}, weighing_type=#{weighingType}, " +
            "min_weight_g=#{minWeightG}, " +
            "max_weight_g=<choose><when test='maxWeightG != null'>#{maxWeightG}</when><otherwise>NULL</otherwise></choose>, " +
            "tolerance_mode=#{toleranceMode}, " +
            "tolerance_percent=<choose><when test='tolerancePercent != null'>#{tolerancePercent}</when><otherwise>NULL</otherwise></choose>, " +
            "tolerance_fixed_g=<choose><when test='toleranceFixedG != null'>#{toleranceFixedG}</when><otherwise>NULL</otherwise></choose>, " +
            "severe_multiplier=#{severeMultiplier}, description=#{description}, enabled=#{enabled}, version=version+1 " +
            "WHERE id=#{id} AND version=#{version}" +
            "</script>")
    int update(ToleranceRule rule);

    @Update("UPDATE tolerance_rules SET enabled = #{enabled} WHERE id = #{id}")
    int updateEnabled(@Param("id") Long id, @Param("enabled") Boolean enabled);

    @Delete("DELETE FROM tolerance_rules WHERE id = #{id}")
    int delete(Long id);
}
