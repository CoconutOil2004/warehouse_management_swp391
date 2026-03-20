package dto;

import lombok.Getter;
import lombok.Setter;

/**
 * DTO for task assignment suggestions.
 * Used in auto-assign feature to suggest which staff should get which tasks.
 */
@Getter
@Setter
public class TaskAssignmentSuggestionDTO {
    private Long pickTaskId;
    private Long suggestedUserId;
    private String suggestedUserName;
    private Integer taskLineCount; // Number of lines in this task
    private Integer currentWorkload; // Current workload of suggested user (in lines)
    private String reason; // Why this suggestion was made

    public TaskAssignmentSuggestionDTO() {
    }

    public TaskAssignmentSuggestionDTO(Long pickTaskId, Long suggestedUserId, String suggestedUserName,
            Integer taskLineCount, Integer currentWorkload, String reason) {
        this.pickTaskId = pickTaskId;
        this.suggestedUserId = suggestedUserId;
        this.suggestedUserName = suggestedUserName;
        this.taskLineCount = taskLineCount != null ? taskLineCount : 0;
        this.currentWorkload = currentWorkload != null ? currentWorkload : 0;
        this.reason = reason;
    }
}
