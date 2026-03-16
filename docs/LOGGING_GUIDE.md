# Logging Guide - Pick Wave Multi-GDN

## Overview

Tài liệu này hướng dẫn thêm logging cho các operations trong picking flow để dễ dàng debug và monitor.

## Logging Levels

- **INFO**: Các operations thành công (assign tasks, create wave, etc.)
- **WARNING**: Các vấn đề không nghiêm trọng (no selection, invalid params)
- **SEVERE**: Lỗi nghiêm trọng (database errors, assignment failures)

## Key Operations to Log

### 1. Task Assignment Operations

**Location:** `PickTaskController.java`

#### handleAssignAllBatch

```java
private void handleAssignAllBatch(
    HttpServletRequest request,
    HttpServletResponse response
) throws Exception {
    Logger logger = Logger.getLogger(PickTaskController.class.getName());
    
    // Validation logging
    if (taskIdsParam == null || taskIdsParam.length == 0) {
        logger.warning("Assign all batch failed: No task IDs provided");
        // ...
    }
    
    // Log each assignment
    for (String taskIdStr : taskIdsParam) {
        // ...
        if (assignedTo > 0) {
            logger.info("Task #" + taskId + " will be assigned to user #" + assignedTo);
        }
    }
    
    // Batch assignment logging
    logger.info("Starting batch assignment: " + assignedTaskIds.size() + " tasks for wave #" + waveId);
    
    int successCount = 0;
    for (int i = 0; i < assignedTaskIds.size(); i++) {
        try {
            // ... assign task
            successCount++;
            logger.info("Successfully assigned task #" + taskId + " to user #" + assignedTo);
        } catch (Exception e) {
            logger.severe("Failed to assign task #" + taskId + ": " + e.getMessage());
        }
    }
    
    logger.info("Batch assignment completed: " + successCount + "/" + assignedTaskIds.size() + " tasks assigned");
}
```

#### handleAutoAssign

```java
private void handleAutoAssign(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Logger logger = Logger.getLogger(PickTaskController.class.getName());
    
    Long waveId = parseLong(request.getParameter("waveId"), -1);
    
    try {
        logger.info("Auto-assign initiated for wave #" + waveId);
        
        pickTaskDao.autoAssignTasks(waveId, assignedBy);
        
        logger.info("Auto-assign completed successfully for wave #" + waveId);
    } catch (Exception e) {
        logger.severe("Auto-assign failed for wave #" + waveId + ": " + e.getMessage());
        throw e;
    }
}
```

### 2. Wave Creation Operations

**Location:** `PickWaveController.java`

#### handleCreate

```java
private void handleCreate(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Logger logger = Logger.getLogger(PickWaveController.class.getName());
    
    // Log GDN selection
    logger.info("Creating wave with " + gdnIds.size() + " GDNs: " + gdnIds);
    
    // Validate warehouse
    if (!warehouseId.equals(gdn.getWarehouseId())) {
        logger.warning("Warehouse mismatch: GDN " + gdnId + " has different warehouse");
        // ...
    }
    
    // Create wave
    try {
        Long waveId = waveDao.createWaveFromGDN(gdnIds.get(0), createdBy);
        logger.info("Wave created successfully: #" + waveId);
        
        // Link GDNs
        for (int i = 1; i < gdnIds.size(); i++) {
            waveDao.addGdnToWave(waveId, gdnIds.get(i));
            logger.info("Linked GDN #" + gdnIds.get(i) + " to wave #" + waveId);
        }
        
        // Create tasks
        created = taskDao.createTasksFromWave(waveId);
        logger.info("Created " + tasks.size() + " tasks for wave #" + waveId);
        
    } catch (Exception ex) {
        logger.severe("Wave creation failed: " + ex.getMessage());
        // Rollback logic
        throw ex;
    }
}
```

### 3. DAO Operations

**Location:** `PickTaskDAO.java`, `PickWaveDAO.java`

#### batchAssignTasks

