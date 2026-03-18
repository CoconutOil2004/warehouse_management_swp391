package model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickTaskLine {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PICKED = "PICKED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private Long pickLineId;
    private Long pickTaskId;
    private Long gdnLineId;
    private Long variantId;
    private Long fromSlotId;
    private BigDecimal qtyRequired;
    private BigDecimal qtyToPick;
    private BigDecimal qtyPicked;
    private String pickStatus;
    private Long assignedTo;
    private Long assignedBy;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String note;
}
