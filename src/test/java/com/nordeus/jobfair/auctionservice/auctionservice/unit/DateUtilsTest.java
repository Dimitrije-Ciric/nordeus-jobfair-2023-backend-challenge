package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.Clock;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class DateUtilsTest {

    @Autowired
    private DateUtils dateUtils;

    @MockBean
    private Clock clock;

    @Test
    void createDateFromNowTest() {
        given(clock.millis()).willReturn(2000L);

        Date date = this.dateUtils.createDateFromNow(1000L);

        assertThat(date.getTime()).isEqualTo(3000L);
    }
}
