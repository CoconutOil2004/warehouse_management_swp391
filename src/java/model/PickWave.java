package model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickWave {

  private Long waveId;
  private String waveCode;
  private Long gdnId; // Nullable - for backward compatibility
  private String status;
  private Long createdBy;
  private LocalDateTime createdAt;
}
