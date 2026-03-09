package com.newsfeed.graph.service;

import com.newsfeed.graph.repository.UserNodeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GraphServiceTest {

    @Mock
    private UserNodeRepository userNodeRepository;

    @InjectMocks
    private GraphService graphService;

    @Test
    void getFriendIds_shouldReturnFriendIdsFromRepository() {
        UUID userId = UUID.randomUUID();
        UUID friend1 = UUID.randomUUID();
        UUID friend2 = UUID.randomUUID();

        when(userNodeRepository.findFriendIdsByUserId(userId)).thenReturn(List.of(friend1, friend2));

        List<UUID> result = graphService.getFriendIds(userId);

        assertThat(result).containsExactly(friend1, friend2);
    }

    @Test
    void getFriendIds_shouldReturnEmptyListWhenNoFriends() {
        UUID userId = UUID.randomUUID();
        when(userNodeRepository.findFriendIdsByUserId(userId)).thenReturn(List.of());

        List<UUID> result = graphService.getFriendIds(userId);

        assertThat(result).isEmpty();
    }
}
