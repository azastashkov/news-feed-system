package com.newsfeed.post.controller;

import com.newsfeed.auth.AuthContext;
import com.newsfeed.fanout.service.NewsFeedService;
import com.newsfeed.post.dto.CreatePostRequest;
import com.newsfeed.post.dto.PostResponse;
import com.newsfeed.post.model.Post;
import com.newsfeed.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/me/feed")
@RequiredArgsConstructor
public class FeedController {

    private final PostService postService;
    private final NewsFeedService newsFeedService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse publishPost(@Valid CreatePostRequest request) {
        UUID userId = AuthContext.getCurrentUserId();
        Post post = postService.createPost(userId, request.getContent());
        return PostResponse.builder()
                .id(post.getId())
                .userId(post.getUserId())
                .content(post.getContent())
                .createdAt(post.getCreatedAt())
                .build();
    }

    @GetMapping
    public List<PostResponse> getNewsFeed() {
        UUID userId = AuthContext.getCurrentUserId();
        return newsFeedService.getFeed(userId);
    }
}
