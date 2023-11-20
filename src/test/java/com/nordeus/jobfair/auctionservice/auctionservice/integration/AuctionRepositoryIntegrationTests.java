package com.nordeus.jobfair.auctionservice.auctionservice.integration;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.util.DomainObjectsFactoryUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AuctionRepositoryIntegrationTests {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private DomainObjectsFactoryUtils domainObjectsFactoryUtils;

    @Test
    @Transactional
    void updateAuctionTest() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 500L, 0L, 0L, 1);
        this.auctionRepository.save(auction);

        int updatedEntitiesCount = this.auctionRepository.updateAuction(auction.getAuctionId(), false, 600L, true, 500L);
        auction = this.auctionRepository.findById(auction.getAuctionId()).get();

        assertThat(updatedEntitiesCount).isEqualTo(1);
        assertFalse(auction.isActive());
        assertThat(auction.getEndTime().getTime()).isEqualTo(600L);
    }
}
