# Các Bước Migration & Kế Hoạch Rollback - Pick Wave Multi-GDN

## Tổng quan

Tài liệu này cung cấp hướng dẫn từng bước để migrate schema database nhằm hỗ trợ nhiều GDNs trên mỗi Pick Wave, cùng với các thủ tục rollback.

## Vị Trí Migration Scripts

Tất cả scripts nằm trong thư mục: `docs/sql/`

| Script | Mục đích | Thứ tự |
|--------|---------|-------|
| `migration_pick_wave_multi_gdn_part1.sql` | Alter bảng pick_wave | 1 |
| `migration_pick_wave_multi_gdn_part2.sql` | Tạo bảng pick_wave_gdn | 2 |
| `migration_pick_wave_multi_gdn_part3.sql` | Alter bảng pick_task | 3 |
| `backfill_pick_wave_gdn.sql` | Backfill dữ liệu pick_wave_gdn | 4 |
| `backfill_wave_code.sql` | Backfill dữ liệu wave_code | 5 |

## Checklist Trước Migration

- [ ] Backup toàn bộ database production
- [ ] Test migration trên môi trường staging
- [ ] Xác minh code application đã được deploy và test
- [ ] Lên lịch bảo trì (nếu cần)
- [ ] Thông báo cho các bên liên quan
- [ ] Chuẩn bị sẵn kế hoạch rollback

## Các Bước Migration

### Bước 1: Backup Database

```bash
# Tạo backup với timestamp
mysqldump -u [username] -p \
  --single-transaction \
  --quick \
  --lock-tables=false \
  warehouse_management > backup_$(date +%Y%m%d_%H%M%S).sql

# Kiểm tra kích thước backup
ls -lh backup_*.sql

# (Tùy chọn) Nén backup
gzip backup_$(date +%Y%m%d_%H%M%S).sql
```

### Bước 2: Chạy Migration Scripts

```bash
# Kết nối đến database
mysql -u [username] -p warehouse_management

# Chạy migrations theo ĐÚNG thứ tự
source docs/sql/migration_pick_wave_multi_gdn_part1.sql;
source docs/sql/migration_pick_wave_multi_gdn_part2.sql;
source docs/sql/migration_pick_wave_multi_gdn_part3.sql;

# Chạy backfill scripts
source docs/sql/backfill_pick_wave_gdn.sql;
source docs/sql/backfill_wave_code.sql;

# Thoát
exit;
```

### Bước 3: Xác Minh Migration

```bash
# Chạy các queries xác minh
mysql -u [username] -p warehouse_management -e "
-- Kiểm tra cấu trúc bảng
DESCRIBE pick_wave;
DESCRIBE pick_wave_gdn;
DESCRIBE pick_task;

-- Kiểm tra dữ liệu
SELECT COUNT(*) AS wave_count FROM pick_wave;
SELECT COUNT(*) AS wave_gdn_count FROM pick_wave_gdn;
SELECT COUNT(*) AS waves_with_code FROM pick_wave WHERE wave_code IS NOT NULL;
"
```

**Kết Quả Mong Đợi:**
- `pick_wave`: cột wave_code tồn tại, gdn_id có thể NULL
- `pick_wave_gdn`: Bảng tồn tại với cấu trúc đúng
- `pick_task`: gdn_id có thể NULL
- Tất cả waves có wave_code được điền
- Tất cả waves hiện có có entries trong pick_wave_gdn

### Bước 4: Smoke Test Application

1. **Tạo Pick Wave mới:**
   - Truy cập `/pick-wave?action=create`
   - Chọn zones và GDNs
   - Tạo wave
   - Xác minh wave_code được tự động sinh

2. **Xem chi tiết Wave:**
   - Truy cập `/pick-wave?action=detail&id=<wave_id>`
   - Xác minh danh sách GDN hiển thị đúng

3. **Phân công Tasks:**
   - Truy cập `/pick-task?action=assign&waveId=<wave_id>`
   - Xác minh tasks được tạo theo zone
   - Phân công tasks cho nhân viên

4. **Hoàn thành Picking:**
   - Nhân viên hoàn thành pick task
   - Xác minh pick lines được sort theo zone → slot

### Bước 5: Giám Sát

- Giám sát application logs để tìm lỗi
- Giám sát hiệu suất database
- Kiểm tra vi phạm foreign key constraint
- Xác minh báo cáo từ người dùng

## Kế Hoạch Rollback

