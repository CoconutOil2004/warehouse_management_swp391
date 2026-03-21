package model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PackingTask {
    private Long packingTaskId;
    private Long packingLineConfigId;
    private Long assignedTo;
    private Integer assignedPacks;
    private Integer packedPacks;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
