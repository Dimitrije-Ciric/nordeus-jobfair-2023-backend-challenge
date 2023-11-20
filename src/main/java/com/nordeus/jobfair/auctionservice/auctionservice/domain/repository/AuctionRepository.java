package com.nordeus.jobfair.auctionservice.auctionservice.domain.repository;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends CrudRepository<Auction, Long> {

    Optional<List<Auction>> findByActiveTrue();

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Auction a SET a.active = :new_active, a.endTime = :new_end_time " +
            "WHERE a.auctionId = :auction_id AND a.active = :old_active AND a.endTime = :old_end_time")
    int updateAuction(@Param("auction_id") Long auctionId,
                       @Param("new_active") boolean newActive,
                       @Param("new_end_time") Long newEndTime,
                       @Param("old_active") boolean oldActive,
                       @Param("old_end_time") Long oldEndTime);
}