Nếu gặp sự cố, làm theo thủ tục rollback sau:

### Rollback Bước 1: Dừng Application

```bash
# Dừng Tomcat hoặc application server
systemctl stop tomcat
# Hoặc undeploy application
```

### Rollback Bước 2: Restore từ Backup (Full Rollback)

```bash
# Restore từ backup
mysql -u [username] -p warehouse_management < backup_YYYYMMDD_HHMMSS.sql

# Xác minh restore
mysql -u [username] -p warehouse_management -e "SHOW TABLES LIKE 'pick_wave_gdn';"
# Kết quả mong đợi: Rỗng (bảng không nên tồn tại)
```

### Rollback Bước 3: Partial Rollback (Chỉ Xóa Bảng Mới)

Nếu muốn giữ schema changes nhưng xóa dữ liệu:

```sql
-- Xóa bảng pick_wave_gdn
DROP TABLE IF EXISTS pick_wave_gdn;

-- Revert pick_wave.gdn_id về NOT NULL (nếu không có giá trị NULL)
-- TRƯỚC TIÊN: Kiểm tra NULL gdn_id
SELECT COUNT(*) FROM pick_wave WHERE gdn_id IS NULL;
-- Nếu count > 0, phải restore từ backup thay vì tiếp tục

-- Nếu count = 0, tiếp tục:
ALTER TABLE pick_wave MODIFY COLUMN gdn_id BIGINT NOT NULL;

-- Xóa cột wave_code (tùy chọn)
ALTER TABLE pick_wave DROP COLUMN wave_code;

-- Revert pick_task.gdn_id về NOT NULL
ALTER TABLE pick_task MODIFY COLUMN gdn_id BIGINT NOT NULL;
```

### Rollback Bước 4: Redeploy Code Cũ

```bash
# Redeploy phiên bản application trước đó
# (Giữ backup của WAR file cũ trước khi deploy)
```

## Xác Minh Sau Rollback

```sql
-- Xác minh pick_wave_gdn không tồn tại
SHOW TABLES LIKE 'pick_wave_gdn';
-- Kết quả mong đợi: Rỗng

-- Xác minh cấu trúc pick_wave
DESCRIBE pick_wave;
-- Kết quả mong đợi: Không có wave_code, gdn_id NOT NULL

-- Xác minh application hoạt động
-- Truy cập /pick-wave?action=list
-- Should work without errors
```

## Troubleshooting

### Vấn đề: Cannot drop foreign key constraint

**Giải pháp:**
```sql
-- Tìm tên constraint
SELECT CONSTRAINT_NAME 
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'pick_wave_gdn' 
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- Xóa constraint
ALTER TABLE pick_wave_gdn DROP FOREIGN KEY fk_wave_gdn_wave;
ALTER TABLE pick_wave_gdn DROP FOREIGN KEY fk_wave_gdn_gdn;

-- Sau đó xóa bảng
DROP TABLE pick_wave_gdn;
```

### Vấn đề: Application errors sau migration

**Giải pháp:**
1. Kiểm tra application logs để tìm lỗi cụ thể
2. Xác minh database connection hoạt động
3. Kiểm tra xem code có tương thích với schema mới không
4. Cân nhắc rollback nếu code chưa sẵn sàng

### Vấn đề: Data inconsistency sau backfill

**Giải pháp:**
```sql
-- Kiểm tra waves không có GDNs
SELECT pw.wave_id, pw.wave_code
FROM pick_wave pw
LEFT JOIN pick_wave_gdn pwg ON pw.wave_id = pwg.wave_id
WHERE pwg.gdn_id IS NULL;

-- Nếu tìm thấy, điều tra và sửa thủ công
-- Hoặc restore từ backup
```

## Liên Hệ Khẩn Cấp

| Vai trò | Tên | Liên hệ |
|------|------|---------|
| DBA | [Tên] | [Email/Phone] |
| Dev Lead | [Tên] | [Email/Phone] |
| Ops Lead | [Tên] | [Email/Phone] |

## Ký Xác Nhận

- [ ] Migration hoàn thành thành công
- [ ] Các queries xác minh đã passed
- [ ] Smoke tests passed
- [ ] Không có lỗi nghiêm trọng trong logs
- [ ] Người dùng có thể truy cập application
- [ ] Kế hoạch rollback đã được test (tùy chọn)

**Migration thực hiện bởi:** ________________  
**Ngày:** ________________  
**Giờ:** ________________  
**Ghi chú:** ________________
