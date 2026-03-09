package com.newsfeed.fanout.service;

import com.newsfeed.post.dto.PostResponse;
import com.newsfeed.post.model.Post;
import com.newsfeed.post.service.PostService;
import com.newsfeed.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NewsFeedServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private PostService postService;

    @Mock
    private UserService userService;

    @InjectMocks
    private NewsFeedService newsFeedService;

    @Test
    void getFeed_shouldReturnPostsFromCache() {
        UUID userId = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        Set<Object> postIds = new LinkedHashSet<>();
        postIds.add(postId.toString());
        when(zSetOperations.reverseRange("feed:" + userId, 0, 19)).thenReturn(postIds);

        Post post = Post.builder()
                .id(postId)
                .userId(authorId)
                .content("Hello from friend")
                .createdAt(Instant.now())
                .build();
        when(postService.getPost(postId)).thenReturn(Optional.of(post));
        when(userService.getUsername(authorId)).thenReturn("bob");

        List<PostResponse> feed = newsFeedService.getFeed(userId);

        assertThat(feed).hasSize(1);
        assertThat(feed.get(0).getContent()).isEqualTo("Hello from friend");
        assertThat(feed.get(0).getUsername()).isEqualTo("bob");
    }

    @Test
    void getFeed_shouldReturnEmptyListWhenNoPosts() {
        UUID userId = UUID.randomUUID();
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.reverseRange("feed:" + userId, 0, 19)).thenReturn(null);

        List<PostResponse> feed = newsFeedService.getFeed(userId);

        assertThat(feed).isEmpty();
    }
}
