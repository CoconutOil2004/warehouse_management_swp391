package model;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

/**
 * Model class for pick_wave_gdn junction table.
 * Represents the N:M relationship between Pick Wave and Goods Delivery Note.
 */
@Getter
@Setter
public class PickWaveGdn {
    private Long waveId;
    private Long gdnId;
    private LocalDateTime createdAt;
}
