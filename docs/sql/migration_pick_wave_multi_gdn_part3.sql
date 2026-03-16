-- ============================================================
-- Migration: Pick Wave Multi-GDN Support
-- Part 3: Alter pick_task table
-- Run against your WMS database (e.g. warehouse_management)
-- ============================================================

-- Make gdn_id nullable (tasks belong to wave, not directly to GDN)
ALTER TABLE pick_task
    MODIFY COLUMN gdn_id BIGINT unsigned NULL;

-- Note: gdn_id is still used for backward compatibility
-- New tasks will have gdn_id NULL and reference GDN via pick_wave_gdn
