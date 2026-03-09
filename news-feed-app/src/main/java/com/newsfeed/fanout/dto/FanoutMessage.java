package com.newsfeed.fanout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FanoutMessage implements Serializable {

    private UUID postId;
    private UUID authorId;
    private Instant timestamp;
}
