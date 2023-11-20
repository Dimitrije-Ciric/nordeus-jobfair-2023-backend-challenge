package com.nordeus.jobfair.auctionservice.auctionservice.integration;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Bid;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionsUserRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.BidRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.util.DomainObjectsFactoryUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
public class BidRepositoryIntegrationTests {

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionsUserRepository auctionsUserRepository;

    @Autowired
    private DomainObjectsFactoryUtils domainObjectsFactoryUtils;

    @AfterEach
    void tearDown() {
        this.bidRepository.deleteAll();
        this.auctionRepository.deleteAll();
        this.auctionsUserRepository.deleteAll();
    }

    @Test
    void successfulBiddingTest() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true);
        this.auctionRepository.save(auction);

        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(124L);
        this.auctionsUserRepository.save(auctionsUser);

        Bid newBid = new Bid(auction, auctionsUser, 2);

        assertDoesNotThrow(() -> {
            this.bidRepository.save(newBid);
        });
    }

    @Test
    void whenBidding_onNonExistingAuction_thenInvalidDataAccessApiUsageExceptionThrown() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(124L);

        Bid newBid = new Bid(auction, auctionsUser, 15);

        assertThrows(InvalidDataAccessApiUsageException.class, () -> {
            this.bidRepository.save(newBid);
        });
    }

    @Test
    void given2SameBidsOnSameAuction_whenSaving_thenDataIntegrityViolationExceptionThrown() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true);
        this.auctionRepository.save(auction);

        AuctionsUser auctionsUser1 = this.domainObjectsFactoryUtils.auctionsUserFactory(124L),
                auctionsUser2 = this.domainObjectsFactoryUtils.auctionsUserFactory(15356L);
        this.auctionsUserRepository.save(auctionsUser1);
        this.auctionsUserRepository.save(auctionsUser2);

        Bid bid1 = new Bid(auction, auctionsUser1, 15),
            bid2 = new Bid(auction, auctionsUser2, 15);

        assertThrows(DataIntegrityViolationException.class, () -> {
            this.bidRepository.save(bid1);
            this.bidRepository.save(bid2);
        });
    }

    @Test
    void givenFewBidsOnSameAuction_whenFindFirstByAuctionIdOrderByBidValueDesc_thenReturnsLastBidPlaced() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true);
        this.auctionRepository.save(auction);

        AuctionsUser auctionsUser1 = this.domainObjectsFactoryUtils.auctionsUserFactory(124L),
                auctionsUser2 = this.domainObjectsFactoryUtils.auctionsUserFactory(15356L);
        this.auctionsUserRepository.save(auctionsUser1);
        this.auctionsUserRepository.save(auctionsUser2);

        Bid bid1 = new Bid(auction, auctionsUser1, 15),
                bid2 = new Bid(auction, auctionsUser2, 16);

        this.bidRepository.save(bid1);
        this.bidRepository.save(bid2);

        Bid lastBid = this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auction).get();
        assertThat(lastBid.getBidId()).isEqualTo(bid2.getBidId());
    }

    @Test
    void givenAuctionWithNoPlacedBids_whenFindFirstByAuctionIdOrderByBidValueDesc_thenNoResultPresent() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true);
        this.auctionRepository.save(auction);

        assertThat(this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auction).isPresent()).isEqualTo(false);
    }

}
