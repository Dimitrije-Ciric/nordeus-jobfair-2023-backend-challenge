package com.nordeus.jobfair.auctionservice.auctionservice.domain.service.implementation;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.job.EndAuctionJob;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.job.StartNewAuctionsJob;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@Component
public class DynamicJobSchedulerServiceImpl implements DynamicJobSchedulerService {

    private final Scheduler scheduler;

    @Autowired
    public DynamicJobSchedulerServiceImpl(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    @Override
    public void scheduleStartNewAuctionsJob(Date triggerFireTime) {
        JobDetail jobDetail = this.createJobDetail(StartNewAuctionsJob.class);
        Trigger jobTrigger = this.createJobTrigger(triggerFireTime);

        this.scheduleJob(jobDetail, jobTrigger);
    }

    @Override
    public void scheduleEndAuctionJob(Date triggerFireTime, Long auctionToEndId) {
        JobDetail jobDetail = this.createJobDetail(EndAuctionJob.class);
        Trigger jobTrigger = this.createJobTrigger(triggerFireTime);

        jobDetail.getJobDataMap().put("auctionToEndId", auctionToEndId);

        this.scheduleJob(jobDetail, jobTrigger);
    }

    @Override
    public void scheduleEndAuctionJobs(Date triggerFireTime, List<Long> auctionToEndIds) {
        for (Long auctionToEndId : auctionToEndIds)
            this.scheduleEndAuctionJob(triggerFireTime, auctionToEndId);
    }

    private JobDetail createJobDetail(Class jobClass) {
        JobDetail jobDetail = newJob(jobClass).build();

        return jobDetail;
    }

    private Trigger createJobTrigger(Date triggerFireTime) {
        Trigger trigger = newTrigger()
                .startAt(triggerFireTime)
                .build();

        return trigger;
    }

    private void scheduleJob(JobDetail jobDetail, Trigger jobTrigger) {
        try {
            this.scheduler.scheduleJob(jobDetail, jobTrigger);
        } catch (SchedulerException e) {
            throw new RuntimeException(e);
        }
    }
}
