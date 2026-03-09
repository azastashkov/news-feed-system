package com.newsfeed.graph.repository;

import com.newsfeed.graph.model.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;

import java.util.List;
import java.util.UUID;

public interface UserNodeRepository extends Neo4jRepository<UserNode, UUID> {

    @Query("MATCH (u:User {userId: $userId})-[:FRIENDS_WITH]->(f:User) RETURN f.userId")
    List<UUID> findFriendIdsByUserId(UUID userId);
}
