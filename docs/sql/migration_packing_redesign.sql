-- ============================================================
-- Migration: Redesign Packing Workflow
-- 1. Drops old packing and packing_package tables
-- 2. Creates new packing_session, packing_line_config, packing_task tables
-- ============================================================

-- 1. Drop old tables (if they exist)
DROP TABLE IF EXISTS packing_package;
DROP TABLE IF EXISTS packing;

-- 2. Create new tables

-- packing_session: 1:1 with GDN, tracks overall packing status
CREATE TABLE packing_session (
    packing_session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gdn_id             BIGINT NOT NULL UNIQUE,
    status             VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING | IN_PROGRESS | DONE
    created_by         BIGINT NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at       DATETIME NULL,
    CONSTRAINT fk_packing_session_gdn FOREIGN KEY (gdn_id) REFERENCES goods_delivery_note(gdn_id),
    CONSTRAINT fk_packing_session_user FOREIGN KEY (created_by) REFERENCES `user`(user_id),
    INDEX idx_packing_session_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- packing_line_config: configuration for packing each GDN line
CREATE TABLE packing_line_config (
    packing_line_config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_session_id     BIGINT NOT NULL,
    gdn_line_id            BIGINT NOT NULL,
    items_per_pack         INT NOT NULL,
    num_packs              INT NOT NULL,
    CONSTRAINT fk_packing_config_session FOREIGN KEY (packing_session_id) REFERENCES packing_session(packing_session_id),
    CONSTRAINT fk_packing_config_line FOREIGN KEY (gdn_line_id) REFERENCES goods_delivery_line(gdn_line_id),
    INDEX idx_packing_config_session (packing_session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- packing_task: assigns staff to pack a specific number of packs for a line config
CREATE TABLE packing_task (
    packing_task_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_line_config_id BIGINT NOT NULL,
    assigned_to            BIGINT NOT NULL,
    assigned_packs         INT NOT NULL,
    packed_packs           INT NOT NULL DEFAULT 0,
    status                 VARCHAR(50) NOT NULL DEFAULT 'PENDING',  -- PENDING | IN_PROGRESS | DONE
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME NULL,
    CONSTRAINT fk_packing_task_config FOREIGN KEY (packing_line_config_id) REFERENCES packing_line_config(packing_line_config_id),
    CONSTRAINT fk_packing_task_user FOREIGN KEY (assigned_to) REFERENCES `user`(user_id),
    INDEX idx_packing_task_config (packing_line_config_id),
    INDEX idx_packing_task_user (assigned_to),
    INDEX idx_packing_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
