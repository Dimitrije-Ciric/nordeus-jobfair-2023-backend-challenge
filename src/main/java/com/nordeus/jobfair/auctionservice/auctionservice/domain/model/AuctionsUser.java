package com.nordeus.jobfair.auctionservice.auctionservice.domain.model;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.util.HashMapConverter;
import jakarta.persistence.*;
import lombok.*;

import java.util.Map;

@Entity
@Getter
@Setter
@EqualsAndHashCode
@RequiredArgsConstructor
public class AuctionsUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long auctionsUserId;

    @NonNull
    @Column(unique = true)
    private Long userId;

    @NonNull
    @Convert(converter = HashMapConverter.class)
    private Map<String, Object> userDetails;

    @NonNull
    private Integer tokens;

    protected AuctionsUser() {}
}
