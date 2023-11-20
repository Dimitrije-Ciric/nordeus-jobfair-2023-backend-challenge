package com.nordeus.jobfair.auctionservice.auctionservice.domain.service;

import java.util.Date;
import java.util.List;

public interface DynamicJobSchedulerService {

    void scheduleStartNewAuctionsJob(Date triggerFireTime);

    void scheduleEndAuctionJob(Date triggerFireTime, Long auctionToEndIds);

    void scheduleEndAuctionJobs(Date triggerFireTime, List<Long> auctionToEndIds);

}
