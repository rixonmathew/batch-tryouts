package com.rixon.batch.datagenoracle;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
public class Account extends Tracker {
    private long id;
    private BigDecimal balance;
    private String clientId;
    private String type;
    private boolean active;
}
