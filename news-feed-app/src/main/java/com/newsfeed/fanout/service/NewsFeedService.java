package com.newsfeed.fanout.service;

import com.newsfeed.post.dto.PostResponse;
import com.newsfeed.post.model.Post;
import com.newsfeed.post.service.PostService;
import com.newsfeed.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsFeedService {

    private static final String NEWS_FEED_CACHE_PREFIX = "feed:";
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostService postService;
    private final UserService userService;

    public List<PostResponse> getFeed(UUID userId) {
        String feedKey = NEWS_FEED_CACHE_PREFIX + userId;

        // Get the most recent post IDs from the News Feed Cache (sorted set, descending by score)
        Set<Object> postIds = redisTemplate.opsForZSet().reverseRange(feedKey, 0, DEFAULT_PAGE_SIZE - 1);

        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<PostResponse> feed = new ArrayList<>();
        for (Object postIdObj : postIds) {
            UUID postId = UUID.fromString(postIdObj.toString());
            Optional<Post> post = postService.getPost(postId);
            post.ifPresent(p -> {
                String username = userService.getUsername(p.getUserId());
                feed.add(PostResponse.builder()
                        .id(p.getId())
                        .userId(p.getUserId())
                        .username(username)
                        .content(p.getContent())
                        .createdAt(p.getCreatedAt())
                        .build());
            });
        }

        return feed;
    }
}
