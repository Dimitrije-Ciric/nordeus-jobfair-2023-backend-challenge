package com.nordeus.jobfair.auctionservice.auctionservice.domain.job;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import lombok.AllArgsConstructor;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class EndAuctionJob extends QuartzJobBean {

    private final AuctionService auctionService;

    @Override
    protected void executeInternal(JobExecutionContext context) {
        Long auctionToEndId = context.getMergedJobDataMap().getLong("auctionToEndId");
        this.auctionService.endAuction(auctionToEndId);
    }
}
