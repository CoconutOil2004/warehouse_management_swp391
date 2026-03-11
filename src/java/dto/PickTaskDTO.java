package dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickTaskDTO {
    private Long pickTaskId;
    private Long waveId;
    private Long gdnId;
    private String gdnNumber;
    private String soNumber;
    private Long soId;
    private Long assignedTo;
    private String assignedToName;
    private Long assignedBy;
    private String assignedByName;
    private String status;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private List<PickTaskLineDTO> lines;
}
