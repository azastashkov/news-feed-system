package com.newsfeed.post.service;

import com.newsfeed.fanout.service.FanoutService;
import com.newsfeed.post.model.Post;
import com.newsfeed.post.repository.PostRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class PostService {

    private static final String POST_CACHE_PREFIX = "post:";
    private static final Duration POST_CACHE_TTL = Duration.ofHours(24);

    private final PostRepository postRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FanoutService fanoutService;
    private final Counter postPublishedCounter;

    public PostService(PostRepository postRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       FanoutService fanoutService,
                       MeterRegistry meterRegistry) {
        this.postRepository = postRepository;
        this.redisTemplate = redisTemplate;
        this.fanoutService = fanoutService;
        this.postPublishedCounter = Counter.builder("posts.published")
                .description("Number of posts published")
                .register(meterRegistry);
    }

    public Post createPost(UUID userId, String content) {
        Post post = Post.builder()
                .userId(userId)
                .content(content)
                .createdAt(Instant.now())
                .build();

        post = postRepository.save(post);
        log.info("Post created: id={}, userId={}", post.getId(), userId);

        // Write to Post Cache
        redisTemplate.opsForValue().set(POST_CACHE_PREFIX + post.getId(), post, POST_CACHE_TTL);

        // Trigger fanout
        fanoutService.fanout(post);

        postPublishedCounter.increment();
        return post;
    }

    public Optional<Post> getPost(UUID postId) {
        // Try Post Cache first
        Object cached = redisTemplate.opsForValue().get(POST_CACHE_PREFIX + postId);
        if (cached instanceof Post post) {
            return Optional.of(post);
        }

        // Fallback to DB
        Optional<Post> post = postRepository.findById(postId);
        post.ifPresent(p -> redisTemplate.opsForValue().set(POST_CACHE_PREFIX + p.getId(), p, POST_CACHE_TTL));
        return post;
    }
}
