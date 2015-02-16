package com.bokwas.database;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.neo4j.helpers.collection.IteratorUtil;

/**
 * 
 * @author sk
 * @Properties text,time,postedBy,postId,isBokwasPost,numLikes
 */
public class Posts {

	static final String POST_TEXT = "posttext";
	static final String POST_TIME = "posttime";
	static final String POST_ID = "postid";
	static final String UPDATED_TIME = "postupdatetime";
	static final String LIKES = "likes";
	static final String POSTED_BY = "postedby";
	static final String POSTED_BY_NAME = "postedbyname";
	static final String IS_BOKWAS_POST = "isbokwaspost";
	static final String POSTED_BY_AVATAR_ID = "avatarid";
	static final String TYPE = "type";
	static final String PHOTO_URL = "photourl";
	public String BOKWAS_NAME = "";
	public String AVATAR_ID = "";
	static final String POSTS_INDEX = "Post";
	static final String POSTED_BY_PIC = "postedbypic";
	static final String ABUSE_COUNT = "abusecount";
	static final String REPORTED_BY = "reportedby";

	private final Node underlyingNode;

	public String getReportedByUserIds() {
		return (String) underlyingNode.getProperty(REPORTED_BY);
	}

	public void setReportedByUserIds(String reportedByUserId) {
		if (getReportedByUserIds().split(",").length == 0) {
			underlyingNode.setProperty(REPORTED_BY, reportedByUserId);
		} else {
			underlyingNode.setProperty(REPORTED_BY, (getReportedByUserIds() + "," + reportedByUserId));
		}
	}

	public int getAbuseCount() {
		if (underlyingNode.hasProperty(ABUSE_COUNT)) {
			return (int) underlyingNode.getProperty(ABUSE_COUNT);
		} else {
			underlyingNode.setProperty(ABUSE_COUNT, 0);
			return 0;
		}
	}

	public void setAbuseCount() {
		underlyingNode.setProperty(ABUSE_COUNT, getAbuseCount() + 1);
	}

	public Posts(Node userNode) {
		this.underlyingNode = userNode;
	}

