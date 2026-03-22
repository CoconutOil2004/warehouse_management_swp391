package dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackingLineConfigDTO {
    private Long packingLineConfigId;
    private Long packingSessionId;
    private Long gdnLineId;
    private Integer itemsPerPack;
    private Integer numPacks;

    // Extra fields from GDN Line / Product
    private String variantSku;
    private String productName;
    private String color;
    private String size;
    private BigDecimal qtyPicked;
}
