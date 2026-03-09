package com.newsfeed.graph.service;

import com.newsfeed.graph.repository.UserNodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GraphService {

    private final UserNodeRepository userNodeRepository;

    public List<UUID> getFriendIds(UUID userId) {
        List<UUID> friendIds = userNodeRepository.findFriendIdsByUserId(userId);
        log.debug("Found {} friends for user {}", friendIds.size(), userId);
        return friendIds;
    }
}
