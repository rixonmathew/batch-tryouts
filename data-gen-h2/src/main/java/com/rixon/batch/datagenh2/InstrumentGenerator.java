package com.rixon.batch.datagenh2;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.rixon.batch.datagenh2.TrackerGenerator.setTracker;

@Service
public class InstrumentGenerator {

    private void populateAttributes(Instrument instrument, long id) {
        instrument.setId(id);
        instrument.setName("Random instrument "+ id);
        instrument.setInceptionDate(LocalDate.now().minusDays((long)(Math.random()*100)));
        instrument.setPrice(BigDecimal.valueOf(Math.random()*100_000L));
        setTracker(instrument);
    }

    public Instrument randomInstrument(long id) {
        Instrument instrument = new Instrument();
        populateAttributes(instrument, id);
        return instrument;
    }

    public Stream<Instrument> generateInstruments(long count){
        return LongStream.rangeClosed(1,count)
                .mapToObj(l->{
                    Instrument instrument = new Instrument();
                    populateAttributes(instrument, l);
                    return instrument;
                });
    }
}
