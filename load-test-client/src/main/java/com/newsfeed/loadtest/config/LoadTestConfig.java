package com.newsfeed.loadtest.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "load-test")
public class LoadTestConfig {

    private String targetUrl = "http://localhost";
    private int userCount = 20;
    private int friendsPerUser = 5;
    private int durationSeconds = 60;
    private int publishRps = 10;
    private int readRps = 50;
}
