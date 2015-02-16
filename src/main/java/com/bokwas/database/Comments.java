package com.bokwas.database;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.graphdb.traversal.TraversalDescription;
import org.neo4j.graphdb.traversal.Traverser;
import org.neo4j.graphdb.traversal.Uniqueness;
import org.neo4j.helpers.collection.IterableWrapper;

/**
 * 
 * @author sk
 * @Properties text,time,commentedBy,commentedId,numLikes
 */
public class Comments {
	static final String COMMENT_TEXT = "commenttext";
	static final String COMMENT_TIME = "commenttime";
	static final String COMMENT_ID = "commentid";
	static final String LIKES = "likes";
	static final String COMMENT_BY = "commentby";
	static final String ABUSE_COUNT = "abusecount";
	static final String REPORTED_BY = "reportedby";
	
	private final Node underlyingNode;
	
	public String getReportedByUserIds() {
		return (String) underlyingNode.getProperty(REPORTED_BY);
	}

	public void setReportedByUserIds(String reportedByUserId) {
		if (getReportedByUserIds().split(",").length == 0) {
			underlyingNode.setProperty(REPORTED_BY, reportedByUserId);
		}
		else {
			underlyingNode.setProperty(REPORTED_BY, (getReportedByUserIds() + "," + reportedByUserId));
		}
	}
	
	public int getAbuseCount() {
		return (int) underlyingNode.getProperty(ABUSE_COUNT);
	}
	
	public void setAbuseCount() {
		underlyingNode.setProperty(ABUSE_COUNT, getAbuseCount() + 1);
	}

	public Comments(Node userNode) {
		this.underlyingNode = userNode;
	}

	public Comments(final String commentText, final long commentTime,
			final String commentId,final String commentedBy) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(
					BokwasDB.getDatabase(), "Comment") {
				@Override
				protected void initialize(Node created,
						Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label("comments"));
					created.setProperty(COMMENT_ID, properties.get(COMMENT_ID));
				}
			};
			Node node = factory.getOrCreate(COMMENT_ID, commentId);
			node.setProperty(COMMENT_TEXT, commentText);
			node.setProperty(COMMENT_TIME, commentTime);
			node.setProperty(COMMENT_BY, commentedBy);
			node.setProperty(ABUSE_COUNT, 0);
			node.setProperty(LIKES, "");
			node.setProperty(REPORTED_BY,"");
			tx.success();
		this.underlyingNode = node;
		}
	}

	public String getCommentText() {
		return (String) underlyingNode.getProperty(COMMENT_TEXT);
	}

	public long getCommentTime() {
		return (long) underlyingNode.getProperty(COMMENT_TIME);
	}

	public String getCommentedBy() {
		return (String) underlyingNode.getProperty(COMMENT_BY);
	}

	public String getCommentId() {
		return (String) underlyingNode.getProperty(COMMENT_ID);
	}

	public HashMap<String,String> getLikes() {
		HashMap<String,String> likes=new HashMap<String,String>();
		TraversalDescription td=BokwasDB.getDatabase().traversalDescription().breadthFirst()
				                .relationships(RelationType.LIKED_COMMENT)
				                .uniqueness(Uniqueness.NODE_PATH)
				                .evaluator(Evaluators.toDepth(1))
				                .evaluator(Evaluators.excludeStartPosition());
		
		 	Iterable<Person> i=createPersonsFromPath(td.traverse(underlyingNode));
		 	for(Person p:i)
		 	{
		 		likes.put(p.getFbId()+"_"+p.getBokwasAvatarId(),p.getBokwasName());
		 	}
		return likes;
	}
	
	private IterableWrapper<Person, Path> createPersonsFromPath(
			Traverser iterableToWrap) {
		return new IterableWrapper<Person, Path>(iterableToWrap) {
			@Override
			protected Person underlyingObjectToObject(Path path) {
				Person po=new Person(path.endNode());
				//posts.add(po.getPostId());
				return po;
				}
	};
	}

   

	public void setCommentId(String id) {
		underlyingNode.setProperty(COMMENT_ID, id);
	}

	public void setCommentedBy(String postedBy) {
		underlyingNode.setProperty(COMMENT_BY, postedBy);
	}

	public void setCommentTime(String time) {
		underlyingNode.setProperty(COMMENT_TIME, time);
	}

	public void setCommentText(String text) {
		underlyingNode.setProperty(COMMENT_TEXT, text);
	}

	public void addLikes(Node person) {
		underlyingNode.createRelationshipTo(person, RelationType.LIKED_COMMENT);

		/*	String likes = getLikes();
		if(likes.contains(fbId)) {
			return;
		}
		likes += fbId + ",";
		setLikes(likes);*/
	}

	public Node getUnderlyingNode() {
		return underlyingNode;
	}
	
	@Override
	public int hashCode() {
		return underlyingNode.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Comments
				&& underlyingNode.equals(((Comments) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Comment[ COMMENT_TEXT : " + getCommentText()
				+ " , COMMENTED_BY : " + getCommentedBy() + "]";
	}

	public void removeLikes(String fbid) {
		   Iterable<Relationship> r=underlyingNode.getRelationships(RelationType.LIKED_COMMENT,Direction.BOTH);
		   
		    for(Relationship rel:r)
		    {
		    	System.out.println(rel.getOtherNode(underlyingNode).getProperty("fbid"));
		    	if(rel.getOtherNode(underlyingNode).getProperty("fbid").equals(fbid))
		    		{
		    		rel.delete();
		    		return;
		    		}
		    }
	
	}
}
