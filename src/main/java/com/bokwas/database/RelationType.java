package com.bokwas.database;

import org.neo4j.graphdb.RelationshipType;

public enum RelationType implements RelationshipType {
	FRIENDS_WITH, POSTS_OF,LIKED_COMMENT, LIKES, COMMENTS_OF,SUBSCRIBE_TO,BOKWAS_POST,NOTIFY,PRIVATE_MESSAGE_OF
	,NOTIFIED
	
}
