package com.nordeus.jobfair.auctionservice.auctionservice.domain;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;

import java.util.Collection;

public interface AuctionService {

    Collection<Auction> getAllActive();

    Auction getAuction(Long auctionId);

    void startNewAuctions();

    void endAuction(Long auctionId);

    void join(Long auctionId, AuctionsUser auctionsUser);

    void bid(Integer bidValue, Long auctionId, AuctionsUser auctionsUser);
}
