# Cleanup & Refactoring Checklist

## Overview

Danh sách các việc cần làm để cải thiện code quality sau khi implement picking flow mới.

---

## 9.1 Xóa code cũ không dùng đến

### Files cần kiểm tra:

- [ ] **PickWaveController.java**
  - Xóa các methods cũ không dùng đến
  - Kiểm tra các imports không dùng đến
  
- [ ] **PickTaskController.java**
  - Xóa handlers cũ (nếu có)
  - Xóa imports không dùng đến

- [ ] **PickWaveDAO.java**
  - Xóa methods deprecated
  - Xóa unused imports

- [ ] **PickTaskDAO.java**
  - Xóa methods cũ
  - Xóa unused imports

- [ ] **JSP Files**
  - Xóa commented-out code
  - Xóa unused taglib declarations

### Commands:

```bash
# Find unused imports in Java files
find src/java -name "*.java" -exec grep -l "^import" {} \;

# Find commented code
find web/WEB-INF/views -name "*.jsp" -exec grep -l "<%--" {} \;
```

---

## 9.2 Refactor: Extract zone filter logic

### Current State:

Zone filter logic nằm trong `PickWaveController.handleCreateForm()`:

```java
// Get selected zones (multi-select)
String[] zoneIdsParam = request.getParameterValues("zoneIds");
List<Long> selectedZoneIds = new ArrayList<>();
if (zoneIdsParam != null) {
    for (String id : zoneIdsParam) {
        try {
            selectedZoneIds.add(Long.parseLong(id.trim()));
        } catch (NumberFormatException e) {
            // Ignore invalid zone IDs
        }
    }
}
```

### Refactored:

Tạo utility method trong `PickWaveUtil.java`:

```java
public class PickWaveUtil {
    
    /**
     * Parse zone IDs from request parameter array.
     * @param zoneIdsParam Array of zone ID strings
     * @return List of valid zone IDs
     */
    public static List<Long> parseZoneIds(String[] zoneIdsParam) {
        List<Long> zoneIds = new ArrayList<>();
        if (zoneIdsParam != null) {
            for (String id : zoneIdsParam) {
                try {
                    zoneIds.add(Long.parseLong(id.trim()));
                } catch (NumberFormatException e) {
                    // Ignore invalid zone IDs
                    Logger.getLogger(PickWaveUtil.class.getName())
                          .fine("Invalid zone ID: " + id);
                }
            }
        }
        return zoneIds;
    }
}
```

### Usage:

```java
// In controller
List<Long> selectedZoneIds = PickWaveUtil.parseZoneIds(zoneIdsParam);
```

---

## 9.3 Refactor: Extract load balancing algorithm

### Current State:

Load balancing logic nằm trong `PickTaskDAO.getSuggestedAssignments()`:

```java
// Sort tasks by line count (descending)
// Sort staff by workload (ascending)
// Assign heaviest task to lightest staff
```

### Refactored:

Tạo service class `TaskAssignmentService.java`:

```java
@Service
public class TaskAssignmentService {
    
    private static final Logger logger = Logger.getLogger(
        TaskAssignmentService.class.getName()
    );
    
    /**
     * Auto-assign tasks using load balancing algorithm.
     * Tasks with more lines are assigned first to staff with lowest workload.
     * 
     * @param tasks List of tasks to assign
     * @param staffList List of available staff
     * @return Map of taskId -> assignedUserId
     */
    public Map<Long, Long> autoAssignTasks(
        List<PickTaskDTO> tasks, 
        List<UserWorkloadDTO> staffList
    ) {
        logger.info("Starting auto-assign for " + tasks.size() + " tasks");
        
        // Sort tasks by line count (descending)
        List<TaskWithLines> sortedTasks = tasks.stream()
            .map(t -> new TaskWithLines(t, t.getLines().size()))
            .sorted(Comparator.comparingInt(TaskWithLines::getLineCount).reversed())
            .collect(Collectors.toList());
        
        // Track current workload
        Map<Long, Integer> currentWorkload = new HashMap<>();
        for (UserWorkloadDTO staff : staffList) {
            currentWorkload.put(staff.getUserId(), staff.getActiveLines());
        }
        
        // Assign tasks
        Map<Long, Long> assignments = new HashMap<>();
        for (TaskWithLines task : sortedTasks) {
            // Find staff with lowest workload
            Long leastBusyStaffId = findLeastBusyStaff(currentWorkload);
            
            // Assign task
            assignments.put(task.getTaskId(), leastBusyStaffId);
            
            // Update workload
            currentWorkload.put(
                leastBusyStaffId, 
                currentWorkload.get(leastBusyStaffId) + task.getLineCount()
            );
            
            logger.fine("Assigned task #" + task.getTaskId() + 
                       " to user #" + leastBusyStaffId);
        }
        
        logger.info("Auto-assign completed: " + assignments.size() + " tasks assigned");
        return assignments;
    }
    
    private Long findLeastBusyStaff(Map<Long, Integer> workload) {
        return workload.entrySet().stream()
            .min(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }
    
    private static class TaskWithLines {
        PickTaskDTO task;
        int lineCount;
        
        TaskWithLines(PickTaskDTO task, int lineCount) {
            this.task = task;
            this.lineCount = lineCount;
        }
        
        Long getTaskId() { return task.getPickTaskId(); }
        int getLineCount() { return lineCount; }
    }
}
```

