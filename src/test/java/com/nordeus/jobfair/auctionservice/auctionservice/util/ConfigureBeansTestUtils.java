package com.nordeus.jobfair.auctionservice.auctionservice.util;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.implementation.AuctionsUserServiceImpl;
import com.nordeus.jobfair.auctionservice.auctionservice.startup.AppStartupRunner;
import org.springframework.stereotype.Component;
import org.springframework.test.util.ReflectionTestUtils;

@Component
public class ConfigureBeansTestUtils {

    public void configureAuctionService(AuctionService auctionService,
                                               Integer newAuctionsCount,
                                               Long auctionsRefreshDelay,
                                               Long FinalBidPeriodDuration,
                                               Long auctionsProlongedTimeDuration,
                                               Integer auctionsInitialBidValue) {
        ReflectionTestUtils.setField(auctionService, "startNewAuctionsCount", newAuctionsCount);
        ReflectionTestUtils.setField(auctionService, "auctionsRefreshDelay", auctionsRefreshDelay);
        ReflectionTestUtils.setField(auctionService, "auctionsFinalBidPeriodDuration", FinalBidPeriodDuration);
        ReflectionTestUtils.setField(auctionService, "auctionsProlongedTimeDuration", auctionsProlongedTimeDuration);
        ReflectionTestUtils.setField(auctionService, "auctionsInitialBidValue", auctionsInitialBidValue);
    }

    public void configureAppStartupRunner(AppStartupRunner appStartupRunner,
                                          boolean runStartNewAuctionsJob,
                                          Long initialStartNewAuctionsJobDelay) {
        ReflectionTestUtils.setField(appStartupRunner, "runStartNewAuctionsJob", runStartNewAuctionsJob);
        ReflectionTestUtils.setField(appStartupRunner, "initialStartNewAuctionsJobDelay", initialStartNewAuctionsJobDelay);
    }

    public void configureAuctionsUserService(AuctionsUserService auctionsUserService,
                                             Integer auctionsUserInitialTokensAmount) {
        ReflectionTestUtils.setField(auctionsUserService, "auctionsUserInitialTokensAmount", auctionsUserInitialTokensAmount);
    }

}
