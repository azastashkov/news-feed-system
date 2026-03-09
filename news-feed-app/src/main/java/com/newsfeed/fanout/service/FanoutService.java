package com.newsfeed.fanout.service;

import com.newsfeed.config.RabbitMqConfig;
import com.newsfeed.fanout.dto.FanoutMessage;
import com.newsfeed.post.model.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FanoutService {

    private final RabbitTemplate rabbitTemplate;

    public void fanout(Post post) {
        FanoutMessage message = FanoutMessage.builder()
                .postId(post.getId())
                .authorId(post.getUserId())
                .timestamp(post.getCreatedAt())
                .build();

        rabbitTemplate.convertAndSend(RabbitMqConfig.FANOUT_EXCHANGE, "", message);
        log.info("Fanout message sent for post {}", post.getId());
    }
}
