package com.bokwas.database;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class BokwasDB {

	private static GraphDatabaseService graphDb;
	private static ExecutionEngine engine;
	public static String DB_PATH = "/root/bokwasdb/graph.db";

//	public static String DB_PATH = "/Users/sk/Documents/bokwas/server/database/graph.db";

	public static GraphDatabaseService getDatabase() {
		if (graphDb == null || (!graphDb.isAvailable(100))) {
			graphDb = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH);
			registerShutdownHook(graphDb);
		}
		return graphDb;
	}

	public static ExecutionEngine getEngine() {
		if (engine == null)
			engine = new ExecutionEngine(graphDb);
		return engine;
	}

	public static void registerShutdownHook(final GraphDatabaseService graphDb) {
		// Registers a shutdown hook for the Neo4j instance so that it
		// shuts down nicely when the VM exits (even if you "Ctrl-C" the
		// running application).
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				graphDb.shutdown();
			}
		});
	}

}
