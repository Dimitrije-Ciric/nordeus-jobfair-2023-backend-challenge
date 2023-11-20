package com.nordeus.jobfair.auctionservice.auctionservice.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "UniqueAuctionIdAndBidValue", columnNames = { "auction_id", "bidValue" })
})
@RequiredArgsConstructor
@Getter
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long bidId;

    @NonNull
    @ManyToOne
    @JsonBackReference
    @JoinColumn(name="auction_id", nullable=false)
    private Auction auction;

    @NonNull
    @ManyToOne
    @JoinColumn(name="auctions_user_id", nullable=false)
    private AuctionsUser auctionsUser;

    @NonNull
    private Integer bidValue;

    @Column(name = "created_at")
    @CreationTimestamp
    private Timestamp createdAt;

    protected Bid() {}
}
