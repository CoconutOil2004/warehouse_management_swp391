package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PickTaskLineDTO {

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_PICKED = "PICKED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_CANCELLED = "CANCELLED";

    private static final DateTimeFormatter DISPLAY_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private Long pickTaskLineId;
    private Long pickTaskId;
    private Long gdnLineId;
    private Long fromSlotId;
    private String slotCode;
    private String zoneCode;
    private Long variantId;
    private String variantSku;
    private String productName;
    private String color;
    private String size;
    private BigDecimal qtyRequired;
    private BigDecimal qtyToPick;
    private BigDecimal qtyPicked;
    private String pickStatus;
    private Long assignedTo;
    private String assignedToName;
    private Long assignedBy;
    private String assignedByName;
    private LocalDateTime assignedAt;
    private LocalDateTime completedAt;
    private String note;
    private Long pickedBy;

    public String getAssignedAtDisplay() {
        return assignedAt == null ? "" : assignedAt.format(DISPLAY_FORMAT);
    }

    public String getCompletedAtDisplay() {
        return completedAt == null ? "" : completedAt.format(DISPLAY_FORMAT);
    }

    public String getPickStatusDisplay() {
        if (pickStatus == null) return "";
        return switch (pickStatus) {
            case STATUS_PENDING -> "Chờ nhặt";
            case STATUS_PICKED -> "Đang nhặt";
            case STATUS_COMPLETED -> "Hoàn thành";
            case STATUS_DONE -> "Hoàn thành";
            case STATUS_CANCELLED -> "Đã hủy";
            default -> pickStatus;
        };
    }

    public boolean isAssigned() {
        return assignedTo != null;
    }

    public boolean canPick() {
        return STATUS_PENDING.equals(pickStatus) || STATUS_PICKED.equals(pickStatus);
    }

    public boolean canAssign() {
        return !STATUS_DONE.equals(pickStatus) && !STATUS_COMPLETED.equals(pickStatus) && !STATUS_CANCELLED.equals(pickStatus);
    }
}
