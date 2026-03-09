package com.newsfeed.loadtest.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
public class NewsFeedApiClient {

    private final WebClient webClient;

    public NewsFeedApiClient(String baseUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public boolean publishPost(String authToken, String content) {
        try {
            webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/me/feed")
                            .queryParam("auth_token", authToken)
                            .build())
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .bodyValue(java.util.Map.of("content", content))
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("Publish failed: {}", e.getMessage());
            return false;
        }
    }

    public boolean retrieveFeed(String authToken) {
        try {
            webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/me/feed")
                            .queryParam("auth_token", authToken)
                            .build())
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            log.debug("Retrieve failed: {}", e.getMessage());
            return false;
        }
    }
}
