package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GDNListDTO {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long gdnId;
    private String gdnNumber;
    private String soNumber;
    private String customerName;
    private String status;
    private String creatorName;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private String lastShipmentStatus;

    /** For JSP display; avoids fmt:formatDate with LocalDateTime (can cause ERR_INCOMPLETE_CHUNKED_ENCODING). */
    public String getCreatedAtDisplay() {
        return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
    }
}
