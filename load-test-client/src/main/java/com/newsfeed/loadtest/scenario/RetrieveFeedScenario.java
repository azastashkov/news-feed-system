package com.newsfeed.loadtest.scenario;

import com.newsfeed.loadtest.client.NewsFeedApiClient;
import com.newsfeed.loadtest.config.LoadTestConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
public class RetrieveFeedScenario {

    private final NewsFeedApiClient apiClient;
    private final DataSeeder dataSeeder;

    @Getter
    private final AtomicLong totalRequests = new AtomicLong();
    @Getter
    private final AtomicLong successCount = new AtomicLong();
    @Getter
    private final AtomicLong failCount = new AtomicLong();
    private final AtomicLong totalLatencyMs = new AtomicLong();

    public RetrieveFeedScenario(LoadTestConfig config, DataSeeder dataSeeder) {
        this.apiClient = new NewsFeedApiClient(config.getTargetUrl());
        this.dataSeeder = dataSeeder;
    }

    public void execute() {
        List<String> tokens = dataSeeder.getAuthTokens();
        if (tokens.isEmpty()) return;

        String token = tokens.get(ThreadLocalRandom.current().nextInt(tokens.size()));

        totalRequests.incrementAndGet();
        long start = System.currentTimeMillis();
        boolean success = apiClient.retrieveFeed(token);
        long latency = System.currentTimeMillis() - start;

        totalLatencyMs.addAndGet(latency);

        if (success) {
            successCount.incrementAndGet();
        } else {
            failCount.incrementAndGet();
        }

        if (totalRequests.get() % 100 == 0) {
            log.info("Read progress: {} requests, {} success, {} failed, avg {}ms",
                    totalRequests.get(), successCount.get(), failCount.get(), getAvgLatencyMs());
        }
    }

    public long getAvgLatencyMs() {
        long total = totalRequests.get();
        return total > 0 ? totalLatencyMs.get() / total : 0;
    }
}
