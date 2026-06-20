package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.RefreshToken;
import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class RefreshTokenRepositoryTest {


    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


    @Autowired
    private UserRepository userRepository;



    private User createUser() {

        User user = new User();

        user.setName("Manoj");
        user.setEmail("refresh@test.com");
        user.setPassword("password123");
        user.setAddress("Bangalore");
        user.setRole(Role.USER);

        return userRepository.save(user);
    }



    private RefreshToken createToken(User user) {

        RefreshToken token = new RefreshToken();

        token.setToken("abc123");
        token.setExpiryDate(
                LocalDateTime.now().plusDays(7)
        );

        token.setUser(user);

        return refreshTokenRepository.save(token);
    }



    @Test
    void findByToken_shouldReturnRefreshToken() {

        User user = createUser();

        createToken(user);


        var result =
                refreshTokenRepository.findByToken(
                        "abc123"
                );


        assertThat(result)
                .isPresent();


        assertThat(result.get().getToken())
                .isEqualTo("abc123");
    }



    @Test
    void findByUser_shouldReturnRefreshToken() {

        User user = createUser();


        createToken(user);


        var result =
                refreshTokenRepository.findByUser(
                        user
                );


        assertThat(result)
                .isPresent();


        assertThat(result.get().getUser())
                .isEqualTo(user);
    }



    @Test
    void deleteByUser_shouldRemoveToken() {

        User user = createUser();


        createToken(user);


        refreshTokenRepository.deleteByUser(user);


        var result =
                refreshTokenRepository.findByUser(user);


        assertThat(result)
                .isEmpty();
    }
}