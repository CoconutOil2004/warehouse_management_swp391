-- ============================================================
-- Migration: add missing columns to pick_task
-- Run this against your WMS database.
-- ============================================================

-- Add completed_at column to pick_task
ALTER TABLE pick_task
    ADD COLUMN IF NOT EXISTS completed_at DATETIME NULL AFTER started_at;

-- Verify: DESCRIBE pick_task;