	public Posts(final String postText, final long postTime, final String postId, final String postedBy, String fbname, String avatarId,
			String pic_link, final boolean isBokwasPost) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(BokwasDB.getDatabase(), "Post") {
				@Override
				protected void initialize(Node created, Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label("posts"));
					created.setProperty(POST_ID, properties.get(POST_ID));
				}
			};
			Node node = factory.getOrCreate(POST_ID, postId);
			node.setProperty(POST_TEXT, postText);
			node.setProperty(POST_TIME, postTime);
			node.setProperty(POSTED_BY, postedBy);
			node.setProperty(IS_BOKWAS_POST, isBokwasPost);
			node.setProperty(POSTED_BY_NAME, fbname);
			node.setProperty(POSTED_BY_AVATAR_ID, avatarId);
			node.setProperty(POSTED_BY_PIC, pic_link);
			node.setProperty(TYPE, "status");
			node.setProperty(ABUSE_COUNT, 0);
			node.setProperty(REPORTED_BY, "");
			if (!node.hasProperty(LIKES)) {
				node.setProperty(LIKES, "");
			}
			if (!node.hasProperty(UPDATED_TIME)) {
				node.setProperty(UPDATED_TIME, postTime);
			}
			tx.success();
			this.underlyingNode = node;
		}

	}

	public Posts(final String postText, final long postTime, final String postId, final String postedBy, String fbname, String avatarId,
			String pic_link, String photo_url, final boolean isBokwasPost) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			UniqueFactory.UniqueNodeFactory factory = new UniqueFactory.UniqueNodeFactory(BokwasDB.getDatabase(), "Photos") {
				@Override
				protected void initialize(Node created, Map<String, Object> properties) {
					created.addLabel(DynamicLabel.label("PHOTOS"));
					created.setProperty(POST_ID, properties.get(POST_ID));
				}
			};
			Node node = factory.getOrCreate(POST_ID, postId);
			node.setProperty(POST_TEXT, postText);
			node.setProperty(POST_TIME, postTime);
			node.setProperty(POSTED_BY, postedBy);
			node.setProperty(IS_BOKWAS_POST, isBokwasPost);
			node.setProperty(POSTED_BY_NAME, fbname);
			node.setProperty(POSTED_BY_AVATAR_ID, avatarId);
			node.setProperty(POSTED_BY_PIC, pic_link);
			node.setProperty(PHOTO_URL, photo_url);
			node.setProperty(TYPE, "photo");
			if (!node.hasProperty(LIKES)) {
				node.setProperty(LIKES, "");
			}
			if (!node.hasProperty(UPDATED_TIME)) {
				node.setProperty(UPDATED_TIME, postTime);
			}
			tx.success();
			this.underlyingNode = node;
		}

	}

	public String getAvatarId() {
		try {
			return (String) underlyingNode.getProperty(POSTED_BY_AVATAR_ID);
		} catch (Exception e) {
			return "";
		}
	}

	public String getPostText() {
		return (String) underlyingNode.getProperty(POST_TEXT);
	}

	public long getPostTime() {
		return (long) underlyingNode.getProperty(POST_TIME);
	}

	public long getPostUpdatedTime() {
		return (long) underlyingNode.getProperty(UPDATED_TIME);
	}

	public String getPostedByName() {
		return (String) underlyingNode.getProperty(POSTED_BY_NAME);
	}

	public String getFbPicture() {
		return (String) underlyingNode.getProperty(POSTED_BY_PIC);
	}

	public String getType() {
		try {
			return underlyingNode.getProperty(TYPE).toString();
		} catch (Exception e) {
			return "status";
		}
	}

	public String getPostedBy() {
		return (String) underlyingNode.getProperty(POSTED_BY);
	}

	public String getPostId() {
		return (String) underlyingNode.getProperty(POST_ID);
	}

	public HashMap<String, String> getLikes() {
		HashMap<String, String> likes = new HashMap<String, String>();
		TraversalDescription td = BokwasDB.getDatabase().traversalDescription().breadthFirst().relationships(RelationType.LIKES)
				.uniqueness(Uniqueness.NODE_PATH).evaluator(Evaluators.toDepth(1)).evaluator(Evaluators.excludeStartPosition());

		Iterable<Person> i = createPersonsFromPath(td.traverse(underlyingNode));
		for (Person p : i) {
			likes.put(p.getFbId() + "_" + p.getBokwasAvatarId(), p.getBokwasName());
		}
		return likes;
	}

	private IterableWrapper<Person, Path> createPersonsFromPath(Traverser iterableToWrap) {
		return new IterableWrapper<Person, Path>(iterableToWrap) {
			@Override
			protected Person underlyingObjectToObject(Path path) {
				Person po = new Person(path.endNode());
				// posts.add(po.getPostId());
				return po;
			}
		};
	}

	public boolean isBokwasPost() {
		return (boolean) underlyingNode.getProperty(IS_BOKWAS_POST);
	}

	public void setIsBokwasPost(boolean isBokwasPost) {
		underlyingNode.setProperty(IS_BOKWAS_POST, isBokwasPost);
	}

	public void setLikes(Node person) {
		underlyingNode.createRelationshipTo(person, RelationType.LIKES);
	}

	public void setPhotourl(String url) {
		underlyingNode.setProperty(PHOTO_URL, url);
	}

	public void setPostId(String id) {
		underlyingNode.setProperty(POST_ID, id);
	}

	public void setPostedBy(String postedBy) {
		underlyingNode.setProperty(POSTED_BY, postedBy);
	}

	public void setPostTime(long time) {
		underlyingNode.setProperty(POST_TIME, time);
	}

	public void setPostUpdatedTime(long time) {
		underlyingNode.setProperty(UPDATED_TIME, time);
	}

	public void setPostText(String text) {
		underlyingNode.setProperty(POST_TEXT, text);
	}

	public void addLikes(String fbId, String l) {
		String likes = l;
		if (likes.contains(fbId)) {
			return;
		}
		likes += fbId + ",";
		// setLikes(likes);
	}

	public Node getUnderlyingNode() {
		return underlyingNode;
	}

	@Override
	public int hashCode() {
		return underlyingNode.hashCode();
	}

	public String getPhotoUrl() {
		return underlyingNode.getProperty(PHOTO_URL).toString();
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof Posts && underlyingNode.equals(((Posts) o).getUnderlyingNode());
	}

	@Override
	public String toString() {
		return "Post[ POST_TEXT : " + getPostText() + " , POSTED_BY : " + getPostedBy() + "]";
	}

	public void addComment(Comments comment) {
		underlyingNode.createRelationshipTo(comment.getUnderlyingNode(), RelationType.COMMENTS_OF);
	}

	public Iterable<Comments> getComments() {
		TraversalDescription travDesc = BokwasDB.getDatabase().traversalDescription().breadthFirst().relationships(RelationType.COMMENTS_OF)
				.uniqueness(Uniqueness.NODE_GLOBAL).evaluator(Evaluators.toDepth(1)).evaluator(Evaluators.excludeStartPosition());

		return createCommentsFromPath(travDesc.traverse(underlyingNode));
	}

	public Comments getComment(String commentId) {
		List<Comments> comments = getCommentsAsList();
		for (Comments comment : comments) {
			if (comment.getCommentId().equals(commentId)) {
				return comment;
			}
		}
		return null;
	}

	public void deletePost(String postId) {
		try (Transaction tx = BokwasDB.getDatabase().beginTx()) {
			Node postNode;
			postNode = BokwasNodeFactory.findPost(postId);

			// REMOVE ALL COMMENT NODES AND RELATIONSHIPS ASSOCIATED TO THE POST
			// BEFORE DELETING
			if (postNode.hasRelationship()) {
				for (Relationship r : postNode.getRelationships(RelationType.COMMENTS_OF)) {
					Node commentNode = r.getOtherNode(underlyingNode);
					r.delete();
					for (Relationship commentRel : commentNode.getRelationships()) {
						commentRel.delete();
					}
				}
				for (Relationship notify : postNode.getRelationships()) {
					notify.delete();
				}

			}

			postNode.delete();
			tx.success();
		}
	}

	public void deleteComment(String commentId) {
		for (Relationship commentRel : underlyingNode.getRelationships(RelationType.COMMENTS_OF)) {
			Node comments = commentRel.getOtherNode(underlyingNode);

			if (comments.getProperty(Comments.COMMENT_ID).toString().equals(commentId)) {
				if (comments.hasRelationship()) {
					for (Relationship r : comments.getRelationships()) {
						r.delete();
					}
				}
				comments.delete();
			}
		}
	}

	public List<Comments> getCommentsAsList() {
		TraversalDescription travDesc = BokwasDB.getDatabase().traversalDescription().breadthFirst().relationships(RelationType.COMMENTS_OF)
				.uniqueness(Uniqueness.NODE_GLOBAL).evaluator(Evaluators.toDepth(1)).sort(new Comparator<Path>() {

					@Override
					public int compare(Path p1, Path p2) {
						Date d1 = new Date((long) p1.endNode().getProperty(Comments.COMMENT_TIME));
						Date d2 = new Date((long) p2.endNode().getProperty(Comments.COMMENT_TIME));
						return d1.compareTo(d2);
					}
				}).evaluator(Evaluators.excludeStartPosition());

		return IteratorUtil.asList(createCommentsFromPath(travDesc.traverse(underlyingNode)));
	}

	private IterableWrapper<Comments, Path> createCommentsFromPath(Traverser iterableToWrap) {
		return new IterableWrapper<Comments, Path>(iterableToWrap) {
			@Override
			protected Comments underlyingObjectToObject(Path path) {
				return new Comments(path.endNode());
			}
		};
	}

	public void removeLikes(String fbid) {
		// underlyingNode.getSingleRelationship(RelationType.LIKES,Direction.BOTH).delete();
		Iterable<Relationship> r = underlyingNode.getRelationships(RelationType.LIKES, Direction.BOTH);

		for (Relationship rel : r) {
			if (rel.getOtherNode(underlyingNode).getProperty("fbid").equals(fbid)) {
				System.out.println("removing like");
				rel.delete();
				return;
			}
		}
	}
}
