package model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickWave {

  public static final String STATUS_CREATED = "CREATED";
  public static final String STATUS_RELEASED = "RELEASED";
  public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
  public static final String STATUS_DONE = "DONE";
  public static final String STATUS_CANCELLED = "CANCELLED";

  private Long waveId;
  private String waveCode;
  private String status;
  private Long createdBy;
  private LocalDateTime createdAt;
}
