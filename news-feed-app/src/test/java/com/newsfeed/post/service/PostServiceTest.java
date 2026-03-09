package com.newsfeed.post.service;

import com.newsfeed.fanout.service.FanoutService;
import com.newsfeed.post.model.Post;
import com.newsfeed.post.repository.PostRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private FanoutService fanoutService;

    private PostService postService;

    private final UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        MeterRegistry meterRegistry = new SimpleMeterRegistry();
        postService = new PostService(postRepository, redisTemplate, fanoutService, meterRegistry);
    }

    @Test
    void createPost_shouldSaveAndCacheAndFanout() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        Post savedPost = Post.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .content("test content")
                .createdAt(Instant.now())
                .build();
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        Post result = postService.createPost(userId, "test content");

        assertThat(result.getContent()).isEqualTo("test content");
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(postRepository).save(any(Post.class));
        verify(valueOperations).set(eq("post:" + savedPost.getId()), eq(savedPost), any(Duration.class));
        verify(fanoutService).fanout(savedPost);
    }

    @Test
    void getPost_shouldReturnFromCacheIfPresent() {
        UUID postId = UUID.randomUUID();
        Post cachedPost = Post.builder().id(postId).userId(userId).content("cached").build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("post:" + postId)).thenReturn(cachedPost);

        Optional<Post> result = postService.getPost(postId);

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("cached");
    }

    @Test
    void getPost_shouldFallbackToDbOnCacheMiss() {
        UUID postId = UUID.randomUUID();
        Post dbPost = Post.builder().id(postId).userId(userId).content("from db").build();
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("post:" + postId)).thenReturn(null);
        when(postRepository.findById(postId)).thenReturn(Optional.of(dbPost));

        Optional<Post> result = postService.getPost(postId);

        assertThat(result).isPresent();
        assertThat(result.get().getContent()).isEqualTo("from db");
        verify(valueOperations).set(eq("post:" + postId), eq(dbPost), any(Duration.class));
    }
}
