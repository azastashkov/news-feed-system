package com.newsfeed.post.controller;

import com.newsfeed.auth.AuthContext;
import com.newsfeed.auth.AuthTokenFilter;
import com.newsfeed.fanout.service.NewsFeedService;
import com.newsfeed.post.dto.PostResponse;
import com.newsfeed.post.model.Post;
import com.newsfeed.post.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FeedController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = AuthTokenFilter.class))
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private NewsFeedService newsFeedService;

    private static final UUID USER_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");

    @TestConfiguration
    static class TestConfig {
        @Bean
        public jakarta.servlet.Filter setAuthContextFilter() {
            return (request, response, chain) -> {
                AuthContext.setCurrentUserId(USER_ID);
                try {
                    chain.doFilter(request, response);
                } finally {
                    AuthContext.clear();
                }
            };
        }
    }

    @Test
    void publishPost_shouldReturnCreatedPost() throws Exception {
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .content("Hello World")
                .createdAt(Instant.now())
                .build();

        when(postService.createPost(eq(USER_ID), eq("Hello World"))).thenReturn(post);

        mockMvc.perform(post("/v1/me/feed")
                        .contentType(APPLICATION_JSON)
                        .content("{\"content\": \"Hello World\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Hello World"))
                .andExpect(jsonPath("$.userId").value(USER_ID.toString()));
    }

    @Test
    void getNewsFeed_shouldReturnFeedPosts() throws Exception {
        PostResponse response = PostResponse.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .username("bob")
                .content("Bob's post")
                .createdAt(Instant.now())
                .build();

        when(newsFeedService.getFeed(USER_ID)).thenReturn(List.of(response));

        mockMvc.perform(get("/v1/me/feed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Bob's post"))
                .andExpect(jsonPath("$[0].username").value("bob"));
    }
}
