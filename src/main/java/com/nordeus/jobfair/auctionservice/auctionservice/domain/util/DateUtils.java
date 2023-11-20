package com.nordeus.jobfair.auctionservice.auctionservice.domain.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Date;

@Component
public class DateUtils {

    private final Clock clock;

    @Autowired
    public DateUtils(Clock clock) {
        this.clock = clock;
    }

    public Date createDateFromNow(Long delay) {
        return new Date(clock.millis() + delay);
    }
}
