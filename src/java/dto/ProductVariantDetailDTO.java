package dto;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariantDetailDTO {
    private Long variantId;
    private String variantSku;
    private String color;
    private String colorHex;
    private String size;
    private String barcode;
    private String status;
    private BigDecimal totalQtyOnHand;
    private BigDecimal totalQtyAvailable;
}
