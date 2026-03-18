-- ============================================================
-- Migration: Add ALL missing columns for pick_task
-- Run this against your WMS database.
-- 
-- This fixes: Unknown column 'assigned_to' in 'field list'
-- ============================================================

-- ============================================================
-- pick_task table - add assignment columns
-- ============================================================

-- Add assigned_to
ALTER TABLE pick_task
    ADD COLUMN IF NOT EXISTS assigned_to BIGINT UNSIGNED NULL;

-- Add assigned_by  
ALTER TABLE pick_task
    ADD COLUMN IF NOT EXISTS assigned_by BIGINT UNSIGNED NULL;

-- Add assigned_at
ALTER TABLE pick_task
    ADD COLUMN IF NOT EXISTS assigned_at DATETIME NULL;

-- Add foreign keys for assigned_to
ALTER TABLE pick_task
    ADD CONSTRAINT IF NOT EXISTS fk_pick_task_assigned_to
    FOREIGN KEY (assigned_to) REFERENCES `user`(user_id);

-- Add foreign key for assigned_by
ALTER TABLE pick_task
    ADD CONSTRAINT IF NOT EXISTS fk_pick_task_assigned_by
    FOREIGN KEY (assigned_by) REFERENCES `user`(user_id);

-- Add indexes
CREATE INDEX IF NOT EXISTS idx_pick_task_assigned_to ON pick_task(assigned_to);

-- ============================================================
-- Verify with:
-- DESCRIBE pick_task;
-- ============================================================
