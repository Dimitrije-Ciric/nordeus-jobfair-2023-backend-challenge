package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.job.EndAuctionJob;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EndAuctionJobUnitTests {

    @Autowired
    private EndAuctionJob endAuctionJob;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private JobExecutionContext context;

    @ParameterizedTest
    @ValueSource(longs = {0, 10, 34})
    void testExecuteInternal(Long auctionToEndId) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method executeInternal = EndAuctionJob.class.getDeclaredMethod("executeInternal", JobExecutionContext.class);
        executeInternal.setAccessible(true);

        JobDataMap mockedMergedJobDataMap = new JobDataMap();
        mockedMergedJobDataMap.put("auctionToEndId", auctionToEndId);

        given(this.context.getMergedJobDataMap()).willReturn(mockedMergedJobDataMap);
        executeInternal.invoke(this.endAuctionJob, this.context);

        ArgumentCaptor<Long> capturedAuctionToEndId = ArgumentCaptor.forClass(Long.class);
        verify(this.auctionService, times(1)).endAuction(capturedAuctionToEndId.capture());
        assertThat(capturedAuctionToEndId.getValue()).isEqualTo(auctionToEndId);
    }

}
