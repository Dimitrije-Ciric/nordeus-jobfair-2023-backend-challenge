package com.nordeus.jobfair.auctionservice.auctionservice.api;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.AuctionService;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.DomainException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.Auction;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@AllArgsConstructor
@RestController
@RequestMapping(path = "/auctions")
public class HttpController {

    private AuctionService auctionService;

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DomainException.class)
    public void conflict() {}

    @GetMapping("/active")
    public Collection<Auction> getAllActive() {
        return this.auctionService.getAllActive();
    }

    @GetMapping("/{auctionId}")
    public Auction getAuctionById(@PathVariable Long auctionId) {
        return this.auctionService.getAuction(auctionId);
    }

    @PutMapping("/{auctionId}/join")
    public void joinAuction(@PathVariable Long auctionId, HttpServletRequest request) {
        AuctionsUser auctionsUser = (AuctionsUser) request.getAttribute("auctions_user");
        this.auctionService.join(auctionId, auctionsUser);
    }

    @PostMapping(value = "/{auctionId}/bid", consumes = {"application/json"})
    public void bidAuction(@PathVariable Long auctionId, @RequestBody BidDetails bidDetails, HttpServletRequest request) {
        AuctionsUser auctionsUser = (AuctionsUser) request.getAttribute("auctions_user");
        Integer bidValue = Integer.valueOf(bidDetails.getBidValue());

        this.auctionService.bid(bidValue, auctionId, auctionsUser);
    }

    @Getter
    private static class BidDetails {
        private String bidValue;
    }
}
