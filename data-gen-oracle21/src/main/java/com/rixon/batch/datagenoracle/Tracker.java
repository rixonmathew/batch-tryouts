package com.rixon.batch.datagenoracle;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public abstract class Tracker {
    private String createdBy;
    private LocalDateTime createdTime;
    private String updatedBy;
    private LocalDateTime updatedTime;
    private int version;
}
