package model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class Shipment {
    private Long shipmentId;
    private String shipmentNumber;
    private Long gdnId;
    private Long carrierId;
    private String shipmentType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime pickedUpAt;
    private LocalDateTime deliveredAt;
    private String trackingCode;
    private String carrierName; // Added for display
    private String gdnNumber; // Added for display
    private String note;
}
