package com.newsfeed.loadtest;

import com.newsfeed.loadtest.config.LoadTestConfig;
import com.newsfeed.loadtest.scenario.DataSeeder;
import com.newsfeed.loadtest.scenario.PublishPostScenario;
import com.newsfeed.loadtest.scenario.RetrieveFeedScenario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class LoadTestApplication implements CommandLineRunner {

    private final LoadTestConfig config;
    private final DataSeeder dataSeeder;
    private final PublishPostScenario publishPostScenario;
    private final RetrieveFeedScenario retrieveFeedScenario;

    public static void main(String[] args) {
        SpringApplication.run(LoadTestApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== News Feed Load Test ===");
        log.info("Target URL: {}", config.getTargetUrl());
        log.info("Users: {}, Duration: {}s, Publish RPS: {}, Read RPS: {}",
                config.getUserCount(), config.getDurationSeconds(),
                config.getPublishRps(), config.getReadRps());

        // Step 1: Seed test data
        log.info("Seeding test data...");
        dataSeeder.seed();
        log.info("Test data seeded.");

        // Wait for app to process any startup fanouts
        Thread.sleep(2000);

        // Step 2: Run load test
        log.info("Starting load test for {} seconds...", config.getDurationSeconds());

        CountDownLatch latch = new CountDownLatch(1);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

        // Schedule publish posts
        scheduler.scheduleAtFixedRate(
                publishPostScenario::execute,
                0, 1000 / config.getPublishRps(), TimeUnit.MILLISECONDS);

        // Schedule read feeds
        scheduler.scheduleAtFixedRate(
                retrieveFeedScenario::execute,
                0, 1000 / config.getReadRps(), TimeUnit.MILLISECONDS);

        // Run for configured duration
        scheduler.schedule(() -> {
            scheduler.shutdown();
            latch.countDown();
        }, config.getDurationSeconds(), TimeUnit.SECONDS);

        latch.await();

        // Print results
        log.info("=== Load Test Complete ===");
        log.info("Publish - Total: {}, Success: {}, Failed: {}, Avg Latency: {}ms",
                publishPostScenario.getTotalRequests(),
                publishPostScenario.getSuccessCount(),
                publishPostScenario.getFailCount(),
                publishPostScenario.getAvgLatencyMs());
        log.info("Read    - Total: {}, Success: {}, Failed: {}, Avg Latency: {}ms",
                retrieveFeedScenario.getTotalRequests(),
                retrieveFeedScenario.getSuccessCount(),
                retrieveFeedScenario.getFailCount(),
                retrieveFeedScenario.getAvgLatencyMs());
    }
}
