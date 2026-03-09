package com.newsfeed.fanout.service;

import com.newsfeed.fanout.dto.FanoutMessage;
import com.newsfeed.graph.service.GraphService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FanoutWorkerTest {

    @Mock
    private GraphService graphService;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    private FanoutWorker fanoutWorker;

    @BeforeEach
    void setUp() {
        fanoutWorker = new FanoutWorker(graphService, redisTemplate, new SimpleMeterRegistry());
    }

    @Test
    void processFanout_shouldAddPostToFriendFeeds() {
        UUID authorId = UUID.randomUUID();
        UUID friendId1 = UUID.randomUUID();
        UUID friendId2 = UUID.randomUUID();
        UUID postId = UUID.randomUUID();
        Instant now = Instant.now();

        when(graphService.getFriendIds(authorId)).thenReturn(List.of(friendId1, friendId2));
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(zSetOperations.size(anyString())).thenReturn(1L);

        FanoutMessage message = FanoutMessage.builder()
                .postId(postId)
                .authorId(authorId)
                .timestamp(now)
                .build();

        fanoutWorker.processFanout(message);

        verify(zSetOperations).add(eq("feed:" + friendId1), eq(postId.toString()), eq((double) now.toEpochMilli()));
        verify(zSetOperations).add(eq("feed:" + friendId2), eq(postId.toString()), eq((double) now.toEpochMilli()));
    }
}
