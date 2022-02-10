package com.rixon.batch.datagenoracle;

import java.time.LocalDateTime;

public class TrackerGenerator {

    public static void setTracker(Tracker tracker) {
        tracker.setCreatedBy("random");
        tracker.setCreatedTime(LocalDateTime.now());
        tracker.setUpdatedBy("random");
        tracker.setUpdatedTime(LocalDateTime.now());
        tracker.setVersion(1);
    }
}
