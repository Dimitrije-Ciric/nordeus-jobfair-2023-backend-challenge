package com.nordeus.jobfair.auctionservice.auctionservice.integration;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.AuctionEndedException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Bid;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionsUserRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.BidRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import com.nordeus.jobfair.auctionservice.auctionservice.startup.AppStartupRunner;
import com.nordeus.jobfair.auctionservice.auctionservice.util.ConfigureBeansTestUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.util.DomainObjectsFactoryUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class SubcutaneousIntegrationTests {

    @Autowired
    private AppStartupRunner appStartupRunner;

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionsUserRepository auctionsUserRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private DomainObjectsFactoryUtils domainObjectsFactoryUtils;

    @Autowired
    private ConfigureBeansTestUtils configureBeansTestUtils;

    @BeforeEach
    void setUp() {}

    @AfterEach
    void tearDown() throws SchedulerException {
        this.bidRepository.deleteAll();
        this.auctionsUserRepository.deleteAll();
        this.auctionRepository.deleteAll();
        this.scheduler.clear();
    }

    @Test
    void startEndAuctionsCycleTest() {
        assertThat(this.auctionRepository.count()).isEqualTo(0);

        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 5, 500L, 50L, 70L, 1);
        this.configureBeansTestUtils.configureAppStartupRunner(this.appStartupRunner, true, 0L);
        this.appStartupRunner.run(null);

        await().atMost(110L, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertThat(this.auctionService.getAllActive().size()).isEqualTo(5);
                } );

        Collection<Auction> lastActiveAuctions = this.auctionService.getAllActive();
        assertThat(lastActiveAuctions.size()).isEqualTo(5);

        await().atMost(500L, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    for (Auction auction : lastActiveAuctions)
                        assertFalse(this.auctionService.getAuction(auction.getAuctionId()).isActive());
                } );
    }

    @Test
    void givenActiveAuctionWithoutFinalBidPeriod_whenSpamBidOnEnding_thenAuctionFinished() {
        assertThat(this.auctionRepository.count()).isEqualTo(0);

        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 5, 500L, 0L, 0L, 1);
        this.configureBeansTestUtils.configureAppStartupRunner(this.appStartupRunner, true, 0L);
        this.appStartupRunner.run(null);

        await().atMost(130L, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> assertThat(this.auctionService.getAllActive().size()).isEqualTo(5) );

        List<Auction> lastActiveAuctions = this.auctionService.getAllActive().stream().toList();
        Auction auctionToBid = lastActiveAuctions.get(0);

        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(12315L);
        this.auctionsUserRepository.save(auctionsUser);

        AtomicInteger lastBidValue = new AtomicInteger();
        AuctionsUser finalAuctionsUser = auctionsUser;
        await().atMost(500L, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    assertThrows(AuctionEndedException.class, () -> {
                        this.auctionService.bid(lastBidValue.incrementAndGet(), auctionToBid.getAuctionId(), finalAuctionsUser);
                    });
                } );

        Auction endedAuction = this.auctionService.getAuction(auctionToBid.getAuctionId());
        Long endedAuctionDurationMillis = endedAuction.getEndTime().getTime() - endedAuction.getStartTime().getTime();
        assertThat(endedAuctionDurationMillis).isEqualTo(500L);
        assertFalse(endedAuction.isActive());

        Bid lastBidPlaced = this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auctionToBid).get();
        assertTrue(lastBidPlaced.getCreatedAt().getTime() <= auctionToBid.getEndTime().getTime());

        auctionsUser = this.auctionsUserRepository.findById(auctionsUser.getAuctionsUserId()).get();
        assertThat(auctionsUser.getTokens()).isEqualTo(40 - lastBidValue.get() + 1);
    }

}
