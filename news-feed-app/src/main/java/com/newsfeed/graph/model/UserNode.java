package com.newsfeed.graph.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Node("User")
public class UserNode {

    @Id
    private UUID userId;

    @Relationship(type = "FRIENDS_WITH")
    private Set<UserNode> friends = new HashSet<>();
}
