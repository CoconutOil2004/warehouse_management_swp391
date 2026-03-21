package model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PackingLineConfig {
    private Long packingLineConfigId;
    private Long packingSessionId;
    private Long gdnLineId;
    private Integer itemsPerPack;
    private Integer numPacks;
}
