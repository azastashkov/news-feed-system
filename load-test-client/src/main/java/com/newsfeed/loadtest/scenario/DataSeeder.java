package com.newsfeed.loadtest.scenario;

import com.newsfeed.loadtest.config.LoadTestConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder {

    private final LoadTestConfig config;
    private final Driver neo4jDriver;

    @PersistenceContext
    private EntityManager entityManager;

    private final List<String> authTokens = new ArrayList<>();

    @Transactional
    public void seed() {
        List<UUID> userIds = new ArrayList<>();

        // Create test users in PostgreSQL
        for (int i = 0; i < config.getUserCount(); i++) {
            UUID userId = UUID.randomUUID();
            String username = "loadtest_user_" + i;
            String authToken = "loadtest-token-" + i;

            entityManager.createNativeQuery(
                    "INSERT INTO users (id, username, email, auth_token, created_at) " +
                    "VALUES (:id, :username, :email, :authToken, NOW()) ON CONFLICT (username) DO NOTHING")
                    .setParameter("id", userId)
                    .setParameter("username", username)
                    .setParameter("email", username + "@loadtest.com")
                    .setParameter("authToken", authToken)
                    .executeUpdate();

            userIds.add(userId);
            authTokens.add(authToken);
        }

        log.info("Created {} test users in PostgreSQL", config.getUserCount());

        // Create user nodes and friendships in Neo4j
        try (Session session = neo4jDriver.session()) {
            // Create user nodes
            for (UUID userId : userIds) {
                session.run("MERGE (u:User {userId: $userId})",
                        org.neo4j.driver.Values.parameters("userId", userId.toString()));
            }

            // Create friendship edges (each user befriends the next N users in a ring)
            for (int i = 0; i < userIds.size(); i++) {
                for (int j = 1; j <= config.getFriendsPerUser(); j++) {
                    int friendIndex = (i + j) % userIds.size();
                    session.run(
                            "MATCH (a:User {userId: $uid1}), (b:User {userId: $uid2}) " +
                            "MERGE (a)-[:FRIENDS_WITH]->(b)",
                            org.neo4j.driver.Values.parameters(
                                    "uid1", userIds.get(i).toString(),
                                    "uid2", userIds.get(friendIndex).toString()));
                }
            }

            log.info("Created {} user nodes and friendships in Neo4j", userIds.size());
        }
    }

    public List<String> getAuthTokens() {
        return authTokens;
    }
}
