package com.nordeus.jobfair.auctionservice.auctionservice.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class UtilConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
