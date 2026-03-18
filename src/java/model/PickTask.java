package model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class PickTask {

    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_ASSIGNED = "ASSIGNED";
    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private Long pickTaskId;
    private Long waveId;
    private Long gdnId;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
