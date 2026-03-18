package dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickWaveDTO {

  public static final String STATUS_CREATED = "CREATED";
  public static final String STATUS_RELEASED = "RELEASED";
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_DONE = "DONE";
  public static final String STATUS_CANCELLED = "CANCELLED";

  private static final DateTimeFormatter DISPLAY_FORMAT =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private Long waveId;
  private String waveCode;
  private String status;
  private Long createdBy;
  private String createdByName;
  private LocalDateTime createdAt;
  private List<PickTaskDTO> tasks;
  private List<GDNListDTO> gdns;
  private Integer gdnCount;

  public String getCreatedAtDisplay() {
    return createdAt == null ? "" : createdAt.format(DISPLAY_FORMAT);
  }

  public String getStatusDisplay() {
    if (status == null) return "";
    return switch (status) {
      case STATUS_CREATED -> "Chờ phát hành";
      case STATUS_RELEASED -> "Đã phát hành";
      case STATUS_IN_PROGRESS -> "Đang thực hiện";
      case STATUS_DONE -> "Hoàn thành";
      case STATUS_CANCELLED -> "Đã hủy";
      default -> status;
    };
  }
}
