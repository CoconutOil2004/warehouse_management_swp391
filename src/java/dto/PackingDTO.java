package dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackingDTO {
    private Long packId;
    private Long gdnId;
    private String gdnNumber;
    private String status;
    private Long packedBy;
    private String packedByName;
    private LocalDateTime packedAt;
    private String packageLabel;
    private String packageType;
    private BigDecimal weight;
    private String weightUnit;
    private String notes;
    private Integer totalPackages;
    private Integer currentPackageNum;
    private String customerName;
    private String soNumber;
}
