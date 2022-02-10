package com.rixon.batch.datagenoracle;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class Instrument extends Tracker {
    private long id;
    private String type;
    private String name;
    private BigDecimal price;
    private LocalDate inceptionDate;
}
