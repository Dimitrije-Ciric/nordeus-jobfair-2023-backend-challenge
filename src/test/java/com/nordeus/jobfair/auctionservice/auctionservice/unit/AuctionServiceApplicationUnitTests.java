package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.api.HttpController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AuctionServiceApplicationUnitTests {

    @Autowired
    private HttpController httpController;

    @Test
    void contextLoads() {
        assertThat(this.httpController).isNotNull();
    }

}
