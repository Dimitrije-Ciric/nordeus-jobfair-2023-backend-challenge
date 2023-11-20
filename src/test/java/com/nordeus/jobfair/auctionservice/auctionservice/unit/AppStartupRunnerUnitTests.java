package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import com.nordeus.jobfair.auctionservice.auctionservice.startup.AppStartupRunner;
import com.nordeus.jobfair.auctionservice.auctionservice.util.ConfigureBeansTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.util.Date;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AppStartupRunnerUnitTests {

    @Autowired
    private AppStartupRunner appStartupRunner;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private ConfigureBeansTestUtils configureBeansTestUtils;

    @MockBean
    private ApplicationArguments applicationArguments;

    @MockBean
    private DynamicJobSchedulerService dynamicJobSchedulerService;

    @MockBean
    private Clock clock;

    @Test
    void startupWithoutJobsScheduling() {
        this.configureBeansTestUtils.configureAppStartupRunner(this.appStartupRunner, false, 0L);

        this.appStartupRunner.run(this.applicationArguments);
        verifyNoInteractions(this.dynamicJobSchedulerService);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1000, 10000})
    void startupWithJobsScheduling(Long initialStartNewAuctionsTriggerDelay) {
        given(clock.millis()).willReturn(0L);

        Date initialStartNewAuctionsTriggerTime = this.dateUtils.createDateFromNow(initialStartNewAuctionsTriggerDelay);
        this.configureBeansTestUtils.configureAppStartupRunner(this.appStartupRunner, true, initialStartNewAuctionsTriggerDelay);

        this.appStartupRunner.run(this.applicationArguments);

        ArgumentCaptor<Date> capturedTriggerFireTime = ArgumentCaptor.forClass(Date.class);
        verify(this.dynamicJobSchedulerService, times(1)).scheduleStartNewAuctionsJob(capturedTriggerFireTime.capture());
        assertThat(capturedTriggerFireTime.getValue()).isEqualTo(initialStartNewAuctionsTriggerTime);
    }

}
