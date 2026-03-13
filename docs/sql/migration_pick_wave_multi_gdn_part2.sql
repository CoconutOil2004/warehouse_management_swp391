-- ============================================================
-- Migration: Pick Wave Multi-GDN Support
-- Part 2: Create pick_wave_gdn junction table
-- Run against your WMS database (e.g. warehouse_management)
-- ============================================================

-- Create pick_wave_gdn table for N:M relationship
CREATE TABLE IF NOT EXISTS pick_wave_gdn (
    wave_id BIGINT unsigned NOT NULL,
    gdn_id BIGINT unsigned NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (wave_id, gdn_id),
    CONSTRAINT fk_wave_gdn_wave FOREIGN KEY (wave_id) REFERENCES pick_wave(wave_id) ON DELETE CASCADE,
    CONSTRAINT fk_wave_gdn_gdn FOREIGN KEY (gdn_id) REFERENCES goods_delivery_note(gdn_id) ON DELETE CASCADE,
    INDEX idx_gdn_wave (gdn_id, wave_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Add index for faster lookups
CREATE INDEX idx_pick_wave_gdn_wave ON pick_wave_gdn(wave_id);
CREATE INDEX idx_pick_wave_gdn_gdn ON pick_wave_gdn(gdn_id);
