-- ============================================================
-- Migration: Enhanced Packing Fields
-- Adds package_type, weight, weight_unit, notes,
-- total_packages, current_package_num to packing table
-- Adds IN_PROGRESS status support
-- ============================================================

-- 1. Add new columns to packing table
ALTER TABLE packing ADD COLUMN package_type VARCHAR(50) NULL AFTER package_label;
ALTER TABLE packing ADD COLUMN weight DECIMAL(10,2) NULL AFTER package_type;
ALTER TABLE packing ADD COLUMN weight_unit VARCHAR(10) NULL DEFAULT 'kg' AFTER weight;
ALTER TABLE packing ADD COLUMN notes VARCHAR(500) NULL AFTER weight_unit;
ALTER TABLE packing ADD COLUMN total_packages INT NULL AFTER notes;
ALTER TABLE packing ADD COLUMN current_package_num INT NULL AFTER total_packages;

-- 2. Update index for better query performance
ALTER TABLE packing ADD INDEX idx_packing_package_type (package_type);

-- 3. Optionally: Add carrier tracking table for multi-package shipments
CREATE TABLE packing_package (
    pkg_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pack_id BIGINT NOT NULL,
    package_number VARCHAR(50) NOT NULL,
    package_type VARCHAR(50) NULL,
    weight DECIMAL(10,2) NULL,
    weight_unit VARCHAR(10) NULL DEFAULT 'kg',
    tracking_code VARCHAR(100) NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_packing_package_packing FOREIGN KEY (pack_id) REFERENCES packing(pack_id),
    INDEX idx_packing_package_pack (pack_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Sample data update (optional - for existing records)
UPDATE packing SET weight_unit = 'kg' WHERE weight_unit IS NULL;
