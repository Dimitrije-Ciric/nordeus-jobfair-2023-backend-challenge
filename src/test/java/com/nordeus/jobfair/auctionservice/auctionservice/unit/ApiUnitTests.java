package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.AuctionDoesNotExists;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import com.nordeus.jobfair.auctionservice.auctionservice.util.DomainObjectsFactoryUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;


import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class ApiUnitTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuctionService auctionService;

    @MockBean
    private AuctionsUserService auctionsUserService;

    @Autowired
    private DomainObjectsFactoryUtils domainObjectsFactoryUtils;

    private final String validJwt = "{\"payload\":{\"user_id\": 134,\"user_details\":{\"user_name\":\"RANDOM USERNAME\"}}}";

    private final String authHeader = String.format("Bearer %s", this.validJwt);

    @Test
    void givenNoActiveAuctions_whenGetAllActiveAuctionsTest_thenJsonEmptyArray() throws Exception {
        given(this.auctionService.getAllActive()).willReturn(new LinkedList<>());

        this.mockMvc.perform(get("/auctions/active").header("Authorization", this.authHeader)).andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    void givenFewActiveAuctions_whenGetAllActiveAuctionsTest_thenJsonActiveAuctions() throws Exception {
        given(this.auctionService.getAllActive()).willReturn(
                Arrays.asList(
                        this.domainObjectsFactoryUtils.auctionWithBidsFactory(1L),
                        this.domainObjectsFactoryUtils.auctionWithBidsFactory(2L),
                        this.domainObjectsFactoryUtils.auctionWithBidsFactory(3L)
                )
        );

        String expectedJson = "[{\"auctionId\":1,\"active\":true,\"startTime\":\"1970-01-01T00:00:00.000+00:00\",\"endTime\":\"1970-01-01T00:00:01.000+00:00\",\"prolongedTimeDurationMillis\":0,\"initialBidValue\":0,\"bids\":[{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":1,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":2,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":3,\"createdAt\":null}],\"finalBidPeriodDurationMillis\":0},{\"auctionId\":2,\"active\":true,\"startTime\":\"1970-01-01T00:00:00.000+00:00\",\"endTime\":\"1970-01-01T00:00:01.000+00:00\",\"prolongedTimeDurationMillis\":0,\"initialBidValue\":0,\"bids\":[{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":1,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":2,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":3,\"createdAt\":null}],\"finalBidPeriodDurationMillis\":0},{\"auctionId\":3,\"active\":true,\"startTime\":\"1970-01-01T00:00:00.000+00:00\",\"endTime\":\"1970-01-01T00:00:01.000+00:00\",\"prolongedTimeDurationMillis\":0,\"initialBidValue\":0,\"bids\":[{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":1,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":2,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":3,\"createdAt\":null}],\"finalBidPeriodDurationMillis\":0}]";
        this.mockMvc.perform(get("/auctions/active").header("Authorization", this.authHeader)).andExpect(status().isOk())
                .andExpect(content().string(expectedJson));
    }

    @Test
    void givenAuctionId_whenGetAuction_thenJsonAuction() throws Exception {
        given(this.auctionService.getAuction(5L)).willReturn(this.domainObjectsFactoryUtils.auctionWithBidsFactory(1L));

        String expectedJson = "{\"auctionId\":1,\"active\":true,\"startTime\":\"1970-01-01T00:00:00.000+00:00\",\"endTime\":\"1970-01-01T00:00:01.000+00:00\",\"prolongedTimeDurationMillis\":0,\"initialBidValue\":0,\"bids\":[{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":1,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":2,\"createdAt\":null},{\"bidId\":null,\"auctionsUser\":{\"auctionsUserId\":65484,\"userId\":124624,\"userDetails\":{},\"tokens\":40},\"bidValue\":3,\"createdAt\":null}],\"finalBidPeriodDurationMillis\":0}";
        this.mockMvc.perform(get("/auctions/5").header("Authorization", this.authHeader)).andExpect(status().isOk())
                .andExpect(content().string(expectedJson));
    }

    @Test
    void givenInvalidAuctionId_whenGetAuction_thenAuctionDoesNotExistsThrown() throws Exception {
        given(this.auctionService.getAuction(5L)).willThrow(AuctionDoesNotExists.class);

        this.mockMvc.perform(get("/auctions/5").header("Authorization", this.authHeader)).andExpect(status().isBadRequest());
    }

    @Test
    void givenNoJwtToken_whenApiCall_thenUnauthorizedStatusReturned() throws Exception {
        this.mockMvc.perform(get("/auctions/5")).andExpect(status().isUnauthorized());
    }

    @Test
    void givenBasicAuthToken_whenApiCall_thenUnauthorizedStatusReturned() throws Exception {
        this.mockMvc.perform(get("/auctions/5").header("Authorization", "Basic X123.31513")).andExpect(status().isUnauthorized());
    }

    @Test
    void givenInvalidJwt_whenApiCall_thenUnauthorizedStatusReturned() throws Exception {
        this.mockMvc.perform(get("/auctions/5").header("Authorization", "Bearer {}")).andExpect(status().isUnauthorized());
    }

    @Test
    void givenValidJwt_whenApiCall_thenUserDataUpdated() throws Exception {
        this.mockMvc.perform(get("/auctions/5").header("Authorization", this.authHeader)).andExpect(status().isOk());

        ArgumentCaptor<Long> capturedUserToUpdateId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Map<String, Object>> capturedNewUserDetails = ArgumentCaptor.forClass(Map.class);
        verify(this.auctionsUserService, times(1)).updateUser(capturedUserToUpdateId.capture(),
                                                                                        capturedNewUserDetails.capture());
    }

}
