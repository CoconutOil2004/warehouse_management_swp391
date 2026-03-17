package dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for displaying warehouse staff workload.
 * Used in task assignment screen to show current workload of each staff member.
 */
@Getter
@Setter
public class UserWorkloadDTO {
    private Long userId;
    private String fullName;
    private Integer activeTasks;    // Number of tasks with status ASSIGNED or IN_PROGRESS
    private Integer activeLines;    // Total number of pick lines across all active tasks
    private String workloadLevel;   // LOW, MEDIUM, HIGH (for badge color)

    public UserWorkloadDTO() {
        this.activeTasks = 0;
        this.activeLines = 0;
    }

    public UserWorkloadDTO(Long userId, String fullName, Integer activeTasks, Integer activeLines) {
        this.userId = userId;
        this.fullName = fullName;
        this.activeTasks = activeTasks != null ? activeTasks : 0;
        this.activeLines = activeLines != null ? activeLines : 0;
    }

    /**
     * Determine workload level based on active tasks count.
     * LOW: 0-4 tasks
     * MEDIUM: 5-10 tasks
     * HIGH: > 10 tasks
     */
    public String getWorkloadLevel() {
        if (activeTasks == null || activeTasks <= 4) {
            return "LOW";
        } else if (activeTasks <= 10) {
            return "MEDIUM";
        } else {
            return "HIGH";
        }
    }
}
