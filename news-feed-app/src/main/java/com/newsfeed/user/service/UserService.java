package com.newsfeed.user.service;

import com.newsfeed.user.model.User;
import com.newsfeed.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class UserService {

    private static final String USER_CACHE_PREFIX = "user:";
    private static final Duration USER_CACHE_TTL = Duration.ofHours(1);

    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public UserService(UserRepository userRepository, RedisTemplate<String, Object> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    public Optional<User> getUser(UUID userId) {
        // Try User Cache
        Object cached = redisTemplate.opsForValue().get(USER_CACHE_PREFIX + userId);
        if (cached instanceof User user) {
            return Optional.of(user);
        }

        // Fallback to User DB
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> redisTemplate.opsForValue().set(USER_CACHE_PREFIX + u.getId(), u, USER_CACHE_TTL));
        return user;
    }

    public String getUsername(UUID userId) {
        return getUser(userId).map(User::getUsername).orElse("unknown");
    }
}
