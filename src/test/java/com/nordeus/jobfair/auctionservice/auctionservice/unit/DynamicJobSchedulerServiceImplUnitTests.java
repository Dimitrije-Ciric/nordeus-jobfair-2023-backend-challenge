package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.job.EndAuctionJob;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.job.StartNewAuctionsJob;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DynamicJobSchedulerServiceImplUnitTests {

    @Autowired
    private DynamicJobSchedulerService dynamicJobSchedulerService;

    @Autowired
    private DateUtils dateUtils;

    @MockBean
    private Scheduler quartzScheduler;

    @MockBean
    private Clock clock;

    @ParameterizedTest
    @ValueSource(longs = {0, 5000, 10000})
    void scheduleStartNewAuctionsJobTest(Long jobTriggerFireDelay) throws SchedulerException {
        Date expectedJobTriggerFireTime = new Date(jobTriggerFireDelay);

        given(clock.millis()).willReturn(0L);
        this.dynamicJobSchedulerService.scheduleStartNewAuctionsJob(expectedJobTriggerFireTime);


        ArgumentCaptor<JobDetail> capturedJobDetail = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> capturedJobTrigger = ArgumentCaptor.forClass(Trigger.class);

        verify(this.quartzScheduler, times(1)).scheduleJob(capturedJobDetail.capture(),
                                                                                   capturedJobTrigger.capture());

        assertThat(capturedJobDetail.getValue().getJobClass()).isEqualTo(StartNewAuctionsJob.class);
        assertThat(capturedJobTrigger.getValue().getStartTime()).isEqualTo(expectedJobTriggerFireTime);
    }

    @ParameterizedTest
    @CsvSource({"0,10", "5000,13", "10000,0"})
    void scheduleEndAuctionJobTest(Long jobTriggerFireDelay,
                                    Long auctionToEndId) throws SchedulerException {
        Date expectedJobTriggerFireTime = new Date(jobTriggerFireDelay);

        this.dynamicJobSchedulerService.scheduleEndAuctionJob(expectedJobTriggerFireTime, auctionToEndId);


        ArgumentCaptor<JobDetail> capturedJobDetail = ArgumentCaptor.forClass(JobDetail.class);
        ArgumentCaptor<Trigger> capturedJobTrigger = ArgumentCaptor.forClass(Trigger.class);

        verify(this.quartzScheduler, times(1)).scheduleJob(capturedJobDetail.capture(),
                                                                                   capturedJobTrigger.capture());

        assertThat(capturedJobDetail.getValue().getJobClass()).isEqualTo(EndAuctionJob.class);
        assertThat(capturedJobDetail.getValue().getJobDataMap().getLong("auctionToEndId")).isEqualTo(auctionToEndId);
        assertThat(capturedJobTrigger.getValue().getStartTime()).isEqualTo(expectedJobTriggerFireTime);
    }

    @Test
    void scheduleEndAuctionJobsTest() {
        Date jobTriggerFireTime = this.dateUtils.createDateFromNow(1000L);
        List<Long> auctionsToEndIds = Arrays.asList(new Long[]{0L, 1L, 2L, 3L, 4L, 5L});

        DynamicJobSchedulerService dynamicJobSchedulerServiceSpy = spy(this.dynamicJobSchedulerService);
        dynamicJobSchedulerServiceSpy.scheduleEndAuctionJobs(jobTriggerFireTime, auctionsToEndIds);

        ArgumentCaptor<Date> capturedJobTriggerFireTimes = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Long> capturedJobAuctionToEndIds = ArgumentCaptor.forClass(Long.class);

        verify(dynamicJobSchedulerServiceSpy, times(auctionsToEndIds.size())).scheduleEndAuctionJob(capturedJobTriggerFireTimes.capture(),
                                                                                              capturedJobAuctionToEndIds.capture());

        for (Date capturedJobTriggerFireDelay : capturedJobTriggerFireTimes.getAllValues())
            assertThat(capturedJobTriggerFireDelay).isEqualTo(jobTriggerFireTime);

        assertThat(capturedJobAuctionToEndIds.getAllValues()).isEqualTo(auctionsToEndIds);
    }

}
