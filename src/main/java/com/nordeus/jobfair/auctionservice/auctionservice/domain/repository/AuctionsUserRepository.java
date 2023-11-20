package com.nordeus.jobfair.auctionservice.auctionservice.domain.repository;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionsUserRepository extends CrudRepository<AuctionsUser, Long> {

    Optional<AuctionsUser> findByUserId(Long userId);
}
