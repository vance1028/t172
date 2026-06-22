package com.market.scale.config;

import com.market.scale.entity.Scale;
import com.market.scale.entity.Stall;
import com.market.scale.entity.ToleranceRule;
import com.market.scale.entity.User;
import com.market.scale.mapper.ScaleMapper;
import com.market.scale.mapper.StallMapper;
import com.market.scale.mapper.ToleranceRuleMapper;
import com.market.scale.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserMapper userMapper;
    private final StallMapper stallMapper;
    private final ScaleMapper scaleMapper;
    private final ToleranceRuleMapper ruleMapper;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserMapper userMapper, StallMapper stallMapper,
                      ScaleMapper scaleMapper, ToleranceRuleMapper ruleMapper,
                      PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.stallMapper = stallMapper;
        this.scaleMapper = scaleMapper;
        this.ruleMapper = ruleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedUsers();
        seedBusinessData();
        seedToleranceRules();
    }

    private void seedUsers() {
        if (userMapper.findByUsername("admin") == null) {
            createUser("admin", "admin123", "系统管理员", "admin");
        }
        if (userMapper.findByUsername("inspector") == null) {
            createUser("inspector", "inspect123", "计量巡查员", "inspector");
        }
        if (userMapper.findByUsername("viewer") == null) {
            createUser("viewer", "viewer123", "只读查看员", "viewer");
        }
    }

    private void createUser(String username, String rawPwd, String displayName, String role) {
        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(passwordEncoder.encode(rawPwd));
        u.setDisplayName(displayName);
        u.setRole(role);
        u.setEnabled(true);
        userMapper.insert(u);
    }

    private void seedBusinessData() {
        if (stallMapper.count(null, null) > 0) {
            return;
        }
        Stall s1 = newStall("A-12", "城东综合农贸市场", "王桂芳", "vegetable", "13800010001");
        Stall s2 = newStall("B-07", "城东综合农贸市场", "李建国", "aquatic", "13800010002");
        Stall s3 = newStall("C-21", "滨江生鲜市场", "赵丽娟", "meat", "13800010003");
        Stall s4 = newStall("D-03", "城东综合农贸市场", "钱德胜", "cooked", "13800010004");
        Stall s5 = newStall("E-18", "滨江生鲜市场", "孙丽华", "drygoods", "13800010005");
        stallMapper.insert(s1);
        stallMapper.insert(s2);
        stallMapper.insert(s3);
        stallMapper.insert(s4);
        stallMapper.insert(s5);

        scaleMapper.insert(newScale(s1.getId(), "JL-2025-0012", "ACS-30", "上海大华衡器", 30000, LocalDate.now().minusDays(40), 365));
        scaleMapper.insert(newScale(s2.getId(), "JL-2025-0033", "TCS-15", "永康精工衡器", 15000, LocalDate.now().minusDays(400), 365));
        scaleMapper.insert(newScale(s4.getId(), "JL-2025-0058", "ACS-15", "上海大华衡器", 15000, LocalDate.now().minusDays(120), 365));
    }

    private void seedToleranceRules() {
        if (ruleMapper.search(null, null, true).size() > 0) {
            return;
        }
        BigDecimal severe = BigDecimal.valueOf(3.0);

        insertRule("vegetable", "loose", 0, 500, "max_both", new BigDecimal("3.0"), 15, severe, "蔬菜散称 小份含失水允差");
        insertRule("vegetable", "loose", 500, 2000, "percent", new BigDecimal("2.5"), null, severe, "蔬菜散称 中份");
        insertRule("vegetable", "loose", 2000, null, "percent", new BigDecimal("2.0"), null, severe, "蔬菜散称 大份无上限");
        insertRule("vegetable", "prepackaged", 0, null, "percent", new BigDecimal("1.5"), null, severe, "蔬菜定量包装");

        insertRule("aquatic", "loose", 0, 500, "max_both", new BigDecimal("4.0"), 20, severe, "水产散称 小份含沥水允差");
        insertRule("aquatic", "loose", 500, 5000, "percent", new BigDecimal("3.0"), null, severe, "水产散称 中份");
        insertRule("aquatic", "loose", 5000, null, "percent", new BigDecimal("2.0"), null, severe, "水产散称 大份无上限");
        insertRule("aquatic", "prepackaged", 0, null, "percent", new BigDecimal("1.5"), null, severe, "水产定量包装");

        insertRule("meat", "loose", 0, 500, "max_both", new BigDecimal("2.0"), 10, severe, "肉类散称 小份");
        insertRule("meat", "loose", 500, null, "percent", new BigDecimal("1.5"), null, severe, "肉类散称 中份及以上无上限");
        insertRule("meat", "prepackaged", 0, null, "percent", new BigDecimal("1.0"), null, severe, "肉类定量包装");

        insertRule("cooked", "loose", 0, null, "max_both", new BigDecimal("1.0"), 5, severe, "熟食散称 全量无上限");
        insertRule("cooked", "prepackaged", 0, null, "percent", new BigDecimal("0.5"), null, severe, "熟食定量包装");

        insertRule("drygoods", "loose", 0, null, "percent", new BigDecimal("0.5"), null, severe, "干货散称 全量无上限");
        insertRule("drygoods", "prepackaged", 0, null, "percent", new BigDecimal("0.2"), null, severe, "干货定量包装");

        insertRule("other", "loose", 0, null, "max_both", new BigDecimal("2.0"), 10, severe, "兜底品类 散称无上限");
        insertRule("other", "prepackaged", 0, null, "percent", new BigDecimal("1.0"), null, severe, "兜底品类 定量包装");
    }

    private void insertRule(String cat, String wt, int minG, Integer maxG,
                            String mode, BigDecimal percent, Integer fixedG,
                            BigDecimal severe, String desc) {
        ToleranceRule r = new ToleranceRule();
        r.setCategory(cat);
        r.setWeighingType(wt);
        r.setMinWeightG(minG);
        r.setMaxWeightG(maxG);
        r.setToleranceMode(mode);
        r.setTolerancePercent(percent);
        r.setToleranceFixedG(fixedG);
        r.setSevereMultiplier(severe);
        r.setDescription(desc);
        r.setEnabled(true);
        ruleMapper.insert(r);
    }

    private Stall newStall(String no, String market, String merchant, String category, String phone) {
        Stall s = new Stall();
        s.setStallNo(no);
        s.setMarketName(market);
        s.setMerchantName(merchant);
        s.setCategory(category);
        s.setContactPhone(phone);
        s.setStatus("active");
        return s;
    }

    private Scale newScale(Long stallId, String assetNo, String model, String maker, int cap, LocalDate verified, int cycle) {
        Scale sc = new Scale();
        sc.setStallId(stallId);
        sc.setAssetNo(assetNo);
        sc.setModel(model);
        sc.setManufacturer(maker);
        sc.setMaxCapacityG(cap);
        sc.setVerifiedAt(verified);
        sc.setVerifyCycleDays(cycle);
        sc.setStatus("in_use");
        return sc;
    }
}
