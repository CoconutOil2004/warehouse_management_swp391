package model;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Warehouse {

    private Long warehouseId;
    private String code;
    private String name;
    private String address;
    private String status;
    private LocalDateTime createdAt;
}
