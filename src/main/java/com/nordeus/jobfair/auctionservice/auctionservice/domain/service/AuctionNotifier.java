package com.nordeus.jobfair.auctionservice.auctionservice.domain.service;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Bid;

import java.util.Collection;

public interface AuctionNotifier {

    void newAuctionsGenerated(Collection<Auction> newAuctions);

    void auctionEnded(Auction auction);

    void bidPlaced(Bid bid);

    void auctionProlonged(Auction auction);
}
