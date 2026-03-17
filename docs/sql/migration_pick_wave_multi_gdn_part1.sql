-- ============================================================
-- Migration: Pick Wave Multi-GDN Support
-- Part 1: Alter pick_wave table
-- Run against your WMS database (e.g. warehouse_management)
-- ============================================================

-- 1. Add wave_code column for unique wave identification
ALTER TABLE pick_wave
    ADD COLUMN wave_code VARCHAR(50) NULL AFTER wave_id;

-- 2. Make gdn_id nullable (to support N:M relationship via pick_wave_gdn table)
ALTER TABLE pick_wave
    MODIFY COLUMN gdn_id BIGINT NULL;

-- 3. Add unique index on wave_code
ALTER TABLE pick_wave
    ADD UNIQUE INDEX idx_pick_wave_code (wave_code);

-- Note: Backfill wave_code will be done in a separate script
-- Note: pick_wave_gdn table creation is in migration_pick_wave_multi_gdn_part2.sql
