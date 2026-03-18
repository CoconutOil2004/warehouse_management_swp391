package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickTaskDTO {

  public static final String STATUS_CREATED = "CREATED";
  public static final String STATUS_PENDING = "PENDING";
  public static final String STATUS_ASSIGNED = "ASSIGNED";
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_COMPLETED = "COMPLETED";
  public static final String STATUS_CANCELLED = "CANCELLED";

  private static final DateTimeFormatter DISPLAY_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private Long pickTaskId;
  private Long waveId;
  private Long gdnId;
  private String gdnNumber;
  private String soNumber;
  private Long soId;
  private String status;
  private LocalDateTime startedAt;
  private LocalDateTime completedAt;
  private Long assignedTo;
  private String assignedToName;
  private LocalDateTime assignedAt;
  private List<PickTaskLineDTO> lines;
  private Integer totalLines;

  public String getStartedAtDisplay() {
    return startedAt == null ? "" : startedAt.format(DISPLAY_FORMAT);
  }

  public String getCompletedAtDisplay() {
    return completedAt == null ? "" : completedAt.format(DISPLAY_FORMAT);
  }

  public String getAssignedAtDisplay() {
    return assignedAt == null ? "" : assignedAt.format(DISPLAY_FORMAT);
  }

  public String getStatusDisplay() {
    if (status == null) return "";
    return switch (status) {
      case STATUS_CREATED -> "Mới tạo";
      case STATUS_PENDING -> "Chờ gán";
      case STATUS_ASSIGNED -> "Đã gán";
      case STATUS_IN_PROGRESS -> "Đang thực hiện";
      case STATUS_COMPLETED -> "Hoàn thành";
      case STATUS_CANCELLED -> "Đã hủy";
      default -> status;
    };
  }
}
