package com.nordeus.jobfair.auctionservice.auctionservice.integration;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class DynamicJobSchedulerServiceImplIntegrationTests {

    @Autowired
    private DynamicJobSchedulerService dynamicJobSchedulerService;

    @Autowired
    private DateUtils dateUtils;

    @MockBean
    private AuctionService auctionService;

    @ParameterizedTest
    @ValueSource(longs = {0, 10, 13})
    void EndAuctionJobScheduleTest(Long auctionToEndId) {
        this.dynamicJobSchedulerService.scheduleEndAuctionJob(this.dateUtils.createDateFromNow(0L), auctionToEndId);

        ArgumentCaptor<Long> capturedAuctionToEndId = ArgumentCaptor.forClass(Long.class);
        await().atMost(1, TimeUnit.SECONDS)
                .untilAsserted(() -> verify(this.auctionService, times(1)).endAuction(capturedAuctionToEndId.capture()));

        assertThat(capturedAuctionToEndId.getValue()).isEqualTo(auctionToEndId);
    }
}
