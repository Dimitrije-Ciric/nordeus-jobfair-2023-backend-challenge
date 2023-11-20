package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class AuctionsUserModelUnitTests {

    @Test
    void structureTest() {
        Map<String, Object> newUserDetails = new HashMap<>();
        newUserDetails.put("username", "Winner123");

        AuctionsUser newAuctionsUser = new AuctionsUser(15L, newUserDetails, 40);

        assertThat(newAuctionsUser.getUserDetails().get("username")).isEqualTo("Winner123");
    }
}
