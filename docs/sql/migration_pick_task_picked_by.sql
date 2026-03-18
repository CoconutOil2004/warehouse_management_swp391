-- Add picked_by column to pick_task_line for tracking who picked the items
ALTER TABLE pick_task_line ADD COLUMN picked_by BIGINT NULL;
ALTER TABLE pick_task_line ADD CONSTRAINT fk_pick_task_line_picked_by FOREIGN KEY (picked_by) REFERENCES `user`(user_id);

-- Add index for faster lookup
CREATE INDEX idx_pick_task_line_picked_by ON pick_task_line(picked_by);
