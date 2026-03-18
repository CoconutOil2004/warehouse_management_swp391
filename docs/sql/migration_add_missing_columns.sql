-- ============================================================
-- Migration: Add missing columns for pick_task and pick_task_line
-- Run this against your WMS database.
-- 
-- This fixes: Unknown column errors after schema update
-- ============================================================

-- ============================================================
-- pick_task table - add missing columns
-- ============================================================

-- Add started_at if not exists
ALTER TABLE pick_task
    ADD COLUMN IF NOT EXISTS started_at DATETIME NULL AFTER status;

-- Add completed_at if not exists  
ALTER TABLE pick_task
    ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL AFTER started_at;

-- ============================================================
-- pick_task_line table - add missing columns
-- ============================================================

-- Add variant_id
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS variant_id BIGINT UNSIGNED NULL AFTER gdn_line_id;

-- Add qty_required
ALTER TABLE pick_task_line  
    ADD COLUMN IF NOT EXISTS qty_required DECIMAL(18,4) NULL AFTER from_slot_id;

-- Add assigned_to
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS assigned_to BIGINT UNSIGNED NULL;

-- Add assigned_by
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS assigned_by BIGINT UNSIGNED NULL;

-- Add assigned_at
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS assigned_at DATETIME NULL;

-- Add completed_at
ALTER TABLE pick_task_line
    ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL;

-- ============================================================
-- Verify with:
-- DESCRIBE pick_task;
-- DESCRIBE pick_task_line;
-- ============================================================
