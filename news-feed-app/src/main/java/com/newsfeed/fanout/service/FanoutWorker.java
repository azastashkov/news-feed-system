package com.newsfeed.fanout.service;

import com.newsfeed.config.RabbitMqConfig;
import com.newsfeed.fanout.dto.FanoutMessage;
import com.newsfeed.graph.service.GraphService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
public class FanoutWorker {

    private static final String NEWS_FEED_CACHE_PREFIX = "feed:";
    private static final int MAX_FEED_SIZE = 500;

    private final GraphService graphService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Timer fanoutTimer;

    public FanoutWorker(GraphService graphService,
                        RedisTemplate<String, Object> redisTemplate,
                        MeterRegistry meterRegistry) {
        this.graphService = graphService;
        this.redisTemplate = redisTemplate;
        this.fanoutTimer = Timer.builder("fanout.processing.time")
                .description("Time to process a fanout message")
                .register(meterRegistry);
    }

    @RabbitListener(queues = RabbitMqConfig.FANOUT_QUEUE, concurrency = "3-10")
    public void processFanout(FanoutMessage message) {
        fanoutTimer.record(() -> {
            log.info("Processing fanout for post {} by author {}", message.getPostId(), message.getAuthorId());

            List<UUID> friendIds = graphService.getFriendIds(message.getAuthorId());
            log.debug("Distributing post {} to {} friends", message.getPostId(), friendIds.size());

            for (UUID friendId : friendIds) {
                String feedKey = NEWS_FEED_CACHE_PREFIX + friendId;
                redisTemplate.opsForZSet().add(feedKey, message.getPostId().toString(),
                        message.getTimestamp().toEpochMilli());

                // Trim feed to keep only the most recent posts
                Long size = redisTemplate.opsForZSet().size(feedKey);
                if (size != null && size > MAX_FEED_SIZE) {
                    redisTemplate.opsForZSet().removeRange(feedKey, 0, size - MAX_FEED_SIZE - 1);
                }
            }

            log.info("Fanout complete for post {}", message.getPostId());
        });
    }
}
