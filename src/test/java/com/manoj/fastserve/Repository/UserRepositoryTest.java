package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;


    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {

        User user = new User();
        user.setName("Manoj");
        user.setEmail("manoj@test.com");
        user.setPassword("password123");
        user.setAddress("Bangalore");
        user.setRole(Role.USER);

        userRepository.save(user);


        Optional<User> result = userRepository.findByEmail("manoj@test.com");


        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("manoj@test.com");
    }


    @Test
    void findByEmail_shouldReturnEmpty_whenEmailDoesNotExist() {

        Optional<User> result = userRepository.findByEmail("unknown@test.com");


        assertThat(result).isEmpty();
    }
}