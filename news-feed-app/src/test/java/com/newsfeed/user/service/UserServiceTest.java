package com.newsfeed.user.service;

import com.newsfeed.user.model.User;
import com.newsfeed.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Test
    void getUser_shouldReturnFromCacheIfPresent() {
        UUID userId = UUID.randomUUID();
        User cachedUser = User.builder().id(userId).username("alice").build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:" + userId)).thenReturn(cachedUser);

        UserService userService = new UserService(userRepository, redisTemplate);
        Optional<User> result = userService.getUser(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("alice");
    }

    @Test
    void getUser_shouldFallbackToDbOnCacheMiss() {
        UUID userId = UUID.randomUUID();
        User dbUser = User.builder().id(userId).username("bob").build();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:" + userId)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(dbUser));

        UserService userService = new UserService(userRepository, redisTemplate);
        Optional<User> result = userService.getUser(userId);

        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo("bob");
        verify(valueOperations).set(eq("user:" + userId), eq(dbUser), any(Duration.class));
    }

    @Test
    void getUsername_shouldReturnUnknownWhenNotFound() {
        UUID userId = UUID.randomUUID();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("user:" + userId)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserService userService = new UserService(userRepository, redisTemplate);
        String username = userService.getUsername(userId);

        assertThat(username).isEqualTo("unknown");
    }
}
