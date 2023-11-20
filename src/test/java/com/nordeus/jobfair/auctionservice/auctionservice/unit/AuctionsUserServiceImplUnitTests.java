package com.nordeus.jobfair.auctionservice.auctionservice.unit;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.exceptions.NotEnoughTokensException;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionsUserRepository;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.service.AuctionsUserService;
import com.nordeus.jobfair.auctionservice.auctionservice.util.ConfigureBeansTestUtils;
import com.nordeus.jobfair.auctionservice.auctionservice.util.DomainObjectsFactoryUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


@SpringBootTest
public class AuctionsUserServiceImplUnitTests {

    @Autowired
    private AuctionsUserService auctionsUserService;

    @Autowired
    private DomainObjectsFactoryUtils domainObjectsFactoryUtils;

    @Autowired
    private ConfigureBeansTestUtils configureBeansTestUtils;

    @MockBean
    private AuctionsUserRepository auctionsUserRepository;


    @Test
    void givenAuctionsUser_whenRemoveTokens_thenTokensRemoved() {
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40),
                        expectedUpdatedAuctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40 - 13);

        given(this.auctionsUserRepository.findById(auctionsUser.getAuctionsUserId())).willReturn(Optional.of(auctionsUser));
        this.auctionsUserService.removeTokens(auctionsUser.getAuctionsUserId(), 13);

        ArgumentCaptor<AuctionsUser> capturedUpdatedAuctionsUser = ArgumentCaptor.forClass(AuctionsUser.class);
        verify(this.auctionsUserRepository, times(1)).save(capturedUpdatedAuctionsUser.capture());

        assertThat(capturedUpdatedAuctionsUser.getValue()).isEqualTo(expectedUpdatedAuctionsUser);
    }

    @Test
    void givenNegativeTokensCountToRemove_whenRemoveTokens_thenAssertionError() {
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40);

        assertThrows(AssertionError.class, () -> {
            this.auctionsUserService.removeTokens(auctionsUser.getAuctionsUserId(), -13);
        });
    }

    @Test
    void givenAuctionsUserWithoutEnoughTokens_whenRemoveTokens_thenNotEnoughTokensExceptionThrown() {
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40);

        given(this.auctionsUserRepository.findById(auctionsUser.getAuctionsUserId())).willReturn(Optional.of(auctionsUser));

        assertThrows(NotEnoughTokensException.class, () -> {
            this.auctionsUserService.removeTokens(auctionsUser.getAuctionsUserId(), 50);
        });
    }

    @Test
    void givenAuctionsUser_thenAddTokens_thenTokensAdded() {
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40),
                expectedUpdatedAuctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40 + 13);

        given(this.auctionsUserRepository.findById(auctionsUser.getAuctionsUserId())).willReturn(Optional.of(auctionsUser));
        this.auctionsUserService.addTokens(auctionsUser.getAuctionsUserId(), 13);

        ArgumentCaptor<AuctionsUser> capturedUpdatedAuctionsUser = ArgumentCaptor.forClass(AuctionsUser.class);
        verify(this.auctionsUserRepository, times(1)).save(capturedUpdatedAuctionsUser.capture());

        assertThat(capturedUpdatedAuctionsUser.getValue()).isEqualTo(expectedUpdatedAuctionsUser);
    }

    @Test
    void givenNegativeTokensCountToAdd_whenRemoveTokens_thenAssertionError() {
        AuctionsUser auctionsUser = this.domainObjectsFactoryUtils.auctionsUserFactory(2151L, 40);

        assertThrows(AssertionError.class, () -> {
            this.auctionsUserService.addTokens(auctionsUser.getAuctionsUserId(), -13);
        });
    }

    @Test
    void givenUserIdAndDetails_whenUpdateNewUser_thenUserSaved() {
        Long userId = 1251356L;
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("user_name", "RAND USER NAME");

        given(this.auctionsUserRepository.findByUserId(1251356L)).willReturn(Optional.empty());

        this.auctionsUserService.updateUser(userId, userDetails);

        ArgumentCaptor<AuctionsUser> capturedSavedAuctionsUser = ArgumentCaptor.forClass(AuctionsUser.class);
        verify(this.auctionsUserRepository, times(1)).save(capturedSavedAuctionsUser.capture());
        assertThat(capturedSavedAuctionsUser.getValue().getUserId()).isEqualTo(userId);
        assertThat(capturedSavedAuctionsUser.getValue().getUserDetails()).isEqualTo(userDetails);
    }

    @ParameterizedTest
    @ValueSource(ints = {20, 40})
    void givenAuctionsUserInitialTokensAmount_whenUpdateNewUser_thenNewUserInitializedTokensAmount(Integer initTokensAmount) {
        this.configureBeansTestUtils.configureAuctionsUserService(this.auctionsUserService, initTokensAmount);

        Long userId = 1251356L;
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("user_name", "RAND USER NAME");

        given(this.auctionsUserRepository.findByUserId(1251356L)).willReturn(Optional.empty());

        this.auctionsUserService.updateUser(userId, userDetails);

        ArgumentCaptor<AuctionsUser> capturedSavedAuctionsUser = ArgumentCaptor.forClass(AuctionsUser.class);
        verify(this.auctionsUserRepository, times(1)).save(capturedSavedAuctionsUser.capture());
        assertThat(capturedSavedAuctionsUser.getValue().getTokens()).isEqualTo(initTokensAmount);
    }

    @Test
    void givenUserIdAndDetails_whenUpdateOldUser_thenUserSaved() {
        Long userId = 1251356L;
        Map<String, Object> userDetails = new HashMap<>();
        userDetails.put("user_name", "RAND USER NAME");

        given(this.auctionsUserRepository.findByUserId(1251356L)).willReturn(Optional.of(this.domainObjectsFactoryUtils.auctionsUserFactory(1251356L, 40)));

        this.auctionsUserService.updateUser(userId, userDetails);

        ArgumentCaptor<AuctionsUser> capturedSavedAuctionsUser = ArgumentCaptor.forClass(AuctionsUser.class);
        verify(this.auctionsUserRepository, times(1)).save(capturedSavedAuctionsUser.capture());
        assertThat(capturedSavedAuctionsUser.getValue().getUserId()).isEqualTo(userId);
        assertThat(capturedSavedAuctionsUser.getValue().getUserDetails()).isEqualTo(userDetails);
        assertThat(capturedSavedAuctionsUser.getValue().getTokens()).isEqualTo(40);
    }


}
