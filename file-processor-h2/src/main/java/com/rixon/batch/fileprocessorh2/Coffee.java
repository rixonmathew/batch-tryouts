package com.rixon.batch.fileprocessorh2;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coffee {
    private String brand;
    private String characteristics;
    private String origin;
}
