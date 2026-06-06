package com.kvl.library.service;

import com.kvl.library.entity.User;
import com.kvl.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomUserDetailsService Unit Tests")
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private final String username = "testUser";
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername(username);
        testUser.setPassword("password123");
    }

    @Test
    @DisplayName("loadUserByUsername() should return UserDetails when the user entity exists in the database")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(testUser));

        UserDetails actualUserDetails = customUserDetailsService.loadUserByUsername(username);

        assertThat(actualUserDetails).isNotNull();
        assertThat(actualUserDetails.getUsername()).isEqualTo(username);
        assertThat(actualUserDetails.getPassword()).isEqualTo("password123");
        verify(userRepository, times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("loadUserByUsername() should throw UsernameNotFoundException when the username does not exist in the database")
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customUserDetailsService.loadUserByUsername(username))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Пользователь не найден: " + username);

        verify(userRepository, times(1)).findByUsername(username);
    }
}