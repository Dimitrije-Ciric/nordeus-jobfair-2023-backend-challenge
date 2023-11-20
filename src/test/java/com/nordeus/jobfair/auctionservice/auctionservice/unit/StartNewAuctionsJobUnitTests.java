package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.job.StartNewAuctionsJob;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import org.junit.jupiter.api.Test;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class StartNewAuctionsJobUnitTests {

    @Autowired
    private StartNewAuctionsJob startNewAuctionsJob;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private JobExecutionContext context;

    @Test
    void testExecuteInternal() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Method executeInternal = StartNewAuctionsJob.class.getDeclaredMethod("executeInternal", JobExecutionContext.class);
        executeInternal.setAccessible(true);

        executeInternal.invoke(this.startNewAuctionsJob, this.context);

        verify(this.auctionService, times(1)).startNewAuctions();
    }

}
