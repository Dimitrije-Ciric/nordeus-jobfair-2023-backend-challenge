package com.nordeus.jobfair.auctionservice.auctionservice.domain;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.AuctionDoesNotExists;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.AuctionEndedException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.InvalidBidValueException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.*;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.BidRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionNotifier;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.DynamicJobSchedulerService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.DateUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Clock;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;

    private final AuctionNotifier auctionNotifier;
    private final AuctionsUserService auctionsUserService;
    private final DynamicJobSchedulerService dynamicJobSchedulerService;

    private final DateUtils dateUtils;
    private final Clock clock;

    @Value("${domain.startNewAuctionsCount}")
    private int startNewAuctionsCount;

    @Value("${domain.auctionsRefreshDelay}")
    private long auctionsRefreshDelay;

    @Value("${domain.auctionsFinalBidPeriodDuration}")
    private long auctionsFinalBidPeriodDuration;

    @Value("${domain.auctionsProlongedTimeDuration}")
    private long auctionsProlongedTimeDuration;

    @Value("${domain.auctionsInitialBidValue}")
    private int auctionsInitialBidValue;

    @Override
    @Transactional(readOnly = true)
    public Collection<Auction> getAllActive() {
        return this.auctionRepository.findByActiveTrue().get();
    }

    @Override
    @Transactional(readOnly = true)
    public Auction getAuction(Long auctionId) {
        Optional<Auction> auction = this.auctionRepository.findById(auctionId);

        if (auction.isEmpty())
            throw new AuctionDoesNotExists();

        return auction.get();
    }

    @Override
    @Transactional
    public void startNewAuctions() {
        Timestamp startTime = new Timestamp(this.clock.millis()),
                    endTime = new Timestamp(startTime.getTime() + this.auctionsRefreshDelay);

        List<Auction> newAuctions = new LinkedList<>();
        for (int i = 0; i < this.startNewAuctionsCount; i++)
            newAuctions.add(new Auction(true, startTime, endTime, this.auctionsFinalBidPeriodDuration, this.auctionsProlongedTimeDuration, this.auctionsInitialBidValue));

        this.auctionRepository.saveAll(newAuctions);

        Date auctionsRefreshAtTime = this.dateUtils.createDateFromNow(this.auctionsRefreshDelay);
        this.dynamicJobSchedulerService.scheduleEndAuctionJobs(auctionsRefreshAtTime, newAuctions.stream()
                                                                                    .map(Auction::getAuctionId)
                                                                                    .collect(toList()));
        this.dynamicJobSchedulerService.scheduleStartNewAuctionsJob(auctionsRefreshAtTime);

        this.auctionNotifier.newAuctionsGenerated(newAuctions);
    }

    @Override
    @Transactional
    public void endAuction(Long auctionToEndId) {
        Long currentTimeMillis = this.clock.millis();
        Auction auctionToEnd = this.auctionRepository.findById(auctionToEndId).get();

        if (currentTimeMillis >= auctionToEnd.getEndTime().getTime()) {
            this.auctionRepository.updateAuction(auctionToEndId, false, auctionToEnd.getEndTime().getTime(), true, auctionToEnd.getEndTime().getTime());
            this.auctionNotifier.auctionEnded(auctionToEnd);
        }
    }

    @Override
    public void join(Long auctionId, AuctionsUser auctionsUser) {

    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void bid(Integer bidValue, Long auctionId, AuctionsUser auctionsUser) {
        Long currentTimeMillis = this.clock.millis();

        Optional<Auction> auctionO = this.auctionRepository.findById(auctionId);
        if (auctionO.isEmpty())
            throw new AuctionDoesNotExists();

        Auction auction = auctionO.get();

        if (!auction.isActive() || currentTimeMillis >= auction.getEndTime().getTime())
            throw new AuctionEndedException();

        Optional<Bid> lastBidO = this.bidRepository.findFirstByAuctionOrderByBidValueDesc(auction);
        if (!isBidValueValid(bidValue, auction.getInitialBidValue(), lastBidO))
            throw new InvalidBidValueException();

        lastBidO.ifPresent(bid -> this.auctionsUserService.addTokens(bid.getAuctionsUser().getAuctionsUserId(), bid.getBidValue()));
        this.auctionsUserService.removeTokens(auctionsUser.getAuctionsUserId(), bidValue);

        Bid newBid = new Bid(auction, auctionsUser, bidValue);
        this.bidRepository.save(newBid);

        this.tryProlongAuction(auction, currentTimeMillis);

        this.auctionNotifier.bidPlaced(newBid);
        this.join(auctionId, auctionsUser);
    }

    private void tryProlongAuction(Auction auction, Long currentTimeMillis) {
        Long auctionFinalBidPeriodDurationMillis = auction.getFinalBidPeriodDurationMillis(),
                auctionProlongedTimeDurationMillis = auction.getProlongedTimeDurationMillis(),
                auctionEndTimeMillis = auction.getEndTime().getTime();

        if (currentTimeMillis + auctionFinalBidPeriodDurationMillis > auctionEndTimeMillis) {
            auction.setEndTime(new Timestamp(currentTimeMillis + auctionProlongedTimeDurationMillis));
            this.auctionRepository.save(auction);
            this.dynamicJobSchedulerService.scheduleEndAuctionJob(new Date(auction.getEndTime().getTime()), auction.getAuctionId());
            this.auctionNotifier.auctionProlonged(auction);
        }
    }

    private boolean isBidValueValid(Integer bidValue, Integer auctionInitialBidValue, Optional<Bid> lastBidO) {
        return (lastBidO.isPresent() && lastBidO.get().getBidValue() + 1 == bidValue) || (lastBidO.isEmpty() && bidValue.equals(auctionInitialBidValue));
    }
}
