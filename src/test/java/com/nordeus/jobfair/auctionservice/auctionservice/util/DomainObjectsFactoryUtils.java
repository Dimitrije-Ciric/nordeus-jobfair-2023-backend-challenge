package com.nordeus.jobfair.auctionservice.auctionservice.util;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Bid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.*;

@Component
@AllArgsConstructor
public class DomainObjectsFactoryUtils {

    private final Clock clock;

    public Auction auctionWithBidsFactory(Long auctionId) {
        Auction auction = this.auctionFactory(auctionId, true, 0L, 1000L);

        AuctionsUser auctionsUser = this.auctionsUserFactory(124624L);
        auctionsUser.setAuctionsUserId(65484L);

        auction.setBids(new LinkedHashSet<>(List.of(new Bid[]{
                this.bidFactory(auction, auctionsUser, 1),
                this.bidFactory(auction, auctionsUser, 2),
                this.bidFactory(auction, auctionsUser, 3)
        })));

        return auction;
    }

    public Auction auctionFactory(Long auctionId,
                                  boolean active,
                                  Long startTimeMillis,
                                  Long endTimeMillis) {
        Auction auction = this.auctionFactory(true);
        auction.setAuctionId(auctionId);
        auction.setActive(active);
        auction.setStartTime(new Timestamp(startTimeMillis));
        auction.setEndTime(new Timestamp(endTimeMillis));

        return auction;
    }

    public Auction auctionFactory(boolean active,
                                  Long startTimeMillis,
                                  Long endTimeMillis,
                                  Long finalBidPeriodDurationMillis,
                                  Long prolongedTimeDurationMillis) {
        Auction auction = this.auctionFactory(active);
        auction.setStartTime(new Timestamp(startTimeMillis));
        auction.setEndTime(new Timestamp(endTimeMillis));
        auction.setFinalBidPeriodDurationMillis(finalBidPeriodDurationMillis);
        auction.setProlongedTimeDurationMillis(prolongedTimeDurationMillis);

        return auction;
    }

    public Auction auctionFactory(boolean active,
                                  Long startTimeMillis,
                                  Long endTimeMillis,
                                  Long finalBidPeriodDurationMillis,
                                  Long prolongedTimeDurationMillis,
                                  Integer initialBidValue) {
        Auction auction = this.auctionFactory(active);
        auction.setStartTime(new Timestamp(startTimeMillis));
        auction.setEndTime(new Timestamp(endTimeMillis));
        auction.setFinalBidPeriodDurationMillis(finalBidPeriodDurationMillis);
        auction.setProlongedTimeDurationMillis(prolongedTimeDurationMillis);
        auction.setInitialBidValue(initialBidValue);

        return auction;
    }

    public Auction auctionFactory(boolean active) {
        Timestamp startDate = new Timestamp((new Date()).getTime()),
             endDate = new Timestamp((new Date()).getTime());

        return new Auction(active, startDate, endDate, 0L, 0L, 0);
    }

    public AuctionsUser auctionsUserFactory(Long userId, Integer tokensCount) {
        AuctionsUser auctionsUser = this.auctionsUserFactory(userId);
        auctionsUser.setTokens(tokensCount);

        return auctionsUser;
    }

    public AuctionsUser auctionsUserFactory(Long userId) {
        Map<String, Object> userDetails = new HashMap<>();

        return new AuctionsUser(userId, userDetails, 40);
    }

    public Bid bidFactory(Auction auction, AuctionsUser auctionsUser, Integer bidValue) {
        return new Bid(auction, auctionsUser, bidValue);
    }

}
