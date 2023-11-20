package com.nordeus.jobfair.auctionservice.auctionservice.domain.service;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;

import java.util.Map;

public interface AuctionsUserService {

    void removeTokens(Long auctionsUserId, Integer tokensCount);

    void addTokens(Long auctionsUserId, Integer tokensCount);

    AuctionsUser updateUser(Long userId, Map<String, Object> userDetails);
}
