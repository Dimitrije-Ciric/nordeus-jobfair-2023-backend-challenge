package com.nordeus.jobfair.auctionservice.auctionservice.startup;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class AppStartupRunner implements ApplicationRunner {

    private final DynamicJobSchedulerService dynamicJobSchedulerService;
    private final DateUtils dateUtils;

    private final boolean runStartNewAuctionsJob;
    private final Long initialStartNewAuctionsJobDelay;

    @Autowired
    public AppStartupRunner(DynamicJobSchedulerService dynamicJobSchedulerService,
                            DateUtils dateUtils,
                            @Value("${startup.runStartNewAuctionsJob}") String runStartNewAuctionsJob,
                            @Value("${startup.initialStartNewAuctionsJobDelay}") String initialStartNewAuctionsJobDelay) {
        this.dynamicJobSchedulerService = dynamicJobSchedulerService;
        this.dateUtils = dateUtils;

        this.runStartNewAuctionsJob = Boolean.parseBoolean(runStartNewAuctionsJob);
        this.initialStartNewAuctionsJobDelay = Long.parseLong(initialStartNewAuctionsJobDelay);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (this.runStartNewAuctionsJob) {
            Date initialStartNewAuctionsJobTriggerTime = dateUtils.createDateFromNow(this.initialStartNewAuctionsJobDelay);
            this.dynamicJobSchedulerService.scheduleStartNewAuctionsJob(initialStartNewAuctionsJobTriggerTime);
        }
    }
}