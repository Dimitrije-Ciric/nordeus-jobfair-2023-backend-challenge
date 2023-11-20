package com.nordeus.jobfair.auctionservice.auctionservice.integration;

import com.nordeus.jobfair.auctionservice.auctionservice.domain.model.AuctionsUser;
import com.nordeus.jobfair.auctionservice.auctionservice.domain.repository.AuctionsUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class AuctionsUserRepositoryIntegrationTests {

    @Autowired
    private AuctionsUserRepository auctionsUserRepository;

    @AfterEach
    void tearDown() {
        this.auctionsUserRepository.deleteAll();
    }

    @Test
    void given2AuctionsUsersWithSameUserId_WhenSave_ThenDatabaseIntegrityExceptionThrown() {
        AuctionsUser newAuctionsUser1 = new AuctionsUser(10L, new HashMap<>(), 40),
                        newAuctionsUser2 = new AuctionsUser(10L, new HashMap<>(), 40);

        this.auctionsUserRepository.save(newAuctionsUser1);
        assertThrows(DataIntegrityViolationException.class,() -> { this.auctionsUserRepository.save(newAuctionsUser2); });
    }

    @Test
    void givenLargeUserDetails_WhenSave_ThenSuccessfulSave() {
        Map<String, Object> largeUserDetails = new HashMap<>();
        largeUserDetails.put("username", "Very LaRgE User NaMe 123");
        largeUserDetails.put("s3_user_image_url", "s3://s3.my.domain.com/sharedfiles/acded7f4-33c2-4087-80f1-b060e57fe99b/bb5f9cce-f148-405c-81e1-ef846052259a.jpg");

        AuctionsUser newAuctionsUser = new AuctionsUser(10L, largeUserDetails, 40);

        AuctionsUser savedAuctionsUser = this.auctionsUserRepository.save(newAuctionsUser);

        assertThat(savedAuctionsUser).isEqualTo(newAuctionsUser);
    }

}
