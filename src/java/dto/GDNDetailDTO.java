package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GDNDetailDTO {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long gdnId;
    private String gdnNumber;
    private Long warehouseId;
    private Long soId;
    private String soNumber;
    private Long customerId;
    private String customerName;
    private String customerAddress;
    private String gdnType;
    private String status;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private String lastShipmentStatus;
    private List<GDNLineDTO> lines;

    /** For JSP display; avoids fmt:formatDate with LocalDateTime (prevents ERR_INCOMPLETE_CHUNKED_ENCODING). */
    public String getCreatedAtDisplay() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
    }

    public String getConfirmedAtDisplay() {
        return confirmedAt == null ? "" : confirmedAt.format(DISPLAY_FORMAT);
    }
}
