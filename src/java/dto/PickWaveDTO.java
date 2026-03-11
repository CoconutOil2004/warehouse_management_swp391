package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickWaveDTO {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long waveId;
    private Long gdnId;
    private String gdnNumber;
    private String status;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
    private List<PickTaskDTO> tasks;

    /** For JSP display; avoids fmt:formatDate with LocalDateTime */
    public String getCreatedAtDisplay() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
    }
}
