package edu.berkeley.ground.api.models.neo4j;

import edu.berkeley.ground.api.models.Edge;
import edu.berkeley.ground.api.models.EdgeFactory;
import edu.berkeley.ground.api.versions.Type;
import edu.berkeley.ground.api.versions.neo4j.Neo4jItemFactory;
import edu.berkeley.ground.db.DBClient.GroundDBConnection;
import edu.berkeley.ground.db.DbDataContainer;
import edu.berkeley.ground.db.Neo4jClient;
import edu.berkeley.ground.db.Neo4jClient.Neo4jConnection;
import edu.berkeley.ground.exceptions.GroundException;
import org.neo4j.driver.v1.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Neo4jEdgeFactory extends EdgeFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jEdgeFactory.class);
    private Neo4jClient dbClient;
    private Neo4jItemFactory itemFactory;

    public Neo4jEdgeFactory(Neo4jItemFactory itemFactory, Neo4jClient dbClient) {
        this.dbClient = dbClient;
        this.itemFactory = itemFactory;
    }

    public Edge create(String name) throws GroundException {
        Neo4jConnection connection = dbClient.getConnection();

        try {
            String uniqueId = "Edges." + name;

            this.itemFactory.insertIntoDatabase(connection, uniqueId);

            List<DbDataContainer> insertions = new ArrayList<>();
            insertions.add(new DbDataContainer("name", Type.STRING, name));
            insertions.add(new DbDataContainer("id", Type.STRING, uniqueId));

            connection.addVertex("GroundEdge", insertions);

            connection.commit();
            LOGGER.info("Created edge " + name + ".");
            return EdgeFactory.construct(uniqueId, name);
        } catch (GroundException e) {
            connection.abort();

            throw e;
        }
    }

    public Edge retrieveFromDatabase(String name) throws GroundException {
        Neo4jConnection connection = dbClient.getConnection();

        try {
            List<DbDataContainer> predicates = new ArrayList<>();
            predicates.add(new DbDataContainer("name", Type.STRING, name));

            Record record = connection.getVertex(predicates);
            String id = record.get("id").toString();

            connection.commit();
            LOGGER.info("Retrieved edge " + name + ".");

            return EdgeFactory.construct(id, name);
        } catch (GroundException e) {
            connection.abort();

            throw e;
        }
    }


    public void update(GroundDBConnection connection, String itemId, String childId, Optional<String> parent) throws GroundException {
        this.itemFactory.update(connection, "Edges." +  itemId, childId, parent);
    }
}
