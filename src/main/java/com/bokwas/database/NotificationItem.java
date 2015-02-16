package com.bokwas.database;

import org.neo4j.graphdb.Relationship;

public class NotificationItem {
	public NotificationItem(Relationship relationship){
		this.edge= relationship;
	}
public Relationship getEdge() {
	return edge;
}private Relationship edge;
public String getData() {
	return edge.getProperty("data").toString();
}
public String getTime() {
	return edge.getProperty("time").toString();
}
}