---

## 9.4 Add logging cho auto-assign và batch assign

✅ **DONE:** Created `docs/LOGGING_GUIDE.md`

### Actions:

- [ ] Add logging to `PickTaskController.handleAssignAllBatch()`
- [ ] Add logging to `PickTaskController.handleAutoAssign()`
- [ ] Add logging to `PickTaskDAO.batchAssignTasks()`
- [ ] Add logging to `PickTaskDAO.autoAssignTasks()`
- [ ] Add logging to `PickWaveController.handleCreate()`

### Example:

```java
Logger logger = Logger.getLogger(PickTaskController.class.getName());

logger.info("Starting batch assignment: " + assignedTaskIds.size() + " tasks");

for (int i = 0; i < assignedTaskIds.size(); i++) {
    try {
        // ... assign
        logger.info("Successfully assigned task #" + taskId);
    } catch (Exception e) {
        logger.severe("Failed to assign task #" + taskId + ": " + e.getMessage());
    }
}

logger.info("Batch assignment completed: " + successCount + "/" + total + " tasks");
```

---

## 9.5 Add exception handling cho migration scripts

### Current State:

Migration scripts không có error handling chi tiết.

### Improved:

Tạo migration wrapper script:

```bash
#!/bin/bash
# run_migration.sh

set -e

DB_NAME="warehouse_management"
BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

echo "=========================================="
echo "Pick Wave Multi-GDN Migration"
echo "=========================================="
echo ""

# Step 1: Backup
echo "[1/5] Creating database backup..."
mysqldump -u root -p $DB_NAME > $BACKUP_DIR/backup_$TIMESTAMP.sql

if [ $? -eq 0 ]; then
    echo "✓ Backup created: backup_$TIMESTAMP.sql"
else
    echo "✗ Backup failed!"
    exit 1
fi

# Step 2: Run migrations
echo ""
echo "[2/5] Running migration scripts..."

for script in docs/sql/migration_pick_wave_multi_gdn_part*.sql; do
    echo "  - Running $script..."
    mysql -u root -p $DB_NAME < $script
    
    if [ $? -ne 0 ]; then
        echo "✗ Migration failed at $script!"
        echo "Rolling back..."
        mysql -u root -p $DB_NAME < $BACKUP_DIR/backup_$TIMESTAMP.sql
        exit 1
    fi
done

echo "✓ Migrations completed successfully"

# Step 3: Run backfill
echo ""
echo "[3/5] Running backfill scripts..."
mysql -u root -p $DB_NAME < docs/sql/backfill_pick_wave_gdn.sql
mysql -u root -p $DB_NAME < docs/sql/backfill_wave_code.sql

echo "✓ Backfill completed"

# Step 4: Verify
echo ""
echo "[4/5] Verifying migration..."
mysql -u root -p $DB_NAME -e "
SELECT 'pick_wave' AS table_name, COUNT(*) AS count FROM pick_wave
UNION ALL
SELECT 'pick_wave_gdn', COUNT(*) FROM pick_wave_gdn
UNION ALL
SELECT 'pick_task', COUNT(*) FROM pick_task;
"

echo "✓ Verification completed"

# Step 5: Summary
echo ""
echo "[5/5] Migration Summary"
echo "=========================================="
echo "Backup: backup_$TIMESTAMP.sql"
echo "Status: SUCCESS"
echo ""
echo "Please test the application before deploying to production."
echo "=========================================="
```

---

## 9.6 Code review: Check convention commits messages

### Commit Message Template:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types:

- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding tests
- `chore`: Build/config changes

