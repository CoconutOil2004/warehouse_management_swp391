package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductVariant {

    private Long variantId;
    private Long productId;
    private String variantSku;
    private String color;
    private String colorHex;  // e.g. #FF0000 for swatch
    private String size;
    private String barcode;
    private String status;
}
