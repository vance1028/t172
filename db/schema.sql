-- 农贸市场计量公平秤监管平台 数据库结构
-- 字符集统一 utf8mb4，避免中文乱码

CREATE DATABASE IF NOT EXISTS fairscale
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE fairscale;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    username      VARCHAR(64)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    display_name  VARCHAR(64)           DEFAULT NULL,
    role          VARCHAR(20)  NOT NULL DEFAULT 'viewer',
    enabled       TINYINT(1)   NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 摊位表
CREATE TABLE IF NOT EXISTS stalls (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    stall_no      VARCHAR(32)  NOT NULL,
    market_name   VARCHAR(128) NOT NULL,
    merchant_name VARCHAR(64)  NOT NULL,
    category      VARCHAR(32)           DEFAULT NULL,
    contact_phone VARCHAR(32)           DEFAULT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'active',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stalls_no (stall_no),
    KEY idx_stalls_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 衡器表
CREATE TABLE IF NOT EXISTS scales (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    stall_id          BIGINT      NOT NULL,
    asset_no          VARCHAR(48) NOT NULL,
    model             VARCHAR(64)          DEFAULT NULL,
    manufacturer      VARCHAR(128)         DEFAULT NULL,
    max_capacity_g    INT                  DEFAULT NULL,
    verified_at       DATE                 DEFAULT NULL,
    verify_cycle_days INT         NOT NULL DEFAULT 365,
    status            VARCHAR(20) NOT NULL DEFAULT 'in_use',
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_scales_asset (asset_no),
    KEY idx_scales_stall (stall_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 允差规则表 ====================
-- 按经营品类+称量方式+重量区间分级设定允差
CREATE TABLE IF NOT EXISTS tolerance_rules (
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    category          VARCHAR(32) NOT NULL COMMENT '经营品类：vegetable/aquatic/meat/cooked/drygoods/other',
    weighing_type     VARCHAR(16) NOT NULL COMMENT '称量方式：loose(散称)/prepackaged(定量包装)',
    min_weight_g     INT       NOT NULL DEFAULT 0 COMMENT '适用最小重量（克），含边界',
    max_weight_g     INT       NOT NULL COMMENT '适用最大重量（克），不含边界；NULL表示无上限',
    tolerance_mode    VARCHAR(16) NOT NULL COMMENT '允差模式：percent(百分比)/fixed(固定克数)/max_both(取两者较大值)',
    tolerance_percent DECIMAL(5,2)          DEFAULT NULL COMMENT '百分比允差，如2.5表示2.5%',
    tolerance_fixed_g INT                  DEFAULT NULL COMMENT '固定克数允差（克）',
    severe_multiplier  DECIMAL(4,1) NOT NULL DEFAULT 3.0 COMMENT '严重短缺倍率，短缺量超过允差×此倍率即判严重短缺',
    description     VARCHAR(255)         DEFAULT NULL COMMENT '规则说明',
    enabled         TINYINT(1)   NOT NULL DEFAULT 1,
    version         INT         NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_tolerance_category (category, weighing_type, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 抽检批次表 ====================
CREATE TABLE IF NOT EXISTS inspection_batches (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    batch_no          VARCHAR(32)  NOT NULL COMMENT '批次编号，如 INS-20250622-001',
    market_name       VARCHAR(128) NOT NULL COMMENT '抽检市场',
    category          VARCHAR(32)           DEFAULT NULL COMMENT '抽检品类，NULL表示全品类',
    inspector         VARCHAR(64)           DEFAULT NULL COMMENT '抽检员',
    status            VARCHAR(16)  NOT NULL DEFAULT 'in_progress' COMMENT 'in_progress/completed',
    total_count       INT         NOT NULL DEFAULT 0,
    pass_count        INT         NOT NULL DEFAULT 0,
    shortage_count    INT         NOT NULL DEFAULT 0,
    severe_count      INT         NOT NULL DEFAULT 0,
    pass_rate         DECIMAL(5,2)          DEFAULT NULL,
    remark            VARCHAR(255)         DEFAULT NULL,
    started_at        DATETIME              DEFAULT NULL,
    completed_at      DATETIME              DEFAULT NULL,
    created_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_batch_no (batch_no),
    KEY idx_batch_market (market_name, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 复称记录表（增强版，含允差快照） ====================
CREATE TABLE IF NOT EXISTS recheck_records (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    stall_id                BIGINT       NOT NULL,
    batch_id                BIGINT                DEFAULT NULL COMMENT '所属抽检批次',
    commodity               VARCHAR(64)  NOT NULL,
    category                VARCHAR(32)           DEFAULT NULL COMMENT '经营品类（冗余）',
    weighing_type           VARCHAR(16)           DEFAULT NULL COMMENT '称量方式（冗余）',
    claimed_weight_g        INT         NOT NULL,
    actual_weight_g         INT         NOT NULL,
    shortage_g             INT         NOT NULL DEFAULT 0,
    tolerance_rule_id       BIGINT                DEFAULT NULL COMMENT '使用的允差规则ID',
    tolerance_rule_version   VARCHAR(512)         DEFAULT NULL COMMENT '允差规则快照JSON，历史隔离',
    tolerance_value_g      INT                  DEFAULT NULL COMMENT '实际判定使用的允差值（克）',
    result                  VARCHAR(16)  NOT NULL DEFAULT 'pass' COMMENT 'pass/shortage/severe_shortage',
    judgment_basis        VARCHAR(512)         DEFAULT NULL COMMENT '判定依据描述',
    handled_by              VARCHAR(64)           DEFAULT NULL,
    remark                  VARCHAR(255)         DEFAULT NULL,
    rechecked_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_recheck_stall (stall_id),
    KEY idx_recheck_result (result),
    KEY idx_recheck_batch (batch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================== 摊位违规累计表 ====================
CREATE TABLE IF NOT EXISTS stall_violation_accum (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    stall_id                BIGINT       NOT NULL,
    total_recheck_count       INT         NOT NULL DEFAULT 0 COMMENT '累计复称次数',
    shortage_count          INT         NOT NULL DEFAULT 0 COMMENT '累计短缺次数',
    severe_count            INT         NOT NULL DEFAULT 0 COMMENT '累计严重短缺次数',
    max_shortage_g        INT         NOT NULL DEFAULT 0 COMMENT '历史最大短缺量（克）',
    focus_flag            TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否重点关注：0否1是',
    focus_threshold       INT         NOT NULL DEFAULT 3 COMMENT '进入重点关注的短缺次数阈值',
    last_shortage_at     DATETIME              DEFAULT NULL,
    created_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_stall_id (stall_id),
    KEY idx_focus_flag (focus_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
