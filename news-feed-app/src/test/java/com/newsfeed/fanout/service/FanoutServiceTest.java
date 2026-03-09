package com.newsfeed.fanout.service;

import com.newsfeed.config.RabbitMqConfig;
import com.newsfeed.post.model.Post;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.newsfeed.fanout.dto.FanoutMessage;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FanoutServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private FanoutService fanoutService;

    @Test
    void fanout_shouldSendMessageToExchange() {
        Post post = Post.builder()
                .id(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .content("test")
                .createdAt(Instant.now())
                .build();

        fanoutService.fanout(post);

        ArgumentCaptor<FanoutMessage> captor = ArgumentCaptor.forClass(FanoutMessage.class);
        verify(rabbitTemplate).convertAndSend(eq(RabbitMqConfig.FANOUT_EXCHANGE), eq(""), captor.capture());

        FanoutMessage message = captor.getValue();
        assertThat(message.getPostId()).isEqualTo(post.getId());
        assertThat(message.getAuthorId()).isEqualTo(post.getUserId());
    }
}
