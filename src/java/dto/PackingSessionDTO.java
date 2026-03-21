package dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class PackingSessionDTO {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long packingSessionId;
    private Long gdnId;
    private String status;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    // Extra fields
    private String gdnNumber;
    private String soNumber;
    private String customerName;
    private String createdByName;

    public String getCreatedAtDisplay() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
    }

    public String getCompletedAtDisplay() {
        return completedAt == null ? "" : completedAt.format(DISPLAY_FORMAT);
    }
}
