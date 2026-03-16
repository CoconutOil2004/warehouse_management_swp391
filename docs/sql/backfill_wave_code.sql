-- ============================================================
-- Backfill Data: Pick Wave Multi-GDN Support
-- Part 2: Update wave_code for existing waves
-- Run against your WMS database (e.g. warehouse_management)
-- ============================================================

-- IMPORTANT: Run this AFTER migration_pick_wave_multi_gdn_part1.sql (add wave_code column)

-- Update wave_code for existing waves (format: WAVE-000001, WAVE-000002, ...)
UPDATE pick_wave
SET wave_code = CONCAT('WAVE-', LPAD(wave_id, 6, '0'))
WHERE wave_code IS NULL;

-- Verify the update
SELECT
    'Wave code backfill complete' AS status,
    COUNT(*) AS total_waves,
    COUNT(wave_code) AS waves_with_code
FROM pick_wave;

-- Show sample data
SELECT
    wave_id,
    wave_code,
    gdn_id,
    status,
    created_at
FROM pick_wave
ORDER BY wave_id DESC
LIMIT 10;
