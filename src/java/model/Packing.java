package model;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class Packing {
    private Long packId;
    private Long gdnId;
    private String status;
    private Long packedBy;
    private LocalDateTime packedAt;
    private String packageLabel;
    private String packageType;
    private BigDecimal weight;
    private String weightUnit;
    private String notes;
    private Integer totalPackages;
    private Integer currentPackageNum;
}
