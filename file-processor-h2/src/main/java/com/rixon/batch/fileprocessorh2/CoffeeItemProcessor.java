package com.rixon.batch.fileprocessorh2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.util.Locale;

public class CoffeeItemProcessor  implements ItemProcessor<Coffee,Coffee> {

    private final static Logger LOGGER = LoggerFactory.getLogger(CoffeeItemProcessor.class);

    @Override
    public Coffee process(Coffee item) throws Exception {
//        LOGGER.info("Translating to upper case [{}]",item);
        String processedBrand = item.getBrand().toUpperCase(Locale.ROOT);
        String processedOrigin = item.getOrigin().toUpperCase(Locale.ROOT);
        String processedCharacteristics = item.getCharacteristics().toUpperCase(Locale.ROOT);
        return new Coffee(processedBrand,processedCharacteristics,processedOrigin);
    }
}
