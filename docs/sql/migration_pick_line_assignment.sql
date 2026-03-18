-- ============================================================
-- Migration: refactor outbound picking - add line-level assignment
-- Run this against your WMS database.
-- 
-- WARNING: Backup your database before running this migration!
-- ============================================================

-- ============================================================
-- STEP 1: Add new columns to pick_task_line
-- ============================================================

-- 1.1 Add variant_id column
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS variant_id BIGINT UNSIGNED NULL AFTER gdn_line_id;

-- 1.2 Add foreign key for variant_id
ALTER TABLE pick_task_line
    ADD CONSTRAINT fk_pick_task_line_variant
        FOREIGN KEY (variant_id) REFERENCES product_variant(variant_id);

-- 1.3 Add qty_required column
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS qty_required DECIMAL(18,4) NULL AFTER from_slot_id;

-- 1.4 Add assigned_to column (for line-level assignment)
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS assigned_to BIGINT UNSIGNED NULL;

-- 1.5 Add foreign key for assigned_to
ALTER TABLE pick_task_line
    ADD CONSTRAINT fk_pick_line_assigned_to_new
        FOREIGN KEY (assigned_to) REFERENCES `user`(user_id);

-- 1.6 Add assigned_by column
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS assigned_by BIGINT UNSIGNED NULL;

-- 1.7 Add foreign key for assigned_by
ALTER TABLE pick_task_line
    ADD CONSTRAINT fk_pick_line_assigned_by_new
        FOREIGN KEY (assigned_by) REFERENCES `user`(user_id);

-- 1.8 Add assigned_at column
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS assigned_at DATETIME NULL;

-- 1.9 Add completed_at column
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL;

-- ============================================================
-- STEP 2: Update CHECK constraints for status columns
-- ============================================================

-- 2.1 Update pick_wave.status - add new statuses
-- First drop existing constraint (MySQL requires this)
ALTER TABLE pick_wave
    DROP CONSTRAINT IF EXISTS chk_pick_wave_status;

-- Then add new constraint with additional statuses
ALTER TABLE pick_wave
    ADD CONSTRAINT chk_pick_wave_status 
    CHECK (status IN ('CREATED','RELEASED','IN_PROGRESS','DONE','CANCELLED'));

-- 2.2 Update pick_task.status - add new statuses
ALTER TABLE pick_task
    DROP CONSTRAINT IF EXISTS chk_pick_task_status;

ALTER TABLE pick_task
    ADD CONSTRAINT chk_pick_task_status 
    CHECK (status IN ('CREATED','PENDING','ASSIGNED','IN_PROGRESS','COMPLETED','CANCELLED'));

-- 2.3 Update pick_task_line.pick_status - add new statuses
ALTER TABLE pick_task_line
    DROP CONSTRAINT IF EXISTS chk_pick_line_status;

ALTER TABLE pick_task_line
    ADD CONSTRAINT chk_pick_line_status 
    CHECK (pick_status IN ('PENDING','PICKED','COMPLETED','CANCELLED','DONE'));

-- ============================================================
-- STEP 3: Migrate existing data (if applicable)
-- ============================================================

-- 3.1 Migrate variant_id from goods_delivery_line if null
UPDATE pick_task_line ptl
SET ptl.variant_id = (
    SELECT gdl.variant_id 
    FROM goods_delivery_line gdl 
    WHERE gdl.gdn_line_id = ptl.gdn_line_id
    LIMIT 1
)
WHERE ptl.variant_id IS NULL;

-- 3.2 Migrate qty_required from goods_delivery_line if null
UPDATE pick_task_line ptl
SET ptl.qty_required = (
    SELECT gdl.qty_required 
    FROM goods_delivery_line gdl 
    WHERE gdl.gdn_line_id = ptl.gdn_line_id
    LIMIT 1
)
WHERE ptl.qty_required IS NULL;

-- ============================================================
-- STEP 4: Create indexes for better query performance
-- ============================================================

-- 4.1 Index for assigned_to lookups
CREATE INDEX IF NOT EXISTS idx_pick_line_assigned_to_new 
    ON pick_task_line(assigned_to);

-- 4.2 Index for variant_id lookups
CREATE INDEX IF NOT EXISTS idx_pick_task_line_variant_new 
    ON pick_task_line(variant_id);

-- ============================================================
-- Verification queries (run these to check migration)
-- ============================================================

-- Check pick_wave status values
-- SELECT DISTINCT status FROM pick_wave;

-- Check pick_task status values  
-- SELECT DISTINCT status FROM pick_task;

-- Check pick_task_line pick_status values
-- SELECT DISTINCT pick_status FROM pick_task_line;

-- Check new columns exist
-- DESCRIBE pick_task_line;