```java
public void batchAssignTasks(List<Long> taskIds, Long userId, Long assignedBy) throws Exception {
    Logger logger = Logger.getLogger(PickTaskDAO.class.getName());
    
    logger.fine("Batch assigning " + taskIds.size() + " tasks to user #" + userId);
    
    String sql = """
        UPDATE pick_task 
        SET assigned_to = ?, assigned_by = ?, assigned_at = NOW(), status = 'ASSIGNED'
        WHERE pick_task_id = ?
        """;
    
    try (Connection conn = getConnection();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        
        conn.setAutoCommit(false);
        try {
            for (Long taskId : taskIds) {
                ps.setLong(1, userId);
                // ... set parameters
                ps.executeUpdate();
                logger.finest("Assigned task #" + taskId);
            }
            conn.commit();
            logger.info("Batch assignment committed successfully");
        } catch (SQLException e) {
            conn.rollback();
            logger.severe("Batch assignment rollback: " + e.getMessage());
            throw e;
        }
    }
}
```

## Log Configuration

### Tomcat Logging Properties

**File:** `conf/logging.properties`

```properties
# PickTaskController logging
controller.PickTaskController.level = INFO
controller.PickTaskController.handlers = java.util.logging.ConsoleHandler

# PickWaveController logging
controller.PickWaveController.level = INFO
controller.PickWaveController.handlers = java.util.logging.ConsoleHandler

# DAO logging
dao.PickTaskDAO.level = FINE
dao.PickWaveDAO.level = FINE

# Console handler format
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter
java.util.logging.ConsoleHandler.level = FINE
```

## Log Messages Template

### Task Assignment
- `INFO: Task #{taskId} will be assigned to user #{userId}`
- `INFO: Successfully assigned task #{taskId} to user #{userId}`
- `WARNING: No staff selected for task #{taskId}`
- `SEVERE: Failed to assign task #{taskId}: {error}`

### Wave Creation
- `INFO: Creating wave with {count} GDNs: {gdnIds}`
- `INFO: Wave created successfully: #{waveId}`
- `INFO: Linked GDN #{gdnId} to wave #{waveId}`
- `WARNING: Warehouse mismatch for GDN #{gdnId}`
- `SEVERE: Wave creation failed: {error}`

### Auto-Assign
- `INFO: Auto-assign initiated for wave #{waveId}`
- `INFO: Auto-assign completed successfully for wave #{waveId}`
- `SEVERE: Auto-assign failed for wave #{waveId}: {error}`

## Monitoring Dashboard

### Key Metrics to Track

1. **Task Assignment Rate**
   - Success count / Total tasks
   - Average time per assignment

2. **Wave Creation Success Rate**
   - Successful waves / Total attempts
   - Average GDNs per wave

3. **Error Rate**
   - SEVERE logs / Total logs
   - Most common errors

### Log Analysis Commands

```bash
# Count assignment successes
grep "Successfully assigned task" catalina.out | wc -l

# Count assignment failures
grep "Failed to assign task" catalina.out | wc -l

# Find warehouse mismatches
grep "Warehouse mismatch" catalina.out

# View recent errors
grep "SEVERE" catalina.out | tail -20
```

## Best Practices

1. **Log Context**: Include wave ID, task ID, user ID in messages
2. **Log Levels**: Use appropriate levels (INFO/WARNING/SEVERE)
3. **Error Messages**: Include exception details in SEVERE logs
4. **Performance**: Log execution time for slow operations
5. **Security**: Don't log sensitive information (passwords, tokens)

## Example Log Output

```
Mar 13, 2026 10:30:15 AM controller.PickTaskController handleAssignAllBatch
INFO: Starting batch assignment: 5 tasks for wave #123

Mar 13, 2026 10:30:15 AM controller.PickTaskController handleAssignAllBatch
INFO: Task #23 will be assigned to user #5

Mar 13, 2026 10:30:15 AM controller.PickTaskController handleAssignAllBatch
INFO: Successfully assigned task #23 to user #5

Mar 13, 2026 10:30:16 AM controller.PickTaskController handleAssignAllBatch
INFO: Batch assignment completed: 5/5 tasks assigned successfully
```
