package dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentListDTO {
    private Long shipmentId;
    private String shipmentNumber;
    private String carrierName;
    private String status;
    private String gdnNumber;
    private String shipmentType;
    private String trackingCode;
    private LocalDateTime createdAt;
}