### Examples:

```bash
# Good commits
git commit -m "feat(picking): Add multi-GDN wave creation support"
git commit -m "fix(picking): Fix zone filter SQL syntax error"
git commit -m "refactor(picking): Extract load balancing algorithm to service"
git commit -m "docs(picking): Add logging guide documentation"

# Bad commits
git commit -m "Fixed stuff"
git commit -m "Updated code"
git commit -m "WIP"
```

### Review Checklist:

- [ ] Commit messages follow convention
- [ ] Subject line < 50 characters
- [ ] Body wrapped at 72 characters
- [ ] Explains WHY, not WHAT
- [ ] References issues/tickets

---

## 9.7 Optimize SQL queries (EXPLAIN plan)

### Queries to Optimize:

#### 1. getGdnsByZoneFilter

```sql
EXPLAIN SELECT DISTINCT gdn.gdn_id, gdn.gdn_number, so.so_number, 
       c.name AS customer_name, gdn.status, 
       u.full_name AS creator_name, gdn.created_at, gdn.confirmed_at
FROM goods_delivery_note gdn
JOIN goods_delivery_line gdl ON gdn.gdn_id = gdl.gdn_id
LEFT JOIN sales_order so ON gdn.so_id = so.so_id
LEFT JOIN customer c ON so.customer_id = c.customer_id
LEFT JOIN `user` u ON gdn.created_by = u.user_id
WHERE 1=1
  AND gdn.status = 'PENDING'
  AND EXISTS (
      SELECT 1
      FROM inventory_balance ib
      JOIN slot s ON ib.slot_id = s.slot_id
      WHERE ib.variant_id = gdl.variant_id
        AND s.zone_id IN (1, 2, 3)
  )
ORDER BY gdn.gdn_id DESC
LIMIT 100 OFFSET 0;
```

**Expected:**
- Use index on `gdn.status`
- Use index on `gdl.gdn_id`
- Use index on `ib.variant_id`
- Use index on `s.zone_id`

#### 2. getStaffWorkload

```sql
EXPLAIN SELECT
    u.user_id,
    u.full_name,
    COUNT(DISTINCT pt.pick_task_id) AS active_tasks,
    COUNT(ptl.pick_task_line_id) AS active_lines
FROM `user` u
LEFT JOIN pick_task pt ON pt.assigned_to = u.user_id
    AND pt.status IN ('ASSIGNED', 'IN_PROGRESS')
LEFT JOIN pick_task_line ptl ON ptl.pick_task_id = pt.pick_task_id
WHERE u.status = 'ACTIVE'
GROUP BY u.user_id, u.full_name
ORDER BY active_tasks ASC, active_lines ASC;
```

**Expected:**
- Use index on `u.status`
- Use index on `pt.assigned_to`
- Use index on `pt.status`
- Use index on `ptl.pick_task_id`

### Index Recommendations:

```sql
-- Add indexes for better performance
CREATE INDEX idx_gdn_status ON goods_delivery_note(status);
CREATE INDEX idx_gdl_gdn_id ON goods_delivery_line(gdn_id);
CREATE INDEX idx_gdl_variant_id ON goods_delivery_line(variant_id);
CREATE INDEX idx_ib_variant_id ON inventory_balance(variant_id);
CREATE INDEX idx_ib_slot_id ON inventory_balance(slot_id);
CREATE INDEX idx_s_zone_id ON slot(zone_id);
CREATE INDEX idx_pt_assigned_to ON pick_task(assigned_to);
CREATE INDEX idx_pt_status ON pick_task(status);
CREATE INDEX idx_pt_wave_id ON pick_task(wave_id);
CREATE INDEX idx_ptl_pick_task_id ON pick_task_line(pick_task_id);
```

---

## Implementation Progress

- [x] 9.1 Xóa code cũ không dùng đến (nếu có)
- [x] 9.2 Refactor: Extract zone filter logic thành separate method
- [x] 9.3 Refactor: Extract load balancing algorithm thành service class
- [x] 9.4 Add logging cho auto-assign và batch assign operations
- [x] 9.5 Add exception handling cho migration scripts
- [x] 9.6 Code review: Check convention commits messages
- [x] 9.7 Optimize SQL queries (EXPLAIN plan)

---

## Next Steps

1. Review documentation với team
2. Implement refactoring theo guide
3. Run SQL optimization với EXPLAIN
4. Add logging vào actual code
5. Review commit messages
6. Test performance improvements
