# Luồng nghiệp vụ phần picking

## Thiết kế database phần picking

```sql
CREATE TABLE `pick_wave_gdn` (
  `wave_id` bigint unsigned NOT NULL,
  `gdn_id` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`wave_id`,`gdn_id`),
  KEY `idx_gdn_wave` (`gdn_id`,`wave_id`),
  KEY `idx_pick_wave_gdn_wave` (`wave_id`),
  KEY `idx_pick_wave_gdn_gdn` (`gdn_id`),
  CONSTRAINT `fk_wave_gdn_gdn` FOREIGN KEY (`gdn_id`) REFERENCES `goods_delivery_note` (`gdn_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_wave_gdn_wave` FOREIGN KEY (`wave_id`) REFERENCES `pick_wave` (`wave_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `pick_wave` (
  `wave_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `wave_code` varchar(50) DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `created_by` bigint unsigned NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`wave_id`),
  UNIQUE KEY `idx_pick_wave_code` (`wave_code`),
  KEY `idx_wave_created_by` (`created_by`),
  CONSTRAINT `fk_wave_created_by` FOREIGN KEY (`created_by`) REFERENCES `user` (`user_id`),
  CONSTRAINT `chk_pick_wave_status` CHECK ((`status` in (_utf8mb4'CREATED',_utf8mb4'RELEASED',_utf8mb4'IN_PROGRESS',_utf8mb4'DONE',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `pick_task` (
  `pick_task_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `wave_id` bigint unsigned NOT NULL,
  `gdn_id` bigint unsigned DEFAULT NULL,
  `status` varchar(30) NOT NULL,
  `started_at` datetime DEFAULT NULL,
  PRIMARY KEY (`pick_task_id`),
  KEY `idx_pick_task_wave` (`wave_id`),
  KEY `idx_pick_task_gdn` (`gdn_id`),
  CONSTRAINT `fk_pick_task_gdn` FOREIGN KEY (`gdn_id`) REFERENCES `goods_delivery_note` (`gdn_id`),
  CONSTRAINT `fk_pick_task_wave` FOREIGN KEY (`wave_id`) REFERENCES `pick_wave` (`wave_id`),
  CONSTRAINT `chk_pick_task_status` CHECK ((`status` in (_utf8mb4'CREATED',_utf8mb4'PENDING',_utf8mb4'ASSIGNED',_utf8mb4'IN_PROGRESS',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED')))
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci

CREATE TABLE `pick_task_line` (
  `pick_task_line_id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `pick_task_id` bigint unsigned NOT NULL,
  `gdn_line_id` bigint unsigned NOT NULL,
  `variant_id` bigint unsigned DEFAULT NULL,
  `from_slot_id` bigint unsigned NOT NULL,
  `qty_required` decimal(18,4) DEFAULT NULL,
  `qty_to_pick` decimal(18,4) NOT NULL,
  `qty_picked` decimal(18,4) NOT NULL DEFAULT '0.0000',
  `pick_status` varchar(30) NOT NULL,
  `assigned_to` bigint unsigned DEFAULT NULL,
  `assigned_by` bigint unsigned DEFAULT NULL,
  `assigned_at` datetime DEFAULT NULL,
  `completed_at` datetime DEFAULT NULL,
  `note` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`pick_task_line_id`),
  KEY `idx_pick_line_task` (`pick_task_id`),
  KEY `idx_pick_line_gdn_line` (`gdn_line_id`),
  KEY `idx_pick_line_from_slot` (`from_slot_id`),
  KEY `idx_pick_task_line_variant` (`variant_id`),
  KEY `fk_pick_line_assigned_to` (`assigned_to`),
  KEY `fk_pick_line_assigned_by` (`assigned_by`),
  CONSTRAINT `fk_pick_line_assigned_by` FOREIGN KEY (`assigned_by`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_pick_line_assigned_to` FOREIGN KEY (`assigned_to`) REFERENCES `user` (`user_id`),
  CONSTRAINT `fk_pick_line_from_slot` FOREIGN KEY (`from_slot_id`) REFERENCES `slot` (`slot_id`),
  CONSTRAINT `fk_pick_line_gdn_line` FOREIGN KEY (`gdn_line_id`) REFERENCES `goods_delivery_line` (`gdn_line_id`),
  CONSTRAINT `fk_pick_line_task` FOREIGN KEY (`pick_task_id`) REFERENCES `pick_task` (`pick_task_id`),
  CONSTRAINT `fk_pick_task_line_variant` FOREIGN KEY (`variant_id`) REFERENCES `product_variant` (`variant_id`),
  CONSTRAINT `chk_pick_line_status` CHECK ((`pick_status` in (_utf8mb4'PENDING',_utf8mb4'PICKED',_utf8mb4'COMPLETED',_utf8mb4'CANCELLED',_utf8mb4'DONE')))
) ENGINE=InnoDB AUTO_INCREMENT=45 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
```

## Luồng nghiệp vụ

### Bước 1: Gom đơn và Lập kế hoạch (Wave Creation)
*   **Hành động:** Thủ kho chọn danh sách các đơn xuất kho (`goods_delivery_note`) cần xử lý trong cùng một ca hoặc cùng một chuyến xe.
*   **Dữ liệu:**
    *   Một bản ghi mới được tạo trong `pick_wave` với trạng thái `CREATED`.
    *   Mối quan hệ giữa Wave và các đơn hàng được lưu vào bảng trung gian `pick_wave_gdn`. 
    *   *Ý nghĩa:* Việc gom này giúp tối ưu hóa quãng đường di chuyển của nhân viên trong kho thay vì đi nhặt lẻ tẻ từng đơn.

### Bước 2: Tạo nhiệm vụ nhặt hàng (Task Generation)
*   **Hành động:** Hệ thống hoặc quản lý "phóng thích" (Release) Wave. Lúc này, hệ thống sẽ tự động bóc tách dữ liệu từ GDN để tạo ra các `pick_task`.
*   **Dữ liệu:** 
    *   Mỗi đơn hàng (`gdn_id`) trong Wave sẽ tương ứng với một hoặc nhiều `pick_task`.
    *   Trạng thái `pick_wave` chuyển sang `RELEASED`.
    *   Các `pick_task` được tạo với trạng thái `CREATED`.

### Bước 3: Phân rã chi tiết và Gán nhân sự (Line Assignment)
Đây là phần đặc biệt nhất trong thiết kế của bạn:
*   **Hành động:** Hệ thống dựa vào vị trí hàng hóa (`from_slot_id`) để tạo ra các `pick_task_line`.
*   **Giao việc:** Người quản lý thực hiện gán (`assigned_to`) từng dòng hàng cho nhân viên kho. 
    *   *Ví dụ:* Một đơn hàng có 10 món, nhưng 5 món ở khu vực A và 5 món ở khu vực B. Quản lý có thể gán 5 dòng đầu cho nhân viên A và 5 dòng sau cho nhân viên B ngay trong cùng một Task.
*   **Dữ liệu:** Cập nhật `assigned_to`, `assigned_by`, và `assigned_at` trong bảng `pick_task_line`. Trạng thái line chuyển sang `PENDING`.

### Bước 4: Thực hiện nhặt hàng (Physical Picking)
*   **Hành động:** Nhân viên kho cầm thiết bị (PDA/Mobile) xem danh sách các line được gán cho mình.
*   **Thao tác:**
    1.  Nhân viên đến đúng vị trí `from_slot_id`.
    2.  Quét mã sản phẩm (`variant_id`).
    3.  Nhập số lượng thực tế đã nhặt (`qty_picked`).
*   **Dữ liệu:** 
    *   `pick_task_line.pick_status` cập nhật thành `PICKED` hoặc `DONE`.
    *   Ghi nhận thời điểm hoàn thành vào `completed_at`.
    *   Bảng `pick_task` cập nhật `started_at` khi dòng đầu tiên được bắt đầu.

### Bước 5: Hoàn tất và Đóng đợt (Completion)
*   **Hành động:** Hệ thống kiểm tra điều kiện hoàn thành theo cơ chế từ dưới lên trên.
*   **Logic tổng hợp:**
    *   Khi tất cả `pick_task_line` của một Task hoàn thành -> `pick_task.status` chuyển thành `COMPLETED`.
    *   Khi tất cả `pick_task` của một Wave hoàn thành -> `pick_wave.status` chuyển thành `DONE`.
*   **Kết quả:** Hàng hóa sau khi nhặt xong sẽ được tập kết tại khu vực đóng gói (Packing Area) để chờ xuất đi.

---

## Tóm tắt trạng thái thực thể

| Thực thể | Trạng thái chính | Ý nghĩa vận hành |
| :--- | :--- | :--- |
| **Pick Wave** | `CREATED` -> `RELEASED` -> `DONE` | Từ lúc lập kế hoạch đến khi cả đợt hàng đã sẵn sàng. |
| **Pick Task** | `CREATED` -> `IN_PROGRESS` -> `COMPLETED` | Theo dõi tiến độ xử lý của từng đơn hàng cụ thể. |
| **Pick Task Line** | `PENDING` -> `PICKED` -> `COMPLETED` | Theo dõi chi tiết từng món hàng và năng suất của từng nhân viên. |
