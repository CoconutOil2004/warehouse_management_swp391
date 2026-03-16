# Hướng Dẫn Testing Migration - Pick Wave Multi-GDN

## Điều Kiện Tiên Quyết

- Môi trường database copy/staging (KHÔNG PHẢI production)
- MySQL client có quyền truy cập database
- Backup của database hiện tại

## Các Bước Test

### Bước 1: Backup Database

```bash
# Backup database hiện tại
mysqldump -u [username] -p warehouse_management > backup_$(date +%Y%m%d_%H%M%S).sql

# Xác minh backup tồn tại
ls -lh backup_*.sql
```

### Bước 2: Chạy Migration Scripts Theo Thứ Tự

```bash
# Kết nối đến database
mysql -u [username] -p warehouse_management

# Chạy migrations theo thứ tự
source docs/sql/migration_pick_wave_multi_gdn_part1.sql;
source docs/sql/migration_pick_wave_multi_gdn_part2.sql;
source docs/sql/migration_pick_wave_multi_gdn_part3.sql;

# Chạy backfill scripts
source docs/sql/backfill_pick_wave_gdn.sql;
source docs/sql/backfill_wave_code.sql;
```

### Bước 3: Xác Minh Migration

```sql
-- 1. Kiểm tra cấu trúc bảng pick_wave
DESCRIBE pick_wave;
-- Kết quả mong đợi: wave_code VARCHAR(50), gdn_id BIGINT NULL

-- 2. Kiểm tra bảng pick_wave_gdn tồn tại
SHOW TABLES LIKE 'pick_wave_gdn';
DESCRIBE pick_wave_gdn;
-- Kết quả mong đợi: wave_id, gdn_id, created_at với foreign keys

-- 3. Kiểm tra cấu trúc bảng pick_task
DESCRIBE pick_task;
-- Kết quả mong đợi: gdn_id BIGINT NULL

-- 4. Xác minh wave_code backfill
SELECT wave_id, wave_code, gdn_id 
FROM pick_wave 
ORDER BY wave_id DESC 
LIMIT 10;
-- Kết quả mong đợi: Tất cả waves có wave_code (WAVE-000001, v.v.)

-- 5. Xác minh pick_wave_gdn backfill
SELECT pw.wave_id, pw.wave_code, COUNT(pwg.gdn_id) AS gdn_count
FROM pick_wave pw
LEFT JOIN pick_wave_gdn pwg ON pw.wave_id = pwg.wave_id
GROUP BY pw.wave_id, pw.wave_code
ORDER BY pw.wave_id DESC
LIMIT 10;
-- Kết quả mong đợi: Mỗi wave có ít nhất 1 GDN

-- 6. Kiểm tra foreign key constraints
SELECT 
    CONSTRAINT_NAME,
    TABLE_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = 'warehouse_management'
  AND TABLE_NAME IN ('pick_wave_gdn')
  AND REFERENCED_TABLE_NAME IS NOT NULL;
-- Kết quả mong đợi: 2 foreign keys (wave_id, gdn_id)

-- 7. Kiểm tra indexes
SHOW INDEX FROM pick_wave_gdn;
-- Kết quả mong đợi: PRIMARY KEY (wave_id, gdn_id), idx_gdn_wave, idx_pick_wave_gdn_wave, idx_pick_wave_gdn_gdn
```

### Bước 4: Test Rollback (Tùy chọn nhưng Khuyến Khích)

```sql
-- Test rollback
DROP TABLE IF EXISTS pick_wave_gdn;

-- Xác minh bảng đã xóa
SHOW TABLES LIKE 'pick_wave_gdn';
-- Kết quả mong đợi: Rỗng

-- Restore từ backup nếu cần
-- mysql -u [username] -p warehouse_management < backup_YYYYMMDD_HHMMSS.sql
```

## Kết Quả Mong Đợi

### Bảng pick_wave changes:
- ✅ wave_code column tồn tại (VARCHAR(50), nullable)
- ✅ gdn_id có thể NULL (BIGINT NULL)
- ✅ Unique index trên wave_code

### Bảng pick_wave_gdn:
- ✅ Bảng tồn tại với cấu trúc đúng
- ✅ Primary key: (wave_id, gdn_id)
- ✅ Foreign key đến pick_wave(wave_id) ON DELETE CASCADE
- ✅ Foreign key đến goods_delivery_note(gdn_id) ON DELETE CASCADE
- ✅ Indexes: idx_gdn_wave, idx_pick_wave_gdn_wave, idx_pick_wave_gdn_gdn

### Bảng pick_task changes:
- ✅ gdn_id có thể NULL (BIGINT NULL)

### Data backfill:
- ✅ Tất cả waves hiện có có wave_code được điền
- ✅ Tất cả waves hiện có có entries trong pick_wave_gdn
- ✅ wave_code format: WAVE-000001, WAVE-000002, v.v.

## Các Vấn Đề Thường Gặp & Giải Pháp

### Vấn đề 1: Foreign key constraint fails
**Lỗi:** Cannot add foreign key constraint

**Giải pháp:**
- Kiểm tra các bảng tham chiếu (pick_wave, goods_delivery_note) tồn tại
- Kiểm tra column types khớp chính xác (BIGINT NOT NULL)
- Chạy: `SHOW ENGINE INNODB STATUS;` để biết lỗi chi tiết

### Vấn đề 2: Duplicate wave_code
**Lỗi:** Duplicate entry for key 'idx_pick_wave_code'

**Giải pháp:**
- Kiểm tra giá trị wave_id trùng lặp (không nên xảy ra)
- Chạy backfill script lại với ON DUPLICATE KEY UPDATE

### Vấn đề 3: Lo ngại mất dữ liệu
**Giải pháp:**
- Xác minh backup tồn tại trước khi chạy migrations
- Test thủ tục rollback
- Chạy trên môi trường staging trước

## Checklist Ký Xác Nhận

- [ ] Backup hoàn thành thành công
- [ ] Tất cả migration scripts chạy không lỗi
- [ ] Tất cả queries xác minh trả về kết quả mong đợi
- [ ] Rollback test thành công (tùy chọn)
- [ ] Application vẫn hoạt động với schema mới (smoke test)
- [ ] Sẵn sàng cho deployment production

## Các Bước Tiếp Theo

Sau khi testing thành công:
1. Ghi lại bất kỳ vấn đề nào gặp phải
2. Cập nhật migration scripts nếu cần
3. Lên lịch migration production
4. Tạo checklist migration production
