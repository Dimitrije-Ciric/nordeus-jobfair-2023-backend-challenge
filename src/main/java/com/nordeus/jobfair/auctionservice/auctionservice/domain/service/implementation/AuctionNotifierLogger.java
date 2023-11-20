package com.nordeus.jobfair.auctionservice.auctionservice.domain.service.implementation;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Bid;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionNotifier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuctionNotifierLogger implements AuctionNotifier {

    @Override
    public void newAuctionsGenerated(Collection<Auction> newAuctions) {
        List<Long> newAuctionIds = newAuctions.stream().map(Auction::getAuctionId).collect(Collectors.toList());
        log.info("New auctions generated with IDs: {}", newAuctionIds);
    }

    @Override
    public void auctionEnded(Auction auction) {
        log.info("Auction finished(ID): {}", auction.getAuctionId());
    }

    @Override
    public void bidPlaced(Bid bid) {
        log.info("Bid placed(ID): {}", bid.getBidId());
    }

    @Override
    public void auctionProlonged(Auction auction) {
        log.info("Auction prolonged(ID): {}", auction.getAuctionId());
    }
}
