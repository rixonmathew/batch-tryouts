package com.rixon.batch.datagenoracle;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static com.rixon.batch.datagenoracle.TrackerGenerator.setTracker;

@Service
public class AccountGenerator {

    private void populateAttributes(Account account, long id) {
        account.setId(id);
        account.setClientId("random client id "+id);
        account.setActive(true);
        account.setType("asset");
        account.setBalance(BigDecimal.valueOf(Math.random()*100_000L));
        setTracker(account);
    }

    public Account randomAccount(long id) {
        Account account = new Account();
        populateAttributes(account, id);
        return account;
    }

    public Stream<Account> generateAccounts(long count){
        return LongStream.rangeClosed(1,count)
                .mapToObj(l->{
                    Account account = new Account();
                    populateAttributes(account, l);
                    return account;
                });
    }
}
