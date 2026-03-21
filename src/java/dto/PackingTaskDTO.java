package dto;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class PackingTaskDTO {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long packingTaskId;
    private Long packingLineConfigId;
    private Long assignedTo;
    private Integer assignedPacks;
    private Integer packedPacks;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Extra fields
    private String assignedToName;
    private String gdnNumber;
    private String variantSku;
    private String productName;
    private Integer itemsPerPack;

    public String getUpdatedAtDisplay() {
        return updatedAt == null ? "" : updatedAt.format(DISPLAY_FORMAT);
    }
}
