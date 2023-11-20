package com.nordeus.jobfair.auctionservice.auctionservice.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.Set;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
public class Auction {

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long auctionId;

    @NonNull
    private boolean active;

    @NonNull
    private Timestamp startTime;

    @NonNull
    private Timestamp endTime;

    @NonNull
    private Long FinalBidPeriodDurationMillis;

    @NonNull
    private Long prolongedTimeDurationMillis;

    @NonNull
    private Integer initialBidValue;

    @JsonManagedReference
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "auction")
    private Set<Bid> bids;

    protected Auction() {}
}
