-- ============================================================
-- Migration: Category code/size_type, Product variant color_hex
-- Run against your WMS database. Skip if column already exists.
-- ============================================================

-- 1. Add code and size_type to category (for SKU prefix and size matrix: LETTER=S,M,L | NUMBER=28,29,30)
-- Run each ALTER only if the column does not exist.
ALTER TABLE category ADD COLUMN code VARCHAR(20) NULL AFTER name;
ALTER TABLE category ADD COLUMN size_type VARCHAR(10) NOT NULL DEFAULT 'NUMBER' AFTER code;
-- Backfill (optional): UPDATE category SET code = 'TSH', size_type = 'LETTER' WHERE ...;

-- 2. Add color_hex for variant color swatches (e.g. #FF0000)
ALTER TABLE product_variant ADD COLUMN color_hex VARCHAR(7) NULL AFTER color;
