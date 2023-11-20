package com.nordeus.jobfair.auctionservice.auctionservice.domain.service.implementation;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.NotEnoughTokensException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionsUserRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(isolation = Isolation.SERIALIZABLE)
public class AuctionsUserServiceImpl implements AuctionsUserService {

    private final AuctionsUserRepository auctionsUserRepository;

    @Value("${domain.auctionsUserInitialTokensAmount}")
    private int auctionsUserInitialTokensAmount;

    @Override
    public void removeTokens(Long auctionsUserId, Integer tokensCount) {
        assert tokensCount >= 0;

        AuctionsUser auctionsUser = this.auctionsUserRepository.findById(auctionsUserId).get();

        if (auctionsUser.getTokens() < tokensCount)
            throw new NotEnoughTokensException();
        auctionsUser.setTokens(auctionsUser.getTokens() - tokensCount);

        this.auctionsUserRepository.save(auctionsUser);
    }

    @Override
    public void addTokens(Long auctionsUserId, Integer tokensCount) {
        assert tokensCount >= 0;

        AuctionsUser auctionsUser = this.auctionsUserRepository.findById(auctionsUserId).get();

        auctionsUser.setTokens(auctionsUser.getTokens() + tokensCount);
        this.auctionsUserRepository.save(auctionsUser);
    }

    @Override
    public AuctionsUser updateUser(Long userId, Map<String, Object> userDetails) {
        Optional<AuctionsUser> auctionsUserO = this.auctionsUserRepository.findByUserId(userId);

        AuctionsUser auctionsUser;

        if (auctionsUserO.isPresent()) {
            auctionsUser = auctionsUserO.get();
            auctionsUser.setUserDetails(userDetails);
        }
        else
            auctionsUser = new AuctionsUser(userId, userDetails, this.auctionsUserInitialTokensAmount);

        return this.auctionsUserRepository.save(auctionsUser);
    }
}
