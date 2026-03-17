package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {
    private Long categoryId;
    private String name;
    private String code;       // e.g. TSH, HD, PANTS - for SKU prefix
    private String sizeType;   // LETTER (S,M,L) or NUMBER (28,30,...)
    private Long parentId;
}
