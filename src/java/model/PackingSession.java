package model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PackingSession {
    private Long packingSessionId;
    private Long gdnId;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
