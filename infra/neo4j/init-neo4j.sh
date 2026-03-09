#!/bin/bash
# Wait for Neo4j to be ready, then run the init script
echo "Waiting for Neo4j to be ready..."
until cypher-shell -u neo4j -p neo4jpassword "RETURN 1" > /dev/null 2>&1; do
  sleep 2
done
echo "Neo4j is ready. Running init script..."
cypher-shell -u neo4j -p neo4jpassword < /var/lib/neo4j/import/init-graph.cypher
echo "Neo4j init complete."
