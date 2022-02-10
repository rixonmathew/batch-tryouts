package com.rixon.batch.datagenh2;

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
