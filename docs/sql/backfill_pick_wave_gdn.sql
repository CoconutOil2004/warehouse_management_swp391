-- ============================================================
-- Backfill Data: Pick Wave Multi-GDN Support
-- Part 1: Backfill pick_wave_gdn from existing pick_wave.gdn_id
-- Run against your WMS database (e.g. warehouse_management)
-- ============================================================

-- IMPORTANT: Run this AFTER migration_pick_wave_multi_gdn_part2.sql (create pick_wave_gdn table)
-- IMPORTANT: Run this BEFORE migration_pick_wave_multi_gdn_part4.sql (update wave_code)

-- Backfill pick_wave_gdn from existing pick_wave.gdn_id
-- Only insert where gdn_id is NOT NULL (existing waves)
INSERT INTO pick_wave_gdn (wave_id, gdn_id, created_at)
SELECT wave_id, gdn_id, created_at
FROM pick_wave
WHERE gdn_id IS NOT NULL
ON DUPLICATE KEY UPDATE created_at = VALUES(created_at);

-- Verify the backfill
SELECT
    'Backfill complete' AS status,
    COUNT(*) AS total_waves,
    COUNT(DISTINCT wave_id) AS waves_with_gdn
FROM pick_wave
WHERE gdn_id IS NOT NULL;

-- Show sample data
SELECT
    pw.wave_id,
    pw.wave_code,
    pwg.gdn_id,
    gdn.gdn_number,
    pwg.created_at
FROM pick_wave_gdn pwg
JOIN pick_wave pw ON pwg.wave_id = pw.wave_id
JOIN goods_delivery_note gdn ON pwg.gdn_id = gdn.gdn_id
ORDER BY pw.wave_id DESC
LIMIT 10;
