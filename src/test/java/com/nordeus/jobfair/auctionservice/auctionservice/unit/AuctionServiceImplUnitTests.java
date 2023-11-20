package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.AuctionDoesNotExists;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionNotifier;
import com.nordeus.jobfair.auctionservice.auctionservice.util.ConfigureBeansTestUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.AuctionEndedException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.InvalidBidValueException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Bid;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.BidRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import com.nordeus.jobfair.auctionservice.auctionservice.util.DomainObjectsFactoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AuctionServiceImplUnitTests {

    @Autowired
    private AuctionService auctionService;

    @MockBean
    private AuctionRepository auctionRepository;

    @MockBean
    private DynamicJobSchedulerService dynamicJobSchedulerService;

    @MockBean
    private BidRepository bidRepository;

    @MockBean
    private AuctionsUserService auctionsUserService;

    @MockBean
    private AuctionNotifier auctionNotifier;

    @MockBean
    private Clock clock;

    @Autowired
    private DateUtils dateUtils;

    @Autowired
    private DomainObjectsFactoryUtils domainObjectsFactoryUtils;

    @Autowired
    private ConfigureBeansTestUtils configureBeansTestUtils;

    @Test
    void getAllActiveTest() {
        List<Auction> activeAuctions = Arrays.asList(
                this.domainObjectsFactoryUtils.auctionFactory(true),
                this.domainObjectsFactoryUtils.auctionFactory(true));

        given(this.auctionRepository.findByActiveTrue()).willReturn(Optional.of(activeAuctions));

        assertThat(this.auctionService.getAllActive()).isEqualTo(activeAuctions);
        verify(this.auctionRepository, times(1)).findByActiveTrue();
    }

    @Test
    void givenValidAuctionId_whenGetAuction_thenAuctionReturned() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(1L, true, 0L, 0L);

        given(this.auctionRepository.findById(1L)).willReturn(Optional.of(auction));

        assertThat(this.auctionService.getAuction(1L)).isEqualTo(auction);
    }

    @Test
    void givenInvalidAuctionId_whenGetAuction_thenAuctionDoesNotExists() {
        given(this.auctionRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(AuctionDoesNotExists.class, () -> this.auctionService.getAuction(1L) );
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 10, 15})
    void givenNewAuctionCount_whenStartNewAuctions_thenNewAuctionsStarted(Integer newAuctionsCount) {
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, newAuctionsCount, 0L, 0L, 0L, 0);

        this.auctionService.startNewAuctions();

        ArgumentCaptor<List<Auction>> capturedNewAuctions = ArgumentCaptor.forClass(List.class);
        verify(this.auctionRepository, times(1)).saveAll(capturedNewAuctions.capture());
        assertThat(capturedNewAuctions.getValue().size()).isEqualTo(newAuctionsCount);
    }

    @ParameterizedTest
    @ValueSource(longs = {1000, 60000})
    void givenAuctionsRefreshDelay_whenStartNewAuction_thenValidNewAuctionsStartEndTime(Long auctionsRefreshDelay) {
        when(clock.millis()).thenReturn(1000L).thenReturn(1003L);
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 3, auctionsRefreshDelay, 0L, 0L, 0);
        Timestamp expectedStartTime = new Timestamp(1000L),
                    expectedEndTime = new Timestamp(expectedStartTime.getTime() + auctionsRefreshDelay);

        this.auctionService.startNewAuctions();

        ArgumentCaptor<List<Auction>> capturedNewAuctions = ArgumentCaptor.forClass(List.class);
        verify(this.auctionRepository, times(1)).saveAll(capturedNewAuctions.capture());

        for (Auction capturedNewAuction : capturedNewAuctions.getValue()) {
            assertThat(capturedNewAuction.getStartTime()).isEqualTo(expectedStartTime);
            assertThat(capturedNewAuction.getEndTime()).isEqualTo(expectedEndTime);
        }
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 5000})
    void givenAuctionsProlongedTimeDuration_whenStartNewAuction_thenValidNewAuctionsProlongedTimeDuration(Long auctionsProlongedTimeDuration) {
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 3, 0L, 0L, auctionsProlongedTimeDuration, 0);

        this.auctionService.startNewAuctions();

        ArgumentCaptor<List<Auction>> capturedNewAuctions = ArgumentCaptor.forClass(List.class);
        verify(this.auctionRepository, times(1)).saveAll(capturedNewAuctions.capture());

        for (Auction capturedNewAuction : capturedNewAuctions.getValue())
            assertThat(capturedNewAuction.getProlongedTimeDurationMillis()).isEqualTo(auctionsProlongedTimeDuration);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 5000})
    void givenAuctionsFinalBidPeriodDuration_whenStartNewAuction_thenValidNewAuctionsFinalBidPeriodDuration(Long finalBidPeriodDuration) {
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 3, 0L, finalBidPeriodDuration, 0L, 0);

        this.auctionService.startNewAuctions();

        ArgumentCaptor<List<Auction>> capturedNewAuctions = ArgumentCaptor.forClass(List.class);
        verify(this.auctionRepository, times(1)).saveAll(capturedNewAuctions.capture());

        for (Auction capturedNewAuction : capturedNewAuctions.getValue())
            assertThat(capturedNewAuction.getFinalBidPeriodDurationMillis()).isEqualTo(finalBidPeriodDuration);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 1000, 60000})
    void givenAuctionsRefreshDelay_whenStartNewAuctions_thenStartNewAuctionsJobScheduled(Long nextAuctionsRefreshDelay) {
        given(clock.millis()).willReturn(0L);

        Date nextAuctionsRefreshTime = this.dateUtils.createDateFromNow(nextAuctionsRefreshDelay);
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 5, nextAuctionsRefreshDelay, 0L, 0L, 0);

        this.auctionService.startNewAuctions();


        ArgumentCaptor<Date> capturedStartNewAuctionsJobTriggerTime = ArgumentCaptor.forClass(Date.class);
        verify(this.dynamicJobSchedulerService, times(1)).scheduleStartNewAuctionsJob(capturedStartNewAuctionsJobTriggerTime.capture());
        assertThat(capturedStartNewAuctionsJobTriggerTime.getValue()).isEqualTo(nextAuctionsRefreshTime);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void givenAuctionsInitialBidValue_whenStartNewAuctions_thenAuctionInitialBidValueSet(Integer initialBidValue) {
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, 3, 0L, 0L, 0L, initialBidValue);

        this.auctionService.startNewAuctions();

        ArgumentCaptor<List<Auction>> capturedNewAuctions = ArgumentCaptor.forClass(List.class);
        verify(this.auctionRepository, times(1)).saveAll(capturedNewAuctions.capture());

        for (Auction capturedNewAuction : capturedNewAuctions.getValue())
            assertThat(capturedNewAuction.getInitialBidValue()).isEqualTo(initialBidValue);
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 10, 15})
    void givenNewAuctionsCount_whenStartNewAuctions_thenLoggedEachNewAuction(Integer newAuctionsCount) {
        this.configureBeansTestUtils.configureAuctionService(this.auctionService, newAuctionsCount, 0L, 0L, 0L, 0);

        this.auctionService.startNewAuctions();

        ArgumentCaptor<Collection<Auction>> capturedNewAuctionsGeneratedToLog = ArgumentCaptor.forClass(Collection.class);
        verify(this.auctionNotifier, times(1)).newAuctionsGenerated(capturedNewAuctionsGeneratedToLog.capture());
        assertThat(capturedNewAuctionsGeneratedToLog.getValue().size()).isEqualTo(newAuctionsCount);
    }

    @ParameterizedTest
    @ValueSource(longs = {0, 3, 10})
    void givenFinishedAuctionToEndId_whenEndAuction_thenAuctionNotActive(Long auctionId) {
        Auction mockedAuction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 450L, 0L, 0L),
                expectedEndedAuction = this.domainObjectsFactoryUtils.auctionFactory(false, 0L, 450L, 0L, 0L);
        mockedAuction.setAuctionId(auctionId);
        expectedEndedAuction.setAuctionId(auctionId);

        given(this.clock.millis()).willReturn(500L);
        given(this.auctionRepository.findById(auctionId)).willReturn(Optional.of(mockedAuction));
        this.auctionService.endAuction(auctionId);

        ArgumentCaptor<Long> capturedAuctionToEndId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> capturedNewActiveStatus = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Long> capturedNewEndTime = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Boolean> capturedOldActiveStatus = ArgumentCaptor.forClass(Boolean.class);
        ArgumentCaptor<Long> capturedOldEndTime = ArgumentCaptor.forClass(Long.class);
        verify(this.auctionRepository, times(1)).updateAuction(capturedAuctionToEndId.capture(),
                                                                                        capturedNewActiveStatus.capture(),
                                                                                        capturedNewEndTime.capture(),
                                                                                        capturedOldActiveStatus.capture(),
                                                                                        capturedOldEndTime.capture());

        assertThat(capturedAuctionToEndId.getValue()).isEqualTo(expectedEndedAuction.getAuctionId());
        assertThat(capturedNewActiveStatus.getValue()).isEqualTo(expectedEndedAuction.isActive());
        assertThat(capturedNewEndTime.getValue()).isEqualTo(expectedEndedAuction.getEndTime().getTime());
        assertThat(capturedOldActiveStatus.getValue()).isEqualTo(mockedAuction.isActive());
        assertThat(capturedOldEndTime.getValue()).isEqualTo(mockedAuction.getEndTime().getTime());
    }

    @Test
    void givenNotFinishedAuction_whenEndAuction_thenAuctionUnchanged() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L);

        given(this.clock.millis()).willReturn(700L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.endAuction(auction.getAuctionId());

        ArgumentCaptor<Long> capturedAuctionId = ArgumentCaptor.forClass(Long.class);
        verify(this.auctionRepository, times(1)).findById(capturedAuctionId.capture());
        verifyNoMoreInteractions(this.auctionRepository);

        assertThat(capturedAuctionId.getValue()).isEqualTo(auction.getAuctionId());
    }

    @Test
    void givenFinishedAuction_whenEndAuction_thenAuctionEndLogged() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 500L, 400L, 500L);

        given(this.clock.millis()).willReturn(700L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));

        this.auctionService.endAuction(auction.getAuctionId());

        ArgumentCaptor<Auction> capturedAuctionEndedToLog = ArgumentCaptor.forClass(Auction.class);
        verify(this.auctionNotifier, times(1)).auctionEnded(capturedAuctionEndedToLog.capture());

        assertThat(capturedAuctionEndedToLog.getValue().getAuctionId()).isEqualTo(auction.getAuctionId());
    }

    @Test
    void givenStartingAuction_whenBid_thenValidBidSaved() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Bid> capturedNewBid = ArgumentCaptor.forClass(Bid.class);
        verify(this.bidRepository, times(1)).save(capturedNewBid.capture());
        assertThat(capturedNewBid.getValue().getBidValue()).isEqualTo(1);
        assertThat(capturedNewBid.getValue().getAuction()).isEqualTo(auction);
        assertThat(capturedNewBid.getValue().getAuctionsUser()).isEqualTo(auctionsUser);
    }

    @Test
    void givenStartingAuction_whenBid_thenAuctionsUserTokensRemoved() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Long> capturedAuctionsUserId = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> capturedBidValue = ArgumentCaptor.forClass(Integer.class);
        verify(this.auctionsUserService, times(1)).removeTokens(capturedAuctionsUserId.capture(),
                                                                                capturedBidValue.capture());
        assertThat(capturedAuctionsUserId.getValue()).isEqualTo(auctionsUser.getAuctionsUserId());
        assertThat(capturedBidValue.getValue()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void givenInitialBidValue_whenPlacingFirstBid_thenSavedBidWithInitialValue(Integer initialBidValue) {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, initialBidValue);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(initialBidValue, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Bid> capturedNewBid = ArgumentCaptor.forClass(Bid.class);
        verify(this.bidRepository, times(1)).save(capturedNewBid.capture());
        assertThat(capturedNewBid.getValue().getBidValue()).isEqualTo(initialBidValue);
    }

    @Test
    void givenWrongBidValue_whenPlacingFirstBid_thenInvalidBidValueExceptionThrown() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));

        assertThrows(InvalidBidValueException.class,() -> {
            this.auctionService.bid(2, auction.getAuctionId(), auctionsUser);
        });
    }

    @Test
    void givenStartingAuction_whenBid_thenAuctionUnchanged() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Auction> capturedAuctionToSave = ArgumentCaptor.forClass(Auction.class);
        verify(this.auctionRepository, never()).save(capturedAuctionToSave.capture());
    }

    @Test
    void givenStartingAuction_whenBid_thenAuctionEndJobNotScheduled() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        verifyNoInteractions(this.dynamicJobSchedulerService);
    }

    @Test
    void givenLastlyPlacedBid_whenBidWithIncrementedBidValue_thenNewBidSaved() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);
        Bid lastBid = this.domainObjectsFactoryUtils.bidFactory(auction, auctionsUser, 1);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        given(this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auction)).willReturn(Optional.of(lastBid));

        this.auctionService.bid(2, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Bid> capturedNewBid = ArgumentCaptor.forClass(Bid.class);
        verify(this.bidRepository, times(1)).save(capturedNewBid.capture());
        assertThat(capturedNewBid.getValue().getBidValue()).isEqualTo(2);
        assertThat(capturedNewBid.getValue().getAuction()).isEqualTo(auction);
        assertThat(capturedNewBid.getValue().getAuctionsUser()).isEqualTo(auctionsUser);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 1, 3, 10})
    void givenLastlyPlacedBid_whenBidWithInvalidBidValue_thenInvalidBidValueThrown(Integer newBidValue) {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);
        Bid lastBid = this.domainObjectsFactoryUtils.bidFactory(auction, auctionsUser, 1);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        given(this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auction)).willReturn(Optional.of(lastBid));

        assertThrows(InvalidBidValueException.class, () -> {
            this.auctionService.bid(newBidValue, auction.getAuctionId(), auctionsUser);
        });
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 10})
    void givenLastlyPlacedBid_whenBid_thenLastBidOwnerReclaimTokens(Integer lastBidValue) {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser lastBidOwner = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);
        Bid lastBid = this.domainObjectsFactoryUtils.bidFactory(auction, lastBidOwner, lastBidValue);

        given(this.clock.millis()).willReturn(100L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        given(this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auction)).willReturn(Optional.of(lastBid));

        Integer newBidValue = lastBidValue + 1;
        AuctionsUser newBidOwner = this.domainObjectsFactoryUtils.auctionsUserFactory(21364L);
        this.auctionService.bid(newBidValue, auction.getAuctionId(), newBidOwner);

        ArgumentCaptor<Long> auctionsUserIdToReclaimTokens = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Integer> tokensReclaimed = ArgumentCaptor.forClass(Integer.class);
        verify(this.auctionsUserService, times(1)).addTokens(auctionsUserIdToReclaimTokens.capture(),
                                                                                        tokensReclaimed.capture());
        assertThat(auctionsUserIdToReclaimTokens.getValue()).isEqualTo(lastBidOwner.getAuctionsUserId());
        assertThat(tokensReclaimed.getValue()).isEqualTo(lastBidValue);
    }

    @Test
    void givenEndingAuction_whenBid_thenAuctionProlonged() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(700L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Auction> capturedProlongedAuction = ArgumentCaptor.forClass(Auction.class);
        verify(this.auctionRepository, times(1)).save(capturedProlongedAuction.capture());
        assertThat(capturedProlongedAuction.getValue().isActive()).isEqualTo(true);
        assertThat(capturedProlongedAuction.getValue().getEndTime().getTime()).isEqualTo(700L + 500L);
    }

    @Test
    void givenEndingAuction_whenBid_thenEndAuctionJobRescheduled() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(700L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Date> capturedEndAuctionJobTriggerTime = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Long> capturedAuctionToEndId = ArgumentCaptor.forClass(Long.class);
        verify(this.dynamicJobSchedulerService, times(1)).scheduleEndAuctionJob(capturedEndAuctionJobTriggerTime.capture(),
                                                                                                    capturedAuctionToEndId.capture());
        assertThat(capturedEndAuctionJobTriggerTime.getValue().getTime()).isEqualTo(700L + 500L);
        assertThat(capturedAuctionToEndId.getValue()).isEqualTo(auction.getAuctionId());
    }

    @Test
    void givenEndedAuction_whenBid_thenAuctionEndedExceptionThrown() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(false);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));

        assertThrows(AuctionEndedException.class, () -> {
            this.auctionService.bid(2, auction.getAuctionId(), auctionsUser);
        });
    }

    @Test
    void givenInvalidAuctionId_whenBid_thenAuctionDoesNotExistsThrown() {
        given(this.auctionRepository.findById(1356L)).willReturn(Optional.empty());

        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(345L);
        assertThrows(AuctionDoesNotExists.class, () -> {
            this.auctionService.bid(2, 1356L, auctionsUser);
        });
    }

    @Test
    void givenAuction_whenBid_thenNewBidLogged() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 0L, 0L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(700L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
        this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Bid> capturedNewBidToLog = ArgumentCaptor.forClass(Bid.class);
        verify(this.auctionNotifier, times(1)).bidPlaced(capturedNewBidToLog.capture());
        assertThat(capturedNewBidToLog.getValue().getAuction().getAuctionId()).isEqualTo(auction.getAuctionId());
        assertThat(capturedNewBidToLog.getValue().getAuctionsUser().getAuctionsUserId()).isEqualTo(auctionsUser.getAuctionsUserId());
    }

    @Test
    void givenAuction_whenBidProlongsAuction_thenBidProlongedLogged() {
        Auction auction = this.domainObjectsFactoryUtils.auctionFactory(true, 0L, 1000L, 400L, 500L, 1);
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(1356L);

        given(this.clock.millis()).willReturn(700L);
        given(this.auctionRepository.findById(auction.getAuctionId())).willReturn(Optional.of(auction));
            this.auctionService.bid(1, auction.getAuctionId(), auctionsUser);

        ArgumentCaptor<Auction> capturedProlongedAuctionToLog = ArgumentCaptor.forClass(Auction.class);
        verify(this.auctionNotifier, times(1)).auctionProlonged(capturedProlongedAuctionToLog.capture());
        assertThat(capturedProlongedAuctionToLog.getValue().getAuctionId()).isEqualTo(auction.getAuctionId());
        assertThat(capturedProlongedAuctionToLog.getValue().isActive()).isEqualTo(true);
        assertThat(capturedProlongedAuctionToLog.getValue().getEndTime().getTime()).isEqualTo(700L + 500L);
    }

}
