-- ============================================================
-- Safe add: packing_session, packing_line_config, packing_task
-- Use when you see: Table 'wms_db.packing_session' doesn't exist
-- Does NOT drop legacy packing / packing_package tables.
-- Requires: goods_delivery_note, goods_delivery_line, user
-- ============================================================

CREATE TABLE IF NOT EXISTS packing_session (
    packing_session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    gdn_id             BIGINT NOT NULL UNIQUE,
    status             VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_by         BIGINT NULL,
    created_at         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at       DATETIME NULL,
    CONSTRAINT fk_packing_session_gdn FOREIGN KEY (gdn_id) REFERENCES goods_delivery_note(gdn_id),
    CONSTRAINT fk_packing_session_user FOREIGN KEY (created_by) REFERENCES `user`(user_id),
    INDEX idx_packing_session_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS packing_line_config (
    packing_line_config_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_session_id     BIGINT NOT NULL,
    gdn_line_id            BIGINT NOT NULL,
    items_per_pack         INT NOT NULL,
    num_packs              INT NOT NULL,
    CONSTRAINT fk_packing_config_session FOREIGN KEY (packing_session_id) REFERENCES packing_session(packing_session_id),
    CONSTRAINT fk_packing_config_line FOREIGN KEY (gdn_line_id) REFERENCES goods_delivery_line(gdn_line_id),
    INDEX idx_packing_config_session (packing_session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS packing_task (
    packing_task_id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    packing_line_config_id BIGINT NOT NULL,
    assigned_to            BIGINT NOT NULL,
    assigned_packs         INT NOT NULL,
    packed_packs           INT NOT NULL DEFAULT 0,
    status                 VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    created_at             DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             DATETIME NULL,
    CONSTRAINT fk_packing_task_config FOREIGN KEY (packing_line_config_id) REFERENCES packing_line_config(packing_line_config_id),
    CONSTRAINT fk_packing_task_user FOREIGN KEY (assigned_to) REFERENCES `user`(user_id),
    INDEX idx_packing_task_config (packing_line_config_id),
    INDEX idx_packing_task_user (assigned_to),
    INDEX idx_packing_task_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
